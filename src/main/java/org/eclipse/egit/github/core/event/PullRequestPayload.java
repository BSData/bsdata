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
 *    Jason Tsay (GitHub Inc.) - initial API and implementation
 *******************************************************************************/

package org.eclipse.egit.github.core.event;

import org.eclipse.egit.github.core.PullRequest;

/**
 * PullRequestEvent payload model class.
 */
public class PullRequestPayload extends EventPayload {

	private static final long serialVersionUID = -8234504270587265625L;

	private String action;

	private int number;

	private PullRequest pullRequest;

	/**
	 * @return action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action
	 * @return this PullRequestPayload
	 */
	public PullRequestPayload setAction(String action) {
		this.action = action;
		return this;
	}

	/**
	 * @return number
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * @param number
	 * @return this PullRequestPayload
	 */
	public PullRequestPayload setNumber(int number) {
		this.number = number;
		return this;
	}

	/**
	 * @return pullRequest
	 */
	public PullRequest getPullRequest() {
		return pullRequest;
	}

	/**
	 * @param pullRequest
	 * @return this PullRequestPayload
	 */
	public PullRequestPayload setPullRequest(PullRequest pullRequest) {
		this.pullRequest = pullRequest;
		return this;
	}
}
