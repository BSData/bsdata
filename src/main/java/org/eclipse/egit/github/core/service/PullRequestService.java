/*******************************************************************************
 *  Copyright (c) 2011 GitHub Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *    Kevin Sawicki (GitHub Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.egit.github.core.service;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_COMMENTS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_COMMITS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_FILES;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_MERGE;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_PULLS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;
import static org.eclipse.egit.github.core.client.PagedRequest.PAGE_FIRST;
import static org.eclipse.egit.github.core.client.PagedRequest.PAGE_SIZE;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.MergeStatus;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.PullRequestMarker;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.client.PagedRequest;

/**
 * Service class for creating, updating, getting, and listing pull requests as
 * well as getting the commits associated with a pull request and the files
 * modified by a pull request.
 *
 * @see <a href="http://developer.github.com/v3/pulls">GitHub Pull Requests API
 *      documentation</a>
 * @see <a href="http://developer.github.com/v3/pulls/comments">GitHub Pull
 *      Request comments API documentation</a>
 */
public class PullRequestService extends GitHubService {

	/**
	 * PR_TITLE
	 */
	public static final String PR_TITLE = "title"; //$NON-NLS-1$

	/**
	 * PR_BODY
	 */
	public static final String PR_BODY = "body"; //$NON-NLS-1$

	/**
	 * PR_BASE
	 */
	public static final String PR_BASE = "base"; //$NON-NLS-1$

	/**
	 * PR_HEAD
	 */
	public static final String PR_HEAD = "head"; //$NON-NLS-1$

	/**
	 * PR_STATE
	 */
	public static final String PR_STATE = "state"; //$NON-NLS-1$

	/**
	 * Create pull request service
	 */
	public PullRequestService() {
		super();
	}

	/**
	 * Create pull request service
	 *
	 * @param client
	 */
	public PullRequestService(GitHubClient client) {
		super(client);
	}

	/**
	 * Create request for single pull request
	 *
	 * @param repository
	 * @param id
	 * @return request
	 * @throws IOException
	 */
	public PullRequest getPullRequest(IRepositoryIdProvider repository, int id)
			throws IOException {
		final String repoId = getId(repository);
		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(repoId);
		uri.append(SEGMENT_PULLS);
		uri.append('/').append(id);
		GitHubRequest request = createRequest();
		request.setUri(uri);
		request.setType(PullRequest.class);
		return (PullRequest) client.get(request).getBody();
	}

	/**
	 * Create paged request for fetching pull requests
	 *
	 * @param provider
	 * @param state
	 * @param start
	 * @param size
	 * @return paged request
	 */
	protected PagedRequest<PullRequest> createPullsRequest(
			IRepositoryIdProvider provider, String state, int start, int size) {
		final String id = getId(provider);

		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(id);
		uri.append(SEGMENT_PULLS);
		PagedRequest<PullRequest> request = createPagedRequest(start, size);
		request.setUri(uri);
		if (state != null)
			request.setParams(Collections.singletonMap(
					IssueService.FILTER_STATE, state));
		request.setType(new TypeToken<List<PullRequest>>() {
			// make protected type visible
		}.getType());
		return request;
	}

	/**
	 * Get pull requests from repository matching state
	 *
	 * @param repository
	 * @param state
	 * @return list of pull requests
	 * @throws IOException
	 */
	public List<PullRequest> getPullRequests(IRepositoryIdProvider repository,
			String state) throws IOException {
		return getAll(pagePullRequests(repository, state));
	}

	/**
	 * Page pull requests with given state
	 *
	 * @param repository
	 * @param state
	 * @return iterator over pages of pull requests
	 */
	public PageIterator<PullRequest> pagePullRequests(
			IRepositoryIdProvider repository, String state) {
		return pagePullRequests(repository, state, PAGE_SIZE);
	}

	/**
	 * Page pull requests with given state
	 *
	 * @param repository
	 * @param state
	 * @param size
	 * @return iterator over pages of pull requests
	 */
	public PageIterator<PullRequest> pagePullRequests(
			IRepositoryIdProvider repository, String state, int size) {
		return pagePullRequests(repository, state, PAGE_FIRST, size);
	}

	/**
	 * Page pull requests with given state
	 *
	 * @param repository
	 * @param state
	 * @param start
	 * @param size
	 * @return iterator over pages of pull requests
	 */
	public PageIterator<PullRequest> pagePullRequests(
			IRepositoryIdProvider repository, String state, int start, int size) {
		PagedRequest<PullRequest> request = createPullsRequest(repository,
				state, start, size);
		return createPageIterator(request);
	}

	private Map<String, String> createPrMap(PullRequest request) {
		Map<String, String> params = new HashMap<>();
		if (request != null) {
			String title = request.getTitle();
			if (title != null)
				params.put(PR_TITLE, title);
			String body = request.getBody();
			if (body != null)
				params.put(PR_BODY, body);
			PullRequestMarker baseMarker = request.getBase();
			if (baseMarker != null) {
				String base = baseMarker.getLabel();
				if (base != null)
					params.put(PR_BASE, base);
			}
			PullRequestMarker headMarker = request.getHead();
			if (headMarker != null) {
				String head = headMarker.getLabel();
				if (head != null)
					params.put(PR_HEAD, head);
			}
		}
		return params;
	}

	private Map<String, String> editPrMap(PullRequest request) {
		Map<String, String> params = new HashMap<>();
		String title = request.getTitle();
		if (title != null)
			params.put(PR_TITLE, title);
		String body = request.getBody();
		if (body != null)
			params.put(PR_BODY, body);
		String state = request.getState();
		if (state != null)
			params.put(PR_STATE, state);
		return params;
	}

	/**
	 * Create pull request
	 *
	 * @param repository
	 * @param request
	 * @return created pull request
	 * @throws IOException
	 */
	public PullRequest createPullRequest(IRepositoryIdProvider repository,
			PullRequest request) throws IOException {
		String id = getId(repository);

		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(id);
		uri.append(SEGMENT_PULLS);
		Map<String, String> params = createPrMap(request);
		return client.post(uri.toString(), params, PullRequest.class);
	}

	/**
	 * Create pull request by attaching branch information to an existing issue
	 *
	 * @param repository
	 * @param issueId
	 * @param head
	 * @param base
	 * @return created pull request
	 * @throws IOException
	 */
	public PullRequest createPullRequest(IRepositoryIdProvider repository,
			int issueId, String head, String base) throws IOException {
		String id = getId(repository);
		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(id);
		uri.append(SEGMENT_PULLS);
		Map<String, Object> params = new HashMap<>();
		params.put("issue", Integer.valueOf(issueId)); //$NON-NLS-1$
		params.put("head", head); //$NON-NLS-1$
		params.put("base", base); //$NON-NLS-1$
		return client.post(uri.toString(), params, PullRequest.class);
	}

	/**
	 * Edit pull request
	 *
	 * @param repository
	 * @param request
	 * @return edited pull request
	 * @throws IOException
	 */
	public PullRequest editPullRequest(IRepositoryIdProvider repository,
			PullRequest request) throws IOException {
		String id = getId(repository);
		if (request == null)
			throw new IllegalArgumentException("Request cannot be null"); //$NON-NLS-1$

		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(id);
		uri.append(SEGMENT_PULLS);
		uri.append('/').append(request.getNumber());
		Map<String, String> params = editPrMap(request);
		return client.post(uri.toString(), params, PullRequest.class);
	}

	/**
	 * Get all commits associated with given pull request id
	 *
	 * @param repository
	 * @param id
	 * @return list of commits
	 * @throws IOException
	 */
	public List<RepositoryCommit> getCommits(IRepositoryIdProvider repository,
			int id) throws IOException {
		final String repoId = getId(repository);
		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(repoId);
		uri.append(SEGMENT_PULLS);
		uri.append('/').append(id);
		uri.append(SEGMENT_COMMITS);
		PagedRequest<RepositoryCommit> request = createPagedRequest();
		request.setUri(uri);
		request.setType(new TypeToken<List<RepositoryCommit>>() {
			// make protected type visible
		}.getType());
		return getAll(request);
	}

	/**
	 * Get all changed files associated with given pull request id
	 *
	 * @param repository
	 * @param id
	 * @return list of commit files
	 * @throws IOException
	 */
	public List<CommitFile> getFiles(IRepositoryIdProvider repository, int id)
			throws IOException {
		final String repoId = getId(repository);
		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(repoId);
		uri.append(SEGMENT_PULLS);
		uri.append('/').append(id);
		uri.append(SEGMENT_FILES);
		PagedRequest<CommitFile> request = createPagedRequest();
		request.setUri(uri);
		request.setType(new TypeToken<List<CommitFile>>() {
			// make protected type visible
		}.getType());
		return getAll(request);
	}

	/**
	 * Is the given pull request id merged?
	 *
	 * @param repository
	 * @param id
	 * @return true if merge, false otherwise
	 * @throws IOException
	 */
	public boolean isMerged(IRepositoryIdProvider repository, int id)
			throws IOException {
		String repoId = getId(repository);
		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(repoId);
		uri.append(SEGMENT_PULLS);
		uri.append('/').append(id);
		uri.append(SEGMENT_MERGE);
		return check(uri.toString());
	}

	/**
	 * Merge given pull request
	 *
	 * @param repository
	 * @param id
	 * @param commitMessage
	 * @return status of merge
	 * @throws IOException
	 */
	public MergeStatus merge(IRepositoryIdProvider repository, int id,
			String commitMessage) throws IOException {
		String repoId = getId(repository);
		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(repoId);
		uri.append(SEGMENT_PULLS);
		uri.append('/').append(id);
		uri.append(SEGMENT_MERGE);
		return client.put(uri.toString(),
				Collections.singletonMap("commit_message", commitMessage), //$NON-NLS-1$
				MergeStatus.class);
	}

	/**
	 * Get all comments on commits in given pull request
	 *
	 * @param repository
	 * @param id
	 * @return non-null list of comments
	 * @throws IOException
	 */
	public List<CommitComment> getComments(IRepositoryIdProvider repository,
			int id) throws IOException {
		return getAll(pageComments(repository, id));
	}

	/**
	 * Page pull request commit comments
	 *
	 * @param repository
	 * @param id
	 * @return iterator over pages of commit comments
	 */
	public PageIterator<CommitComment> pageComments(
			IRepositoryIdProvider repository, int id) {
		return pageComments(repository, id, PAGE_SIZE);
	}

	/**
	 * Page pull request commit comments
	 *
	 * @param repository
	 * @param id
	 * @param size
	 * @return iterator over pages of commit comments
	 */
	public PageIterator<CommitComment> pageComments(
			IRepositoryIdProvider repository, int id, int size) {
		return pageComments(repository, id, PAGE_FIRST, size);
	}

	/**
	 * Page pull request commit comments
	 *
	 * @param repository
	 * @param id
	 * @param start
	 * @param size
	 * @return iterator over pages of commit comments
	 */
	public PageIterator<CommitComment> pageComments(
			IRepositoryIdProvider repository, int id, int start, int size) {
		String repoId = getId(repository);
		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(repoId);
		uri.append(SEGMENT_PULLS);
		uri.append('/').append(id);
		uri.append(SEGMENT_COMMENTS);
		PagedRequest<CommitComment> request = createPagedRequest(start, size);
		request.setUri(uri);
		request.setType(new TypeToken<List<CommitComment>>() {
			// make protected type visible
		}.getType());
		return createPageIterator(request);
	}

	/**
	 * Get commit comment with given id
	 *
	 * @param repository
	 * @param commentId
	 * @return commit comment
	 * @throws IOException
	 */
	public CommitComment getComment(IRepositoryIdProvider repository,
			long commentId) throws IOException {
		String repoId = getId(repository);
		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(repoId);
		uri.append(SEGMENT_PULLS);
		uri.append(SEGMENT_COMMENTS);
		uri.append('/').append(commentId);
		GitHubRequest request = createRequest();
		request.setUri(uri);
		request.setType(CommitComment.class);
		return (CommitComment) client.get(request).getBody();
	}

	/**
	 * Create comment on given pull request
	 *
	 * @param repository
	 * @param id
	 * @param comment
	 * @return created commit comment
	 * @throws IOException
	 */
	public CommitComment createComment(IRepositoryIdProvider repository,
			int id, CommitComment comment) throws IOException {
		String repoId = getId(repository);

		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(repoId);
		uri.append(SEGMENT_PULLS);
		uri.append('/').append(id);
		uri.append(SEGMENT_COMMENTS);
		return client.post(uri.toString(), comment, CommitComment.class);
	}

	/**
	 * Reply to given comment
	 *
	 * @param repository
	 * @param pullRequestId
	 * @param commentId
	 * @param body
	 * @return created comment
	 * @throws IOException
	 */
	public CommitComment replyToComment(IRepositoryIdProvider repository,
			int pullRequestId, int commentId, String body) throws IOException {
		String repoId = getId(repository);
		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(repoId);
		uri.append(SEGMENT_PULLS);
		uri.append('/').append(pullRequestId);
		uri.append(SEGMENT_COMMENTS);
		Map<String, String> params = new HashMap<>();
		params.put("in_reply_to", Integer.toString(commentId)); //$NON-NLS-1$
		params.put("body", body); //$NON-NLS-1$
		return client.post(uri.toString(), params, CommitComment.class);
	}

	/**
	 * Edit pull request comment
	 *
	 * @param repository
	 * @param comment
	 * @return edited comment
	 * @throws IOException
	 */
	public CommitComment editComment(IRepositoryIdProvider repository,
			CommitComment comment) throws IOException {
		String repoId = getId(repository);
		if (comment == null)
			throw new IllegalArgumentException("Comment cannot be null"); //$NON-NLS-1$

		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(repoId);
		uri.append(SEGMENT_PULLS);
		uri.append(SEGMENT_COMMENTS);
		uri.append('/').append(comment.getId());
		return client.post(uri.toString(), comment, CommitComment.class);
	}

	/**
	 * Delete commit comment with given id
	 *
	 * @param repository
	 * @param commentId
	 * @throws IOException
	 */
	public void deleteComment(IRepositoryIdProvider repository, long commentId)
			throws IOException {
		String repoId = getId(repository);
		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(repoId);
		uri.append(SEGMENT_PULLS);
		uri.append(SEGMENT_COMMENTS);
		uri.append('/').append(commentId);
		client.delete(uri.toString());
	}
}
