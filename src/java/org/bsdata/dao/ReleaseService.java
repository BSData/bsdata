/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bsdata.dao;

import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.util.List;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.IGitHubConstants;
import org.eclipse.egit.github.core.client.PagedRequest;
import org.eclipse.egit.github.core.service.GitHubService;

/**
 *
 * @author Jonskichov
 */
public class ReleaseService extends GitHubService {
    
    private static final String SEGMENT_RELEASES = "/releases";
    
    
    public ReleaseService(GitHubClient client) {
        super(client);
    }
    
    /**
     * Get all releases in given repository
     *
     * @param repository
     * @return list of releases
     * @throws IOException
     */
    public List<Release> getAllReleases(IRepositoryIdProvider repository) throws IOException {
        return getReleases(repository, -1, -1);
    }
    
    /**
     * Get releases in given repository for a given start page and number of pages
     *
     * @param repository
     * @return list of releases
     * @throws IOException
     */
    public List<Release> getReleases(IRepositoryIdProvider repository, int start, int size) throws IOException {
        String id = getId(repository);
        StringBuilder uri = new StringBuilder(IGitHubConstants.SEGMENT_REPOS);
        uri.append('/').append(id);
        uri.append(SEGMENT_RELEASES);
        PagedRequest<Release> request;
        if (start == -1 || size == -1) {
            request = createPagedRequest();
        }
        else {
            request = createPagedRequest(start, size);
        }
        request.setUri(uri);
        request.setType(new TypeToken<List<Release>>() {}.getType());
        return getAll(request);
    }
}
