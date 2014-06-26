
package org.bsdata.web;

import com.google.gson.Gson;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bsdata.constants.DataConstants;
import org.bsdata.constants.WebConstants;
import org.bsdata.dao.GitHubDao;
import org.bsdata.model.DataFile;
import org.bsdata.viewmodel.RepositoryListVm;
import org.bsdata.utils.Utils;
import org.bsdata.viewmodel.RepositoryVm;
import org.bsdata.viewmodel.ResponseVm;


/**
 * Web service class that handles 
 * - Calls from the browser for the front end (list repos etc)
 * - File submissions
 * - Serving the data files themselves
 * 
 * @author Jonskichov
 */
@Path(WebConstants.REPO_SERVICE_PATH)
public class RepoService {
  
    private static final Logger logger = Logger.getLogger("org.bsdata");
    private GitHubDao dao;

    /**
     * Creates a new instance of RepoService
     */
    public RepoService() {
        try {
            dao = new GitHubDao();
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Could not load GitHubDao: {0}", e.getMessage());
            throw new RuntimeException("Could not load GitHubDao: " + e.getMessage());
        }
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
        
        HashMap<String, DataFile> repoData;
        try {
            repoData = dao.getRepoFileData(repoName, getBaseUrl(request));
        }
        catch (NotFoundException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load repo data: {0}", e.getMessage());
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        byte[] fileData = repoData.get(fileName).getData();
        if (fileData == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        return Response
                .ok()
                .entity(fileData)
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
            repositoryVm = dao.getRepoFiles(repoName, getBaseUrl(request));
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
            Feed feed = dao.getReleaseFeed(repoName, getBaseUrl(request));
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
        RepositoryListVm repositoryList;
        try {
            repositoryList = dao.getRepos(getBaseUrl(request));
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
            response = dao.submitFile(repoName, fileName, contentDisposition.getFileName(), outputStream.toByteArray(), commitMessage);
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
            @FormDataParam("issueBody") String issueBody,
            @Context HttpServletRequest request) {
        
        ResponseVm response;
        Gson gson = new Gson();
        
//        response = new ResponseVm();
//        response.setErrorMessage("There was an error submitting your bug report. Please try again later.");
//        return gson.toJson(response);
        
        if (StringUtils.isEmpty(repoName)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if (StringUtils.isEmpty(fileName)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if (StringUtils.isEmpty(issueBody)) {
            response = new ResponseVm();
            response.setErrorMessage("You must enter a bug description.");
            return gson.toJson(response);
        }
        
        try {
            response = dao.createIssue(repoName, fileName, issueBody);
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
    @Path("/{repoName}/prime")
    @Produces(MediaType.TEXT_PLAIN)
    public String primeRepositoryCache(
            @PathParam("repoName") String repoName, 
            @Context HttpServletRequest request) {
        try {
            dao.primeCache(getBaseUrl(request), repoName);
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to prime repo cache: {0}", e.getMessage());
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        return "Repo cache for " + repoName + " primed!";
    }
    
    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String submit(@Context HttpServletRequest request) {
        try {
            // Do something...
            dao.getRepos(getBaseUrl(request));
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load repo list: {0}", e.getMessage());
        }
        return "Pong!";
    }
    
//    @Context ServletContext context;
//    @GET
//    @Path("/submit")
//    @Produces(MediaType.TEXT_PLAIN)
//    public String submit(@Context HttpServletRequest request) {
//        
//        try {
//            dao.createIssue("wh40k", "Issue Title Test", "Issue body test");
//        }
//        catch (Exception e) {
//            logger.log(Level.SEVERE, "Failed to submit file: {0}", e.getMessage());
//            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
//        }
//        
//        return "Submitted!";
//    }
}
