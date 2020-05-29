/******************************************************************************
 *  Copyright (c) 2012 GitHub Inc.
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
package org.eclipse.egit.github.core.event;

import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.PullRequest;

/**
 * Payload for an event with type {@link Event#TYPE_PULL_REQUEST_REVIEW_COMMENT}
 */
public class PullRequestReviewCommentPayload extends EventPayload {

	private static final long serialVersionUID = -2403658752886394741L;

	private String action;

	private CommitComment comment;

	private PullRequest pullRequest;

	/**
	 * @return action
	 * @since 4.1
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action
	 * @return this PullRequestReviewCommentPayload
	 * @since 4.1
	 */
	public PullRequestReviewCommentPayload setAction(String action) {
		this.action = action;
		return this;
	}

	/**
	 * @return comment
	 */
	public CommitComment getComment() {
		return comment;
	}

	/**
	 * @param comment
	 * @return this payload
	 */
	public PullRequestReviewCommentPayload setComment(CommitComment comment) {
		this.comment = comment;
		return this;
	}

	/**
	 * @return pullRequest
	 * @since 4.1
	 */
	public PullRequest getPullRequest() {
		return pullRequest;
	}

	/**
	 * @param pullRequest
	 * @return this PullRequestReviewCommentPayload
	 * @since 4.1
	 */
	public PullRequestReviewCommentPayload setPullRequest(PullRequest pullRequest) {
		this.pullRequest = pullRequest;
		return this;
	}
}
