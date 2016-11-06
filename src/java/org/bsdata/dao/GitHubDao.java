
package org.bsdata.dao;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sun.jersey.api.NotFoundException;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.bsdata.constants.DataConstants;
import org.bsdata.constants.DataConstants.DataType;
import org.bsdata.constants.PropertiesConstants;
import org.bsdata.constants.WebConstants;
import org.bsdata.model.DataFile;
import org.bsdata.viewmodel.RepositoryVm;
import org.bsdata.viewmodel.RepositoryFileVm;
import org.bsdata.viewmodel.RepositoryListVm;
import org.bsdata.repository.Indexer;
import org.bsdata.utils.ApplicationProperties;
import org.bsdata.utils.Utils;
import org.bsdata.viewmodel.ResponseVm;
import org.eclipse.egit.github.core.Blob;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.CommitUser;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.PullRequestMarker;
import org.eclipse.egit.github.core.Reference;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.SearchRepository;
import org.eclipse.egit.github.core.Tree;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.TypedResource;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;


/**
 * Handles all reading and writing of data files to/from GitHub.
 * Methods in this class will generally convert GitHub objects to/from simpler objects from the viewmodel package.
 * These viewmodel objects can then be converted to/from JSON for sending/receiving over the wire.
 * 
 * @author Jonskichov
 */
public class GitHubDao {
    
    private static final int MAX_FEED_ENTRIES = 5;
    private static final int REPO_CACHE_EXPIRY_MINS = 24 * 60;
    private static final long RELEASE_CACHE_EXPIRY_TIME_MINS = 12 * 60;
  
    private static final Logger logger = Logger.getLogger("org.bsdata");
    
    private static final SimpleDateFormat branchDateFormat = new SimpleDateFormat("yyMMddHHmmssSSS");
    private static final SimpleDateFormat longDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    
    private final Indexer indexer;
    
//    private static Cache<String, List<Repository>> repoListCache;
//    private static Cache<String, List<Release>> repoReleasesCache;
    
    
    private static Date nextReposUpdateDate = new Date();
    private static boolean reposRequiresUpdate = true;
    private static ReentrantLock reposUpdateLock = new ReentrantLock(true);
    
    private static final Map<String, Repository> repos 
            = Collections.synchronizedMap(new HashMap<String, Repository>());
    
    private static final Map<String, List<Release>> repoReleases 
            = Collections.synchronizedMap(new HashMap<String, List<Release>>());
    
    
    private static final Map<String, Date> repoReleaseDates 
            = Collections.synchronizedMap(new HashMap<String, Date>());
    
    private static final Map<String, Boolean> dataRequiresUpdateFlags 
            = Collections.synchronizedMap(new HashMap<String, Boolean>());
    
    private static final Map<String, ReentrantLock> repoDownloadLocks 
            = Collections.synchronizedMap(new HashMap<String, ReentrantLock>());
    
    private static final Map<String, HashMap<String, DataFile>> repoFiles 
            = Collections.synchronizedMap(new HashMap<String, HashMap<String, DataFile>>());
    
    private static final Map<String, List<Entry>> repoFeedEntries 
            = Collections.synchronizedMap(new HashMap<String, List<Entry>>());
    
    
    
    public GitHubDao() {
        indexer = new Indexer();
        setupCaches();
    }
    
    /**
     * Create the caches if we need to
     */
    private static synchronized void setupCaches() {
        if (repoListCache != null && repoReleasesCache != null) {
            // Caches already created
            return;
        }
        
        repoListCache = CacheBuilder.newBuilder()
            .expireAfterWrite(REPO_CACHE_EXPIRY_MINS, TimeUnit.MINUTES)
            .build();
        
        repoReleasesCache = CacheBuilder.newBuilder()
            .expireAfterWrite(RELEASE_CACHE_EXPIRY_TIME_MINS, TimeUnit.MINUTES)
            .build();
    }
    
    /**
     * Gets a connection to GitHub for BSDataAnon using the OAuth token in bsdata.properties
     * 
     * @return
     * @throws IOException 
     */
    private GitHubClient connectToGitHub() throws IOException {
        Properties properties = ApplicationProperties.getProperties();
        GitHubClient gitHubClient = new GitHubClient();
        gitHubClient.setOAuth2Token(properties.getProperty(PropertiesConstants.GITHUB_ANON_TOKEN));
        gitHubClient.setUserAgent(properties.getProperty(PropertiesConstants.GITHUB_ANON_USERNAME));
        return gitHubClient;
    }
    
    /**
     * Ensures data files are cached for all data file repositories.
     * 
     * @param baseUrl
     * @param repoName
     * @throws IOException 
     */
    public synchronized void primeCache(String baseUrl, String repoName) throws IOException {
        // Clear the caches so it forces data to be re-cached.
        repoReleaseDates.remove(repoName);
        repoReleasesCache.invalidate(repoName);
        repoReleasesCache.cleanUp();
        
        dataRequiresUpdateFlags.put(repoName, Boolean.TRUE);
        
        // Get the repo data to repopulate the cache
        getRepoFileData(repoName, baseUrl);
    }
    
    
    //////////////////
    // Repositories //
    //////////////////
    
    private boolean reposRequireUpdate() {
        if (nextReposUpdateDate == null
                || repos.isEmpty()
                || repoReleases.isEmpty()) {
            
            reposRequiresUpdate = true;
            return true;
        }
        
        if (reposRequiresUpdate == true) {
            return true;
        }
        
        if (new Date().after(nextReposUpdateDate)) {
            reposRequiresUpdate = true;
            return true;
        }
        
        return false;
    }
    
    private Future<Void> refreshRepositoriesAsync(final String organizationName) {
        return Executors.newSingleThreadExecutor().submit(new Callable<Void>() {

            @Override
            public Void call() throws IOException {
                try {
                    refreshRepositories(organizationName);
                }
                catch (IOException e) {
                    logger.log(
                            Level.SEVERE, 
                            "Failed to update repositories", 
                            e);
                    
                    throw e;
                }
                return null;
            }
        });
    }
    
    private void refreshRepositories(String organizationName) throws IOException {
        if (reposUpdateLock.isLocked()) {
            // We are currently updating the repos
            return;
        }
            
        try {
            reposUpdateLock.lock();
            
            List<Repository> repositories = getGitHubRepositories(organizationName);

            for (Repository repo : repositories) {
                List<Release> releases;
                try {
                    releases = getGitHubReleases(repo);
                }
                catch (IOException e) {

                    continue;
                }

                repos.put(repo.getName(), repo);
                repoReleases.put(repo.getName(), releases);
            }
            
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, REPO_CACHE_EXPIRY_MINS);
            nextReposUpdateDate = calendar.getTime();
            reposRequiresUpdate = false;
        }
        finally {
            // Done! Unlock in a finally block as we don't want to leave anything locked if there's an exception...
            reposUpdateLock.unlock();
        }
    }
    
    private List<Repository> getGitHubRepositories(String organizationName) throws IOException {
        logger.log(Level.INFO, "Getting repositories from GitHub for {0}.", organizationName);

        RepositoryService repositoryService = new RepositoryService(connectToGitHub());
        List<Repository> repositories = repositoryService.getOrgRepositories(organizationName);
        if (repositories == null) {
            // Callable must return a value or throw an exception - can't return null
            throw new IllegalArgumentException();
        }
        return repositories;
    }
    
    private List<Release> getGitHubReleases(Repository repository) throws IOException {
        logger.log(Level.INFO, "Getting releases from GitHub for {0}.", repository.getName());

        ReleaseService releaseService = new ReleaseService(connectToGitHub());
        List<Release> releases = releaseService.getReleases(repository, 1, 1);
        if (releases == null) {
            // Callable must return a value or throw an exception - can't return null
            throw new IllegalArgumentException();
        }

        // Sort by publish date so latest release is at position 0
        Collections.sort(releases, new Comparator<Release>() {

            @Override
            public int compare(Release o1, Release o2) {
                return o2.getPublishedAt().compareTo(o1.getPublishedAt());
            }
        });
        
        return releases;
    }
    
    
    
    
    
    private Repository getRepository(String organizationName, String repositoryName) throws IOException {
        for (Repository repository : getRepositories(organizationName)) {
            if (repository.getName().equals(repositoryName)) {
                return repository;
            }
        }
        
        // We don't know about that repo.
        throw new NotFoundException("Could not find repository " + repositoryName + " in organization " + organizationName);
    }
    
    private Release getLatestRelease(Repository repository) throws IOException {
        List<Release> releases = getReleases(repository);
        if (releases.isEmpty()) {
            return null;
        }
        
        // Releases should be sorted so latest is at position 0
        return getReleases(repository).get(0);
    }
    
    
    //////////////
    // Releases //
    //////////////
    
    /**
     * Get the first page of releases from GitHub for the given repository
     * 
     * @param repository
     * @return
     * @throws ExecutionException 
     */
    private List<Release> getReleases(final Repository repository) throws IOException {
        try {
            return repoReleasesCache.get(repository.getName(), new Callable<List<Release>>() {
                
                @Override
                public List<Release> call() throws IOException {
                }
            });
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException)e.getCause();
            }
            // Callable should only throw an IOException. If we get here, something fishy is going on...
            throw new IllegalStateException(e.getCause());
        }
    }
    
    
    //////////////////
    // Data Refresh //
    //////////////////
    
    private boolean requiresDataRefresh(Repository repository, Release latestRelease) {
        String repoName = repository.getName();
        
        if (latestRelease == null) {
            dataRequiresUpdateFlags.put(repoName, false);
            return false;
        }
        
        if (!dataRequiresUpdateFlags.containsKey(repoName)
                || !repoReleaseDates.containsKey(repoName)
                || !repoFiles.containsKey(repoName)
                || !repoFeedEntries.containsKey(repoName)) {
            
            dataRequiresUpdateFlags.put(repoName, true);
            return true;
        }
        
        if (dataRequiresUpdateFlags.get(repoName) == true) {
            return true;
        }
        
        if (latestRelease.getPublishedAt().after(repoReleaseDates.get(repoName))) {
            dataRequiresUpdateFlags.put(repoName, true);
            return true;
        }
        
        return false;
    }
    
    private Future<Void> refreshDataAsync(final String baseUrl, final Repository repository, final Release latestRelease) {
        return Executors.newSingleThreadExecutor().submit(new Callable<Void>() {

            @Override
            public Void call() throws IOException {
                try {
                    refreshData(baseUrl, repository, latestRelease);
                }
                catch (IOException e) {
                    logger.log(
                            Level.SEVERE, 
                            "Failed to update data for repository " + repository.getName(), 
                            e);
                    
                    throw e;
                }
                return null;
            }
        });
    }
    
    private void refreshData(String baseUrl, Repository repository, Release latestRelease) throws IOException {
        String repoName = repository.getName();
        
        if (!repoDownloadLocks.containsKey(repoName)) {
            // Create a lock object for this repo name if we don't already have one
            repoDownloadLocks.put(repoName, new ReentrantLock(true));
        }

        // Get the lock object associated with this repo (want to prevent multiple threads downloading from the same repo at the same time)
        ReentrantLock downloadLock = repoDownloadLocks.get(repoName);
        if (downloadLock.isLocked()) {
            // We are currently downloading for this repo
            return;
        }
        
        try {
            downloadLock.lock();
        
            // Download and cache the repository data files
            HashMap<String, byte[]> dataFiles = downloadFromGitHub(repository, latestRelease);
            HashMap<String, DataFile> repositoryData = indexer.createRepositoryData(repoName, baseUrl, null, dataFiles);


            // Setup feed
            List<Entry> releaseFeedEntries = getReleaseFeedEntries(baseUrl, repository);


            repoFiles.put(repoName, repositoryData);
            repoFeedEntries.put(repoName, releaseFeedEntries);
            repoReleaseDates.put(repoName, latestRelease.getPublishedAt());
            dataRequiresUpdateFlags.put(repoName, Boolean.FALSE);
        }
        finally {
            // Done! Unlock in a finally block as we don't want to leave anything locked if there's an exception...
            downloadLock.unlock();
        }
    }
    
    /**
     * Downloads all the data files from a specific release in a specific repository.
     * Returns a HashMap of uncompressed filename --> uncompressed file data
     * 
     * @param repositoryName
     * @return
     * @throws IOException 
     */
    private HashMap<String, byte[]> downloadFromGitHub(Repository repository, Release release) throws IOException {
        logger.log(Level.INFO, "Downloading data from GitHub for {0}.", repository.getName());
        
        String zipUrl = repository.getHtmlUrl();
        if (!zipUrl.endsWith("/")) {
            zipUrl += "/";
        }
        zipUrl += "archive/" + release.getTagName() + DataConstants.ZIP_FILE_EXTENSION;
        
        byte[] fileData = Utils.downloadFile(zipUrl);
        
        return Utils.unpackZip(fileData);
    }
    
    private List<Entry> getReleaseFeedEntries(String baseUrl, Repository repository) throws IOException {
        logger.log(Level.INFO, "Creating feed entries for {0}.", repository.getName());
            
        List<Entry> entries = new ArrayList<>();
        
        for (Release release : getReleases(repository)) {
            Entry entry = new Entry();
            
            entry.setId(release.getHtmlUrl());
            entry.setTitle(repository.getDescription() + ": " + release.getName());
            entry.setPublished(release.getPublishedAt());
            entry.setUpdated(release.getPublishedAt());
            
            Link link = new Link();
            link.setType(DataConstants.HTML_MIME_TYPE);
            link.setHref(getFeedHref(baseUrl, repository.getName()));
            entry.setAlternateLinks(Collections.singletonList(link));
            
            StringBuilder contentBuffer = new StringBuilder();
            if (!StringUtils.isEmpty(release.getBody())) {
                contentBuffer.append("<p>").append(release.getBody()).append("</p>");
            }
            contentBuffer.append("<p><a href=\"").append(link.getHref()).append("\">Source repository details</a>");
            contentBuffer.append("<br><a href=\"").append(release.getHtmlUrl()).append("\">GitHub release details</a></p>");
            
            Content description = new Content();
            description.setType(DataConstants.HTML_MIME_TYPE);
            description.setValue(contentBuffer.toString());
            entry.setSummary(description);
            
            entries.add(entry);
            
            if (entries.size() >= MAX_FEED_ENTRIES) {
                break;
            }
        }
        
        return entries;
    }
    
    private String getFeedHref(String baseUrl, String repositoryName) {
        if (repositoryName == null || repositoryName.equals(WebConstants.ALL_REPO_FEEDS)) {
            return Utils.checkUrl(baseUrl.replace(WebConstants.REPO_SERVICE_PATH, "#/repos"));
        }
        else {
            return Utils.checkUrl(baseUrl.replace(WebConstants.REPO_SERVICE_PATH, "#/repo/" + repositoryName));
        }
    }
    
    
    //////////////
    // Get Data //
    //////////////
    
    /**
     * Gets the data files for a particular data file repository. File data is cached.
     * 
     * @param repoName
     * @param baseUrl
     * @return
     * @throws IOException
     */
    public HashMap<String, DataFile> getRepoFileData(
            String repoName, 
            String baseUrl) throws IOException {
        
        HashMap<String, DataFile> fileData = repoFiles.get(repoName);
        
        String organizationName = ApplicationProperties.getProperties().getProperty(PropertiesConstants.GITHUB_ORGANIZATION);
        Repository repository = getRepository(organizationName, repoName);
        Release latestRelease = getLatestRelease(repository);
        
        if (fileData == null) {
            refreshData(baseUrl, repository, latestRelease);
            return repoFiles.get(repoName);
        }
        
        if (requiresDataRefresh(repository, latestRelease)) {
            refreshDataAsync(baseUrl, repository, latestRelease);
        }
        
        return fileData;
    }
    
    public Feed getReleaseFeed(String baseUrl, String repoName) throws IOException {
        if (repoName.toLowerCase().equals(WebConstants.ALL_REPO_FEEDS)) {
            List<Entry> entries = new ArrayList<>();
            for (List<Entry> repoEntries : repoFeedEntries.values()) {
                entries.addAll(repoEntries);
            }
            
            return getReleaseFeed(
                    baseUrl, 
                    WebConstants.ALL_REPO_FEEDS, 
                    "All Repository Releases", 
                    "Data file releases for all repositories", 
                    entries);
        }
        else {
            List<Entry> entries = repoFeedEntries.get(repoName);
            if (entries == null) {
                entries = new ArrayList<>();
            }
            
            return getReleaseFeed(
                    baseUrl, 
                    repoName, 
                    repoName + " Releases", 
                    "Data file releases for " + repoName, 
                    entries);
        }
    }
    
    private Feed getReleaseFeed(
            String baseUrl, 
            String repoName,
            String title, 
            String description, 
            List<Entry> entries) {
        
        String feedUrl = Utils.checkUrl(baseUrl + "/feeds/" + repoName + ".atom");
        
        Feed feed = new Feed();
        feed.setFeedType("atom_1.0");
        feed.setId(feedUrl);
        
        feed.setTitle(title);
            
        Content subTitle = new Content();
        subTitle.setType(DataConstants.TEXT_MIME_TYPE);
        subTitle.setValue(description);
        feed.setSubtitle(subTitle);
        
        Person author = new Person();
        author.setName("BattleScribe Data");
        author.setUrl(baseUrl.replace("/" + WebConstants.REPO_SERVICE_PATH, ""));
        feed.setAuthors(Collections.singletonList(author));
            
        Link selfLink = new Link();
        selfLink.setRel("self");
        selfLink.setHref(feedUrl);
        feed.setOtherLinks(Collections.singletonList(selfLink));
            
        Link altLink = new Link();
        altLink.setType(DataConstants.HTML_MIME_TYPE);
        altLink.setHref(getFeedHref(baseUrl, repoName));
        feed.setAlternateLinks(Collections.singletonList(altLink));
        
        Collections.sort(entries, new Comparator<Entry>() {
            
            @Override
            public int compare(Entry o1, Entry o2) {
                return o2.getPublished().compareTo(o1.getPublished());
            }
        });
        
        if (!entries.isEmpty()) {
            feed.setUpdated(entries.get(0).getPublished());
        }
        else {
            feed.setUpdated(new Date());
        }
        
        feed.setEntries(entries);
        return feed;
    }
    
    
    ////////////////
    // View Model //
    ////////////////
    
    /**
     * Gets details of the data file repositories managed by the system.
     * 
     * @param baseUrl
     * @return
     * @throws IOException 
     */
    public RepositoryListVm getRepos(String baseUrl) throws IOException {
        Properties properties = ApplicationProperties.getProperties();
        String organizationName = properties.getProperty(PropertiesConstants.GITHUB_ORGANIZATION);
        List<Repository> orgRepositories = getRepositories(organizationName);
        
        boolean isDev = baseUrl.toLowerCase().contains("localhost") || baseUrl.toLowerCase().contains("bsdatadev");
        
        List<RepositoryVm> repositories = new ArrayList<>();
        for (Repository repository : orgRepositories) {
            if (repository.getName().equals(DataConstants.GITHUB_BSDATA_REPO_NAME)) {
                continue;
            }

            if (!isDev && repository.getName().toLowerCase().equals("test")) {
                continue; // Hack to prevent test repo showing up on the live site
            }

            Release latestRelease = getLatestRelease(repository);
            if (latestRelease == null) {
                continue;
            }
            RepositoryVm repositoryVm = createRepositoryVm(repository, baseUrl, latestRelease);
            repositories.add(repositoryVm);
        }
                        
        Collections.sort(repositories, new Comparator<RepositoryVm>() {
            @Override
            public int compare(RepositoryVm o1, RepositoryVm o2) {
                return o1.getDescription().compareTo(o2.getDescription());
            }
        });
        
        RepositoryListVm repositoryList = new RepositoryListVm();
        repositoryList.setRepositories(repositories);
        repositoryList.setFeedUrl(baseUrl + "/feeds/all.atom");
        repositoryList.setTwitterUrl(properties.getProperty(PropertiesConstants.TWITTER_URL));
        repositoryList.setFacebookUrl(properties.getProperty(PropertiesConstants.FACEBOOK_URL));
        return repositoryList;
    }
    
    /**
     * Gets details of a particular data file repository and the files stored in it.
     * 
     * @param repositoryName
     * @param baseUrl
     * @return
     * @throws IOException 
     */
    public RepositoryVm getRepoFiles(String repositoryName, String baseUrl) throws IOException {
        String organizationName = ApplicationProperties.getProperties().getProperty(PropertiesConstants.GITHUB_ORGANIZATION);
        Repository repository = getRepository(organizationName, repositoryName);
        Release latestRelease = getLatestRelease(repository);
        RepositoryVm repositoryVm = createRepositoryVm(repository, baseUrl, latestRelease);
        
        HashMap<String, DataFile> repoFileData = getRepoFileData(repositoryName, baseUrl);
        List<RepositoryFileVm> repositoryFiles = new ArrayList<>();
        for (String fileName : repoFileData.keySet()) {
            if (!Utils.isDataFilePath(fileName)) {
                continue;
            }
            
            DataFile dataFile = repoFileData.get(fileName);
            RepositoryFileVm repositoryFile = new RepositoryFileVm();
            repositoryFile.setName(fileName);
            if (Utils.isCataloguePath(fileName)) {
                repositoryFile.setType(StringUtils.capitalize(DataType.CATALOGUE.toString()));
            }
            else if (Utils.isGameSytstemPath(fileName)) {
                repositoryFile.setType(StringUtils.capitalize(DataType.GAME_SYSTEM.toString()));
            }
            else if (Utils.isRosterPath(fileName)) {
                repositoryFile.setType(StringUtils.capitalize(DataType.ROSTER.toString()));
            }
            
            repositoryFile.setDataFileUrl(Utils.checkUrl(baseUrl + "/" + repository.getName() + "/" + fileName));
            repositoryFile.setIssueUrl(Utils.checkUrl(baseUrl + "/" + repository.getName() + "/" + fileName + "/issue"));
            
            String uncompressedFileName = Utils.getUncompressedFileName(fileName);
            repositoryFile.setGitHubUrl(Utils.checkUrl(repositoryVm.getGitHubUrl() + "/blob/master/" + uncompressedFileName));
            
            repositoryFile.setRevision(dataFile.getRevision());
            repositoryFile.setAuthorName(dataFile.getAuthorName());
            repositoryFile.setAuthorContact(dataFile.getAuthorContact());
            repositoryFile.setAuthorUrl(dataFile.getAuthorUrl());
            
            repositoryFiles.add(repositoryFile);
        }
        
        Collections.sort(repositoryFiles, new Comparator<RepositoryFileVm>() {
            @Override
            public int compare(RepositoryFileVm o1, RepositoryFileVm o2) {
                String o1Type = o1.getType().toLowerCase();
                String o2Type = o2.getType().toLowerCase();
                if (o1Type.equals(DataType.CATALOGUE.toString())
                        && o2Type.equals(DataType.GAME_SYSTEM.toString())) {
                    return 1;
                }
                else if (o1Type.equals(DataType.GAME_SYSTEM.toString())
                        && o2Type.equals(DataType.CATALOGUE.toString())) {
                    return -1;
                }
                
                return o1.getName().compareTo(o2.getName());
            }
        });
        
        repositoryVm.setRepositoryFiles(repositoryFiles);
        return repositoryVm;
    }
    
    private RepositoryVm createRepositoryVm(Repository repository, String baseUrl, Release latestRelease) {
        RepositoryVm repositoryVm = new RepositoryVm();
        repositoryVm.setName(repository.getName());
        repositoryVm.setDescription(repository.getDescription());
        if (latestRelease != null) {
            repositoryVm.setLastUpdated(longDateFormat.format(latestRelease.getPublishedAt()));
            repositoryVm.setLastUpdateDescription(latestRelease.getName());
        }
        repositoryVm.setRepoUrl(Utils.checkUrl(baseUrl + "/" + repository.getName() + "/" + DataConstants.DEFAULT_INDEX_COMPRESSED_FILE_NAME));
        repositoryVm.setGitHubUrl(repository.getHtmlUrl());
        repositoryVm.setBugTrackerUrl(repository.getHtmlUrl() + "/issues");
        repositoryVm.setFeedUrl(Utils.checkUrl(baseUrl + "/feeds/" + repository.getName() + ".atom"));
        return repositoryVm;
    }
    
    /**
     * See http://developer.github.com/v3/git/
     * 
     * 1) Create a fork of the main repository if BSDataAnon doesn't already have one
     * 2) Commit the file submission to a new branch of the fork
     * 3) Create a pull request from the new branch back to the original bsdata repo
     * 
     * @param repositoryName
     * @param fileName
     * @param sourceFileName
     * @param fileData
     * @param commitMessage
     * @return 
     * @throws IOException 
     */
    public ResponseVm submitFile(String repositoryName, String fileName, String sourceFileName, byte[] fileData, String commitMessage) throws IOException {
        // Ensure we have the decompressed filename for the destination
        fileName = Utils.getUncompressedFileName(FilenameUtils.getName(fileName));
        
        if (Utils.isCompressedPath(sourceFileName)) {
            // If source file name is compressed, decompress the data
            fileData = Utils.decompressData(fileData);
        }
        
        GitHubClient gitHubClient = connectToGitHub();
        DataService dataService = new DataService(gitHubClient);
        CommitService commitService = new CommitService(gitHubClient);
        PullRequestService pullRequestService = new PullRequestService(gitHubClient);
        
        // Get BSDataAnon's fork of the repo (creates one if it doesn't already exist)
        String organizationName = ApplicationProperties.getProperties().getProperty(PropertiesConstants.GITHUB_ORGANIZATION);
        Repository repositoryMaster = getRepository(organizationName, repositoryName);
        Repository repositoryFork = getRepositoryFork(gitHubClient, repositoryMaster);
        
        // get the current commit on the master branch in the fork and get the tree it points to
        Reference masterRefFork = dataService.getReference(repositoryFork, "heads/master");
        RepositoryCommit latestCommitFork = commitService.getCommit(repositoryFork, masterRefFork.getObject().getSha());
        Tree masterTreeFork = latestCommitFork.getCommit().getTree();
        
        // post a new blob object with new content, getting a blob SHA back
        Blob contentBlobFork = new Blob();
        contentBlobFork.setContent(Base64.encodeBase64String(fileData));
        contentBlobFork.setEncoding("base64");
        String blobSha = dataService.createBlob(repositoryFork, contentBlobFork);
        
        // post a new tree object with file path pointer = your new blob SHA getting a tree SHA back
        TreeEntry treeEntryFork = new TreeEntry();
        treeEntryFork.setPath(fileName);
        treeEntryFork.setMode("100644");
        treeEntryFork.setType("blob");
        treeEntryFork.setSha(blobSha);
        Collection<TreeEntry> treeEntries = new ArrayList<>();
        treeEntries.add(treeEntryFork);
        Tree treeFork = dataService.createTree(repositoryFork, treeEntries, masterTreeFork.getSha());
        
        Properties properties = ApplicationProperties.getProperties();
        CommitUser commitUser = new CommitUser();
        commitUser.setName(properties.getProperty(PropertiesConstants.GITHUB_ANON_USERNAME));
        commitUser.setEmail(properties.getProperty(PropertiesConstants.GITHUB_ANON_EMAIL));
        commitUser.setDate(new Date());
        
        // create a new commit object with the current commit SHA as the parent and the new tree SHA, getting a commit SHA back
        Commit commitFork = new Commit();
        commitFork.setMessage(commitMessage);
        commitFork.setTree(treeFork);
        commitFork.setAuthor(commitUser);
        commitFork.setCommitter(commitUser);
        Commit parentCommit = latestCommitFork.getCommit();
        parentCommit.setSha(latestCommitFork.getSha()); // For some reason the parentCommit's sha isn't set
        List<Commit> commitParentsFork = new ArrayList<>();
        commitParentsFork.add(parentCommit);
        commitFork.setParents(commitParentsFork);
        commitFork = dataService.createCommit(repositoryFork, commitFork);
        
        // create a new branch reference pointing to the new commit SHA
        Reference branchRefFork = new Reference();
        TypedResource resourceFork = new TypedResource();
        resourceFork.setType(TypedResource.TYPE_COMMIT);
        resourceFork.setSha(commitFork.getSha());
        resourceFork.setUrl(commitFork.getUrl());
        branchRefFork.setObject(resourceFork);
        String branchNameFork = FilenameUtils.getBaseName(fileName).trim().replace(" ", "_").replaceAll("[^A-Za-z0-9]", "") 
                + "_" + branchDateFormat.format(new Date());
        branchRefFork.setRef("refs/heads/" + branchNameFork);
        dataService.createReference(repositoryFork, branchRefFork);
         
        StringBuilder issueBody = new StringBuilder();
        issueBody.append("**File:** ").append(fileName)
                .append("\n\n**Description:** ").append(commitMessage);
        
        // Submit a pull request back to the source repository
        PullRequestMarker sourceRequestMarker = new PullRequestMarker();
        sourceRequestMarker.setLabel("BSDataAnon:" + branchNameFork); // Source is our new branch in the fork
        PullRequestMarker destinationRequestMarker = new PullRequestMarker();
        destinationRequestMarker.setLabel("master"); // Destination is the master branch of the bsdata repository
        PullRequest pullRequest = new PullRequest();
        pullRequest.setTitle("[Anon] File update: " + fileName);
        pullRequest.setHead(sourceRequestMarker);
        pullRequest.setBase(destinationRequestMarker);
        pullRequest.setBody(issueBody.toString());
        pullRequest = pullRequestService.createPullRequest(repositoryMaster, pullRequest);
        
        ResponseVm responseVm = new ResponseVm();
        responseVm.setSuccessMessage("Successfully submitted file update for " + fileName + ".");
        responseVm.setResponseUrl(pullRequest.getHtmlUrl());
        return responseVm;
    }
    
    /**
     * Get BSDataAnon's fork of the main repo.
     * 1) Search for the fork.
     * 2) If we can't find it, fork the main repo and return the fork.
     * 3) Otherwise compare the number of releases in the fork to the number of releases in the main repo.
     * 4) If the number of releases is different, delete the fork, re-fork the main repo and return the fork.
     * 5) Otherwise just return the fork we found in the search.
     * 
     * @param gitHubClient
     * @param repositoryName
     * @return
     * @throws IOException 
     */
    private Repository getRepositoryFork(GitHubClient gitHubClient, Repository masterRepository) throws IOException {
        Properties properties = ApplicationProperties.getProperties();
        RepositoryService repositoryService = new RepositoryService(gitHubClient);
        
        List<SearchRepository> searchRepositories = repositoryService.searchRepositories(
                masterRepository.getName()
                + " user:" + properties.getProperty(PropertiesConstants.GITHUB_ANON_USERNAME) 
                + " fork:only");
        
        if (searchRepositories.isEmpty()) {
            logger.log(Level.INFO, "No anon fork of {0} found. Forking.", masterRepository.getName());
            return forkAndWait(gitHubClient, masterRepository);
        }
        
        Release latestRelease = getLatestRelease(masterRepository);
        Repository repositoryFork = repositoryService.getRepository(
                properties.getProperty(PropertiesConstants.GITHUB_ANON_USERNAME), 
                masterRepository.getName());
        
        if (latestRelease.getPublishedAt().after(repositoryFork.getCreatedAt())) {
            // There's been a release on the master since we last forked, so delete and re-create
            logger.log(Level.INFO, "New release found since anon fork of {0} was created. Deleting and re-forking.", masterRepository.getName());
            deleteAndWait(gitHubClient, repositoryFork);
            return forkAndWait(gitHubClient, masterRepository);
        }
        else {
            return repositoryFork;
        }
    }
    
    @SuppressWarnings("SleepWhileInLoop")
    private Repository forkAndWait(GitHubClient gitHubClient, Repository masterRepository) throws IOException {
        Properties properties = ApplicationProperties.getProperties();
        RepositoryService repositoryService = new RepositoryService(gitHubClient);
        Repository repositoryFork = repositoryService.forkRepository(masterRepository);
        
        int maxAttempts = 5;
        for (int i = 0; i < maxAttempts; i++) {
            try {
                logger.log(Level.INFO, "Waiting for fork of {0} to become available...", masterRepository.getName());
                Thread.sleep(5 * 1000);
                
                List<SearchRepository> searchRepositories = repositoryService.searchRepositories(
                        masterRepository.getName()
                        + " user:" + properties.getProperty(PropertiesConstants.GITHUB_ANON_USERNAME) 
                        + " fork:only");
                
                if (searchRepositories.size() == 1) {
                    return repositoryFork;
                }
            }
            catch (InterruptedException | IOException e) {
                logger.log(Level.INFO, "Error waiting for fork of {0} to create: {1}", new String[] {masterRepository.getName(), e.getMessage()});
            }
        }
        
        logger.log(Level.INFO, "Fork for {0} took too long to create.", masterRepository.getName());
        gitHubClient.delete("/repos/" + properties.getProperty(PropertiesConstants.GITHUB_ANON_USERNAME) + "/" + masterRepository.getName());
        throw new IOException("Repository fork could not be created.");
    }
    
    @SuppressWarnings("SleepWhileInLoop")
    private boolean deleteAndWait(GitHubClient gitHubClient, Repository repository) throws IOException {
        Properties properties = ApplicationProperties.getProperties();
        RepositoryService repositoryService = new RepositoryService(gitHubClient);
        gitHubClient.delete("/repos/" + properties.getProperty(PropertiesConstants.GITHUB_ANON_USERNAME) + "/" + repository.getName());
        
        int maxAttempts = 5;
        for (int i = 0; i < maxAttempts; i++) {
            try {
                logger.log(Level.INFO, "Waiting for fork of {0} to delete...", repository.getName());
                Thread.sleep(5 * 1000);
                
                List<SearchRepository> searchRepositories = repositoryService.searchRepositories(
                        repository.getName()
                        + " user:" + properties.getProperty(PropertiesConstants.GITHUB_ANON_USERNAME) 
                        + " fork:only");
                
                if (searchRepositories.isEmpty()) {
                    return true;
                }
            }
            catch (InterruptedException | IOException e) {
                logger.log(Level.INFO, "Error waiting for fork of {0} to delete: {1}", new String[] {repository.getName(), e.getMessage()});
            }
        }
        
        logger.log(Level.INFO, "Deleting repository {0} took too long.", repository.getName());
        throw new IOException("Repository fork could not be deleted.");
    }
    
    public ResponseVm createIssue(
            String repositoryName, String fileName, String battleScribeVersion, String platform, boolean usingDropbox, String body) throws IOException {
        
        StringBuilder issueBody = new StringBuilder();
        issueBody.append("**File:** ").append(fileName)
                .append("\n\n**BattleScribe version:** ").append(battleScribeVersion)
                .append("\n\n**Platform:** ").append(platform)
                .append("\n\n**Dropbox:** ");
        if (usingDropbox) {
            issueBody.append("Yes");
        }
        else {
            issueBody.append("No");
        }
        issueBody.append("\n\n**Description:** ").append(body);
        
        GitHubClient gitHubClient = connectToGitHub();
        IssueService issueService = new IssueService(gitHubClient);
        
        String organizationName = ApplicationProperties.getProperties().getProperty(PropertiesConstants.GITHUB_ORGANIZATION);
        Repository repository = getRepository(organizationName, repositoryName);
        
        Issue issue = new Issue();
        issue.setTitle("[Anon] Bug report: " + fileName);
        issue.setBody(issueBody.toString());
        
        issue = issueService.createIssue(repository, issue);
        
        ResponseVm responseVm = new ResponseVm();
        responseVm.setSuccessMessage("Successfully submitted bug report for " + fileName + ".");
        responseVm.setResponseUrl(issue.getHtmlUrl());
        return responseVm;
    }
}
