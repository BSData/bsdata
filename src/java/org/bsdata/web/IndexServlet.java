
package org.bsdata.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.bsdata.constants.DataConstants;
import org.bsdata.dao.GitHubDao;
import org.bsdata.repository.Indexer;
import org.bsdata.utils.Utils;


public class IndexServlet extends HttpServlet {
  
    private static final Logger logger = Logger.getLogger("org.bsdata");
    private GitHubDao dao;
    
    @Override
    public void init() throws ServletException {
        super.init();
        
        try {
            dao = new GitHubDao();
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Could not load GitHubDao: {0}", e.getMessage());
            throw new RuntimeException("Could not load GitHubDao: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get <repo name>/<file name> out of the path info
        String[] pathInfo = request.getPathInfo().replaceFirst("^/", "").split("/");
        if (pathInfo.length != 2) {
            // Wrong number of arguments
        }
        
        String repoName = pathInfo[0];
        String fileName = pathInfo[1];
        if (StringUtils.isEmpty(repoName)) {
            // No repo name
        }
        if (StringUtils.isEmpty(fileName)) {
            // No filename
        }
        
        ServletContext servletContext = getServletContext();
        HashMap<String, byte[]> repoData;
        if (servletContext.getAttribute(repoName) == null) {
            // We don't have the data for this repo cached
            HashMap<String, ByteArrayInputStream> repoFiles = dao.getRepoFiles(repoName);
            repoData = Indexer.getRepoFiles(repoName, request.getRequestURL().toString(), null, repoFiles);
            servletContext.setAttribute(repoName, repoData);
        }
        else {
            repoData = (HashMap<String, byte[]>)servletContext.getAttribute(repoName);
        }
        
        if (Utils.isRosterPath(fileName)) {
            response.setContentType(DataConstants.ROSTER_FILE_MIME_TYPE);
        }
        else if (Utils.isCataloguePath(fileName)) {
            response.setContentType(DataConstants.CATALOGUE_FILE_MIME_TYPE);
        }
        else if (Utils.isGameSytstemPath(fileName)) {
            response.setContentType(DataConstants.GAME_SYSTEM_FILE_MIME_TYPE);
        }
        else if (Utils.isIndexPath(fileName)) {
            response.setContentType(DataConstants.INDEX_FILE_MIME_TYPE);
        }
        response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\""); 
        
        byte[] fileData = repoData.get(fileName);
        response.setContentLength(fileData.length);
        response.getOutputStream().write(fileData);
        response.flushBuffer();
    }
    
}
