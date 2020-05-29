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
package org.eclipse.egit.github.core;

import java.io.Serializable;

/**
 * Id model class.
 */
public class Id implements Serializable {

	/** serialVersionUID */
	private static final long serialVersionUID = -1074145490136786429L;

	private String id;

	/**
	 * Get id
	 *
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 * @return this id
	 */
	public Id setId(String id) {
		this.id = id;
		return this;
	}
}
