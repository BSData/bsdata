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

import org.eclipse.egit.github.core.CommitComment;

/**
 * CommitCommentEvent payload model class.
 */
public class CommitCommentPayload extends EventPayload {

	private static final long serialVersionUID = -2606554911096551099L;

	private CommitComment comment;

	/**
	 * @return comment
	 */
	public CommitComment getComment() {
		return comment;
	}

	/**
	 * @param comment
	 * @return this CommitCommentPayload
	 */
	public CommitCommentPayload setComment(CommitComment comment) {
		this.comment = comment;
		return this;
	}
}
