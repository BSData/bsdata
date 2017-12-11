
package org.battlescribedata.rest;

import com.google.gson.Gson;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.WireFeedOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.battlescribedata.constants.DataConstants;
import org.battlescribedata.constants.WebConstants;
import org.battlescribedata.dao.GitHubDao;
import org.battlescribedata.model.DataFile;
import org.battlescribedata.viewmodel.RepositorySourceVm;
import org.battlescribedata.utils.Utils;
import org.battlescribedata.viewmodel.RepositoryVm;
import org.battlescribedata.viewmodel.ResponseVm;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;


@Path(WebConstants.REPO_SERVICE_PATH)
public class RepoResource {
  
    @Inject
    private Logger logger;
    
    @Inject
    private Properties properties;
    
    @Inject
    private GitHubDao gitHubDao;

    /**
     * Creates a new instance of RepoService
     */
    public RepoResource() {
    }
    
    /**
     * Returns the URL of this service request without the path info/conext.
     * So... http://something.com/REPO_SERVICE_PATH/some/thing becomes http://something.com/REPO_SERVICE_PATH
     * 
     * @param request
     * @return 
     */
    public static String getBaseUrl(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        url = url.substring(0, url.indexOf(WebConstants.REPO_SERVICE_PATH) + WebConstants.REPO_SERVICE_PATH.length());
        return url;
    }

    @GET
    @Path("/{repoName}/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(
            @PathParam("repoName") String repoName, 
            @PathParam("fileName") String fileName,
            @Context HttpServletRequest request) {
        
        if (StringUtils.isEmpty(repoName)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if (StringUtils.isEmpty(fileName)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        String mimeType;
        if (Utils.isRosterPath(fileName)) {
            mimeType = DataConstants.ROSTER_FILE_MIME_TYPE;
        }
        else if (Utils.isCataloguePath(fileName)) {
            mimeType = DataConstants.CATALOGUE_FILE_MIME_TYPE;
        }
        else if (Utils.isGameSytstemPath(fileName)) {
            mimeType = DataConstants.GAME_SYSTEM_FILE_MIME_TYPE;
        }
        else if (Utils.isIndexPath(fileName)) {
            mimeType = DataConstants.INDEX_FILE_MIME_TYPE;
        }
        else {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        Map<String, DataFile> repoData;
        try {
            repoData = gitHubDao.getRepoFileData(getBaseUrl(request), repoName);
        }
        catch (NotFoundException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load repo data: {0}", e.getMessage());
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        DataFile dataFile = repoData.get(fileName);
        if (dataFile == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        return Response
                .ok()
                .entity(dataFile.getData())
                .type(mimeType)
                .build();
    }

    @GET
    @Path("/{repoName}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getRepositoryFiles(
            @PathParam("repoName") String repoName,
            @Context HttpServletRequest request) {
        
        RepositoryVm repositoryVm;
        Gson gson = new Gson();
        
        if (StringUtils.isEmpty(repoName)) {
            repositoryVm = new RepositoryVm();
            repositoryVm.setErrorMessage("You must provide a repository name.");
            return gson.toJson(repositoryVm);
        }
    
        try {
            repositoryVm = gitHubDao.getRepoFiles(getBaseUrl(request), repoName);
        }
        catch (NotFoundException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            repositoryVm = new RepositoryVm();
            repositoryVm.setErrorMessage("Could not find repository " + repoName + ".");
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load repo file list: {0}", e.getMessage());
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        if (repositoryVm == null) {
            throw new NotFoundException("Could not find repository named " + repoName);
        }
        
        return gson.toJson(repositoryVm);
    }

    @GET
    @Path("/feeds/{repoName}.atom")
    @Produces(MediaType.APPLICATION_ATOM_XML)
    public String getRepositoryFeed(
            @PathParam("repoName") String repoName,
            @Context HttpServletRequest request) {
        
        if (StringUtils.isEmpty(repoName)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    
        StringWriter writer = new StringWriter();
        try {
            Feed feed = gitHubDao.getReleaseFeed(getBaseUrl(request), repoName);
            WireFeedOutput feedOutput = new WireFeedOutput();
            feedOutput.output(feed, writer);
        }
        catch (NotFoundException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load repo list: {0}", e.getMessage());
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        catch (FeedException e) {
            logger.log(Level.SEVERE, "Failed to create feed: {0}", e.getMessage());
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        return writer.toString();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getRepositories(@Context HttpServletRequest request) {
        RepositorySourceVm repositoryList;
        try {
            repositoryList = gitHubDao.getRepos(getBaseUrl(request));
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load repo list: {0}", e.getMessage());
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        Gson gson = new Gson();
        return gson.toJson(repositoryList);
    }

    @POST
    @Path("/{repoName}/{fileName}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public String submitFile(
            @PathParam("repoName") String repoName, 
            @PathParam("fileName") String fileName,
            @FormDataParam("commitMessage") String commitMessage,
            @FormDataParam("file") InputStream file,
            @FormDataParam("file") FormDataContentDisposition contentDisposition,
            @Context HttpServletRequest request) {
        
        ResponseVm response = null;
        Gson gson = new Gson();
        
//        response = new ResponseVm();
//        response.setSuccessMessage("Successfully submitted file update for " + fileName + ".");
//        response.setResponseUrl("http://github.com");
//        return gson.toJson(response);
        
        if (StringUtils.isEmpty(repoName)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if (StringUtils.isEmpty(fileName)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if (StringUtils.isEmpty(commitMessage)) {
            response = new ResponseVm();
            response.setErrorMessage("You must enter a change description.");
        }
        if (file == null) {
            response = new ResponseVm();
            response.setErrorMessage("You must select a file.");
        }
        
        if (response != null) {
            return gson.toJson(response);
        }
        
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024 * 80);
            IOUtils.copy(file, outputStream);
            response = gitHubDao.submitFile(repoName, fileName, contentDisposition.getFileName(), outputStream.toByteArray(), commitMessage);
        }
        catch (NotFoundException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            response = new ResponseVm();
            response.setErrorMessage("Could not find repository " + repoName + ".");
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to submit file: {0}", e.getMessage());
            response = new ResponseVm();
            response.setErrorMessage("There was an error submitting your file. Please try again later.");
        }
        
        return gson.toJson(response);
    }

    @POST
    @Path("/{repoName}/{fileName}/issue")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public String submitIssue(
            @PathParam("repoName") String repoName, 
            @PathParam("fileName") String fileName,
            @FormDataParam("battleScribeVersion") String battleScribeVersion,
            @FormDataParam("platform") String platform,
            @FormDataParam("usingDropbox") String usingDropbox,
            @FormDataParam("issueBody") String issueBody,
            @Context HttpServletRequest request) {
        
        Gson gson = new Gson();
        
        if (StringUtils.isEmpty(repoName)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if (StringUtils.isEmpty(fileName)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        ResponseVm response = new ResponseVm();
        if (StringUtils.isEmpty(battleScribeVersion)) {
            response.setErrorMessage("You must enter the version of BattleScribe you are using.");
        }
        if (StringUtils.isEmpty(platform)) {
            response.setErrorMessage("You must select the platform you use BattleScribe on.");
        }
        if (StringUtils.isEmpty(usingDropbox)) {
            response.setErrorMessage("You must select whether or not you are using Dropbox.");
        }
        if (StringUtils.isEmpty(issueBody)) {
            response.setErrorMessage("You must enter a bug description.");
        }
        
        if (!StringUtils.isEmpty(response.getErrorMessage())) {
            return gson.toJson(response);
        }
        
        try {
            response = gitHubDao.createIssue(repoName, fileName, battleScribeVersion, platform, Boolean.parseBoolean(usingDropbox), issueBody);
        }
        catch (NotFoundException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            response = new ResponseVm();
            response.setErrorMessage("Could not find repository " + repoName + ".");
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to submit issue: {0}", e.getMessage());
            response = new ResponseVm();
            response.setErrorMessage("There was an error submitting your bug report. Please try again later.");
        }
        
        return gson.toJson(response);
    }

    @GET
    @Path("/prime")
    @Produces(MediaType.TEXT_PLAIN)
    public String primeRepositoryCache(
            @Context HttpServletRequest request) {
        
        try {
            gitHubDao.primeCache(getBaseUrl(request));
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to prime repo cache: {0}", e.getMessage());
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        return "Primed cache";
    }
}
