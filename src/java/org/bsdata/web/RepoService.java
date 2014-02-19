
package org.bsdata.web;

import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
import org.bsdata.viewmodel.RepositoryListVm;
import org.bsdata.utils.Utils;
import org.bsdata.viewmodel.RepositoryVm;


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
        return request.getRequestURL().toString().replace(request.getPathInfo(), "") 
                + "/" + WebConstants.REPO_SERVICE_PATH;
    }

    @GET
    @Path("/{repoName}/{fileName}")
    @Produces("application/octet-stream")
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
        
        HashMap<String, byte[]> repoData;
        try {
            repoData = dao.getRepoFileData(repoName, getBaseUrl(request), null);
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load repo data: {0}", e.getMessage());
            e.printStackTrace();
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        byte[] fileData = repoData.get(fileName);
        if (fileData == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        return Response.ok()
                .header("Content-Disposition", "attachment;filename=\"" + fileName + "\"")
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
            repositoryVm = dao.getRepoFiles(repoName,getBaseUrl(request));
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load repo file list: {0}", e.getMessage());
            e.printStackTrace();
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        return gson.toJson(repositoryVm);
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
            e.printStackTrace();
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        Gson gson = new Gson();
        return gson.toJson(repositoryList);
    }

    @GET
    @Path("/prime")
    @Produces(MediaType.TEXT_PLAIN)
    public String primeRepositoryCache(@Context HttpServletRequest request) {
        try {
            dao.primeCache(getBaseUrl(request));
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to prime repo cache: {0}", e.getMessage());
            e.printStackTrace();
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        return "Repo cache primed!";
    }

    @POST
    @Path("/{repoName}/{fileName}")
    @Consumes("application/octet-stream")
    public void submitFile(
            @PathParam("repoName") String repoName, 
            @PathParam("fileName") String fileName,
            @HeaderParam("commitMessage") String commitMessage,
            InputStream inputStream,
            @Context HttpServletRequest request) {
        
        if (StringUtils.isEmpty(repoName)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if (StringUtils.isEmpty(fileName)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if (StringUtils.isEmpty(commitMessage)) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        if (inputStream == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024 * 80);
            IOUtils.copy(inputStream, outputStream);
            dao.submitFile(repoName, fileName, outputStream.toByteArray(), commitMessage);
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to submit file: {0}", e.getMessage());
            e.printStackTrace();
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
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
//            e.printStackTrace();
//            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
//        }
//        
//        return "Submitted!";
//    }
}
