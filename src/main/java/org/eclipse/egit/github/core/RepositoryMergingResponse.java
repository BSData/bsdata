/******************************************************************************
 *  Copyright (c) 2018 Frédéric Cilia
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *    Frédéric Cilia - initial API and implementation
 *****************************************************************************/
package org.eclipse.egit.github.core;

import java.io.Serializable;
import java.util.List;

/**
 * Repository merging response model class
 *
 * @since 5.3
 */
public class RepositoryMergingResponse implements Serializable {

	private static final long serialVersionUID = 3450081957091778831L;

	private String sha;

	private String nodeId;

	private Commit commit;

	private String url;

	private String htmlUrl;

	private String commentsUrl;

	private User author;

	private User committer;

	private List<Commit> parents;

	/**
	 * @return the sha
	 */
	public String getSha() {
		return sha;
	}

	/**
	 * @param sha
	 *            the sha to set
	 * @return this merge response
	 */
	public RepositoryMergingResponse setSha(String sha) {
		this.sha = sha;
		return this;
	}

	/**
	 * @return the nodeId
	 */
	public String getNodeId() {
		return nodeId;
	}

	/**
	 * @param nodeId
	 *            the nodeId to set
	 * @return this merge response
	 */
	public RepositoryMergingResponse setNodeId(String nodeId) {
		this.nodeId = nodeId;
		return this;
	}

	/**
	 * @return the commit
	 */
	public Commit getCommit() {
		return commit;
	}

	/**
	 * @param commit
	 *            the commit to set
	 * @return this merge response
	 */
	public RepositoryMergingResponse setCommit(Commit commit) {
		this.commit = commit;
		return this;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 * @return this merge response
	 */
	public RepositoryMergingResponse setUrl(String url) {
		this.url = url;
		return this;
	}

	/**
	 * @return the htmlUrl
	 */
	public String getHtmlUrl() {
		return htmlUrl;
	}

	/**
	 * @param htmlUrl
	 *            the htmlUrl to set
	 * @return this merge response
	 */
	public RepositoryMergingResponse setHtmlUrl(String htmlUrl) {
		this.htmlUrl = htmlUrl;
		return this;
	}

	/**
	 * @return the commentsUrl
	 */
	public String getCommentsUrl() {
		return commentsUrl;
	}

	/**
	 * @param commentsUrl
	 *            the commentsUrl to set
	 * @return this merge response
	 */
	public RepositoryMergingResponse setCommentsUrl(String commentsUrl) {
		this.commentsUrl = commentsUrl;
		return this;
	}

	/**
	 * @return the author
	 */
	public User getAuthor() {
		return author;
	}

	/**
	 * @param author
	 *            the author to set
	 * @return this merge response
	 */
	public RepositoryMergingResponse setAuthor(User author) {
		this.author = author;
		return this;
	}

	/**
	 * @return the committer
	 */
	public User getCommitter() {
		return committer;
	}

	/**
	 * @param committer
	 *            the committer to set
	 * @return this merge response
	 */
	public RepositoryMergingResponse setCommitter(User committer) {
		this.committer = committer;
		return this;
	}

	/**
	 * @return the parents
	 */
	public List<Commit> getParents() {
		return parents;
	}

	/**
	 * @param parents
	 *            the parents to set
	 * @return this merge response
	 */
	public RepositoryMergingResponse setParents(List<Commit> parents) {
		this.parents = parents;
		return this;
	}

}
