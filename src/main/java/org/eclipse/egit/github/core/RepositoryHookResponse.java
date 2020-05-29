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
package org.eclipse.egit.github.core;

import java.io.Serializable;

/**
 * Repository hook response model class
 */
public class RepositoryHookResponse implements Serializable {

	/** serialVersionUID */
	private static final long serialVersionUID = -1168379336046512838L;

	private int code;

	private String message;

	/**
	 * @return code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * @param code
	 * @return this repsonse
	 */
	public RepositoryHookResponse setCode(int code) {
		this.code = code;
		return this;
	}

	/**
	 * @return message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 * @return this response
	 */
	public RepositoryHookResponse setMessage(String message) {
		this.message = message;
		return this;
	}
}
