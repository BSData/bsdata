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

import org.eclipse.egit.github.core.Gist;

/**
 * GistEvent payload model class.
 */
public class GistPayload extends EventPayload {

	private static final long serialVersionUID = 8916400800708594462L;

	private String action;

	private Gist gist;

	/**
	 * @return action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action
	 * @return this GistPayload
	 */
	public GistPayload setAction(String action) {
		this.action = action;
		return this;
	}

	/**
	 * @return gist
	 */
	public Gist getGist() {
		return gist;
	}

	/**
	 * @param gist
	 * @return this GistPayload
	 */
	public GistPayload setGist(Gist gist) {
		this.gist = gist;
		return this;
	}
}
