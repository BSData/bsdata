/******************************************************************************
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
 *****************************************************************************/
package org.eclipse.egit.github.core.service;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_COMMENTS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_COMMITS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_COMPARE;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_STATUSES;
import static org.eclipse.egit.github.core.client.PagedRequest.PAGE_FIRST;
import static org.eclipse.egit.github.core.client.PagedRequest.PAGE_SIZE;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.CommitStatus;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryCommitCompare;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.client.PagedRequest;

/**
 * Service for interacting with repository commits
 *
 * @see <a href="http://developer.github.com/v3/repos/commits">GitHub commit API
 *      documentation</a>
 */
public class CommitService extends GitHubService {

	/**
	 * Create commit service
	 */
	public CommitService() {
		super();
	}

	/**
	 * Create commit service
	 *
	 * @param client
	 */
	public CommitService(GitHubClient client) {
		super(client);
	}

	/**
	 * Get all commits in given repository
	 *
	 * @param repository
	 * @return non-null but possibly empty list of repository commits
	 * @throws IOException
	 */
	public List<RepositoryCommit> getCommits(IRepositoryIdProvider repository)
			throws IOException {
		return getCommits(repository, null, null);
	}

	/**
	 * Get all commits in given repository beginning at an optional commit SHA-1
	 * and affecting an optional path.
	 *
	 * @param repository
	 * @param sha
	 * @param path
	 * @return non-null but possibly empty list of repository commits
	 * @throws IOException
	 */
	public List<RepositoryCommit> getCommits(IRepositoryIdProvider repository,
			String sha, String path) throws IOException {
		return getAll(pageCommits(repository, sha, path));
	}

	/**
	 * Page commits in given repository
	 *
	 * @param repository
	 * @return page iterator
	 */
	public PageIterator<RepositoryCommit> pageCommits(
			IRepositoryIdProvider repository) {
		return pageCommits(repository, null, null);
	}

	/**
	 * Page commits in given repository
	 *
	 * @param repository
	 * @param size
	 * @return page iterator
	 */
	public PageIterator<RepositoryCommit> pageCommits(
			IRepositoryIdProvider repository, int size) {
		return pageCommits(repository, null, null, size);
	}

	/**
	 * Page commits in given repository
	 *
	 * @param repository
	 * @param sha
	 * @param path
	 * @return page iterator
	 */
	public PageIterator<RepositoryCommit> pageCommits(
			IRepositoryIdProvider repository, String sha, String path) {
		return pageCommits(repository, sha, path, PAGE_SIZE);
	}

	/**
	 * Page commits in given repository
	 *
	 * @param repository
	 * @param sha
	 * @param path
	 * @param size
	 * @return page iterator
	 */
	public PageIterator<RepositoryCommit> pageCommits(
			IRepositoryIdProvider repository, String sha, String path, int size) {
		String id = getId(repository);
		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(id);
		uri.append(SEGMENT_COMMITS);
		PagedRequest<RepositoryCommit> request = createPagedRequest(PAGE_FIRST,
				size);
		request.setUri(uri);
		request.setType(new TypeToken<List<RepositoryCommit>>() {
			// make protected type visible
		}.getType());

		if (sha != null || path != null) {
			Map<String, String> params = new HashMap<>();
			if (sha != null)
				params.put("sha", sha); //$NON-NLS-1$
			if (path != null)
				params.put("path", path); //$NON-NLS-1$
			request.setParams(params);
		}

		return createPageIterator(request);
	}

	/**
	 * Get commit with given SHA-1 from given repository
	 *
	 * @param repository
	 * @param sha
	 * @return repository commit
	 * @throws IOException
	 */
	public RepositoryCommit getCommit(IRepositoryIdProvider repository,
			String sha) throws IOException {
		GitHubRequest request = getCommitRequest(repository, sha);
		request.setType(RepositoryCommit.class);
		return (RepositoryCommit) client.get(request).getBody();
	}

	/**
	 * Get diff for commit with given SHA-1 from given repository. It is the
	 * responsibility of the calling method to close the returned stream.
	 *
	 * @param repository
	 * @param sha
	 * @return diff stream
	 * @throws IOException
	 */
	public InputStream getCommitDiff(IRepositoryIdProvider repository,
			String sha) throws IOException {
		GitHubRequest request = getCommitRequest(repository, sha);
		request.setResponseContentType(ACCEPT_DIFF);
		return client.getStream(request);
	}

	/**
	 * Get patch for commit with given SHA-1 from given repository. It is the
	 * responsibility of the calling method to close the returned stream.
	 *
	 * @param repository
	 * @param sha
	 * @return patch stream
	 * @throws IOException
	 */
	public InputStream getCommitPatch(IRepositoryIdProvider repository,
			String sha) throws IOException {
		GitHubRequest request = getCommitRequest(repository, sha);
		request.setResponseContentType(ACCEPT_PATCH);
		return client.getStream(request);
	}

	private GitHubRequest getCommitRequest(IRepositoryIdProvider repository,
			String sha) {
		String id = getId(repository);
		if (sha == null) {
			throw new IllegalArgumentException("Sha cannot be null"); //$NON-NLS-1$
		} else if (sha.length() == 0) {
			throw new IllegalArgumentException("Sha cannot be empty"); //$NON-NLS-1$
		}

		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(id);
		uri.append(SEGMENT_COMMITS);
		uri.append('/').append(sha);
		return createRequest().setUri(uri);
	}

	/**
	 * Get all comments on commit with given SHA-1
	 *
	 * @param repository
	 * @param sha
	 * @return non-null but possibly empty list of commits
	 * @throws IOException
	 */
	public List<CommitComment> getComments(IRepositoryIdProvider repository,
			String sha) throws IOException {
		return getAll(pageComments(repository, sha));
	}

	/**
	 * Page comments on commit with given SHA-1
	 *
	 * @param repository
	 * @param sha
	 * @return page iterator over comments
	 */
	public PageIterator<CommitComment> pageComments(
			IRepositoryIdProvider repository, String sha) {
		return pageComments(repository, sha, PAGE_SIZE);
	}

	/**
	 * Page comments on commit with given SHA-1
	 *
	 * @param repository
	 * @param sha
	 * @param size
	 * @return page iterator over comments
	 */
	public PageIterator<CommitComment> pageComments(
			IRepositoryIdProvider repository, String sha, int size) {
		return pageComments(repository, sha, PAGE_FIRST, size);
	}

	/**
	 * Page comments on commit with given SHA-1
	 *
	 * @param repository
	 * @param sha
	 * @param start
	 * @param size
	 * @return page iterator over comments
	 */
	public PageIterator<CommitComment> pageComments(
			IRepositoryIdProvider repository, String sha, int start, int size) {
		String id = getId(repository);
		if (sha == null)
			throw new IllegalArgumentException("Sha cannot be null"); //$NON-NLS-1$
		if (sha.length() == 0)
			throw new IllegalArgumentException("Sha cannot be empty"); //$NON-NLS-1$

		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(id);
		uri.append(SEGMENT_COMMITS);
		uri.append('/').append(sha);
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
		uri.append(SEGMENT_COMMENTS);
		uri.append('/').append(commentId);
		GitHubRequest request = createRequest();
		request.setUri(uri);
		request.setType(CommitComment.class);
		return (CommitComment) client.get(request).getBody();
	}

	/**
	 * Add comment to given commit
	 *
	 * @param repository
	 * @param sha
	 * @param comment
	 * @return created comment
	 * @throws IOException
	 */
	public CommitComment addComment(IRepositoryIdProvider repository,
			String sha, CommitComment comment) throws IOException {
		String id = getId(repository);
		if (sha == null)
			throw new IllegalArgumentException("Sha cannot be null"); //$NON-NLS-1$
		if (sha.length() == 0)
			throw new IllegalArgumentException("Sha cannot be empty"); //$NON-NLS-1$

		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(id);
		uri.append(SEGMENT_COMMITS);
		uri.append('/').append(sha);
		uri.append(SEGMENT_COMMENTS);
		return client.post(uri.toString(), comment, CommitComment.class);
	}

	/**
	 * Edit given comment
	 *
	 * @param repository
	 * @param comment
	 * @return edited comment
	 * @throws IOException
	 */
	public CommitComment editComment(IRepositoryIdProvider repository,
			CommitComment comment) throws IOException {
		String id = getId(repository);
		if (comment == null)
			throw new IllegalArgumentException("Comment cannot be null"); //$NON-NLS-1$

		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(id);
		uri.append(SEGMENT_COMMENTS);
		uri.append('/').append(comment.getId());
		return client.post(uri.toString(), comment, CommitComment.class);
	}

	/**
	 * Delete commit comment with given id from given repository
	 *
	 * @param repository
	 * @param commentId
	 * @throws IOException
	 */
	public void deleteComment(IRepositoryIdProvider repository, long commentId)
			throws IOException {
		String id = getId(repository);
		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(id);
		uri.append(SEGMENT_COMMENTS);
		uri.append('/').append(commentId);
		client.delete(uri.toString());
	}

	/**
	 * Compare base and head commits
	 *
	 * @param repository
	 * @param base
	 * @param head
	 * @return commit compare
	 * @throws IOException
	 */
	public RepositoryCommitCompare compare(IRepositoryIdProvider repository,
			String base, String head) throws IOException {
		GitHubRequest request = getCompareRequest(repository, base, head);
		request.setType(RepositoryCommitCompare.class);
		return (RepositoryCommitCompare) client.get(request).getBody();
	}

	/**
	 * Get diff between base and head commits. It is the responsibility of the
	 * calling method to close the returned stream.
	 *
	 * @param repository
	 * @param base
	 * @param head
	 * @return diff stream
	 * @throws IOException
	 */
	public InputStream compareDiff(IRepositoryIdProvider repository,
			String base, String head) throws IOException {
		GitHubRequest request = getCompareRequest(repository, base, head);
		request.setResponseContentType(ACCEPT_DIFF);
		return client.getStream(request);
	}

	/**
	 * Get patch between base and head commits. It is the responsibility of the
	 * calling method to close the returned stream.
	 *
	 * @param repository
	 * @param base
	 * @param head
	 * @return patch stream
	 * @throws IOException
	 */
	public InputStream comparePatch(IRepositoryIdProvider repository,
			String base, String head) throws IOException {
		GitHubRequest request = getCompareRequest(repository, base, head);
		request.setResponseContentType(ACCEPT_PATCH);
		return client.getStream(request);
	}

	private GitHubRequest getCompareRequest(IRepositoryIdProvider repository,
			String base, String head) {
		String id = getId(repository);
		if (base == null) {
			throw new IllegalArgumentException("Base cannot be null"); //$NON-NLS-1$
		} else if (base.length() == 0) {
			throw new IllegalArgumentException("Base cannot be empty"); //$NON-NLS-1$
		}
		if (head == null) {
			throw new IllegalArgumentException("Head cannot be null"); //$NON-NLS-1$
		} else if (head.length() == 0) {
			throw new IllegalArgumentException("Head cannot be empty"); //$NON-NLS-1$
		}

		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(id);
		uri.append(SEGMENT_COMPARE);
		uri.append('/').append(base).append("...").append(head); //$NON-NLS-1$
		return createRequest().setUri(uri);
	}

	/**
	 * Get statuses for commit SHA-1
	 *
	 * @param repository
	 * @param sha
	 * @return list of statuses
	 * @throws IOException
	 */
	public List<CommitStatus> getStatuses(IRepositoryIdProvider repository,
			String sha) throws IOException {
		String id = getId(repository);
		if (sha == null)
			throw new IllegalArgumentException("SHA-1 cannot be null"); //$NON-NLS-1$
		if (sha.length() == 0)
			throw new IllegalArgumentException("SHA-1 cannot be empty"); //$NON-NLS-1$

		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(id);
		uri.append(SEGMENT_STATUSES);
		uri.append('/').append(sha);
		PagedRequest<CommitStatus> request = createPagedRequest();
		request.setType(new TypeToken<List<CommitStatus>>() {
			// make protected type visible
		}.getType());
		request.setUri(uri);
		return getAll(request);
	}

	/**
	 * Create status for commit SHA-1
	 *
	 * @param repository
	 * @param sha
	 * @param status
	 * @return created status
	 * @throws IOException
	 */
	public CommitStatus createStatus(IRepositoryIdProvider repository,
			String sha, CommitStatus status) throws IOException {
		String id = getId(repository);
		if (sha == null)
			throw new IllegalArgumentException("SHA-1 cannot be null"); //$NON-NLS-1$
		if (sha.length() == 0)
			throw new IllegalArgumentException("SHA-1 cannot be empty"); //$NON-NLS-1$
		if (status == null)
			throw new IllegalArgumentException("Status cannot be null"); //$NON-NLS-1$

		Map<String, String> params = new HashMap<>(3, 1);
		if (status.getState() != null)
			params.put("state", status.getState()); //$NON-NLS-1$
		if (status.getTargetUrl() != null)
			params.put("target_url", status.getTargetUrl()); //$NON-NLS-1$
		if (status.getDescription() != null)
			params.put("description", status.getDescription()); //$NON-NLS-1$
		if (status.getContext() != null)
			params.put("context", status.getContext()); //$NON-NLS-1$

		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(id);
		uri.append(SEGMENT_STATUSES);
		uri.append('/').append(sha);

		return client.post(uri.toString(), params, CommitStatus.class);
	}

	/**
	 * Get all comments on all commits in the given repository
	 *
	 * @param repository
	 * @return non-null but possibly empty list of commits
	 * @throws IOException
	 */
	public List<CommitComment> getComments(IRepositoryIdProvider repository)
			throws IOException {
		return getAll(pageComments(repository));
	}

	/**
	 * Page all comments on all commits in the given repository
	 *
	 * @param repository
	 * @return page iterator over comments
	 */
	public PageIterator<CommitComment> pageComments(
			IRepositoryIdProvider repository) {
		return pageComments(repository, PAGE_SIZE);
	}

	/**
	 * Page all comments on all commits in the given repository
	 *
	 * @param repository
	 * @param size
	 * @return page iterator over comments
	 */
	public PageIterator<CommitComment> pageComments(
			IRepositoryIdProvider repository, int size) {
		return pageComments(repository, PAGE_FIRST, size);
	}

	/**
	 * Page all comments on all commits in the given repository
	 *
	 * @param repository
	 * @param start
	 * @param size
	 * @return page iterator over comments
	 */
	public PageIterator<CommitComment> pageComments(
			IRepositoryIdProvider repository, int start, int size) {
		String id = getId(repository);

		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(id);
		uri.append(SEGMENT_COMMENTS);
		PagedRequest<CommitComment> request = createPagedRequest(start, size);
		request.setUri(uri);
		request.setType(new TypeToken<List<CommitComment>>() {
			// make protected type visible
		}.getType());
		return createPageIterator(request);
	}
}
