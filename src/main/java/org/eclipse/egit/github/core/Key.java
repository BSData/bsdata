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
 * Key model class.
 */
public class Key implements Serializable {

	/** serialVersionUID */
	private static final long serialVersionUID = -7763033793023520265L;

	private int id;

	private String key;

	private String title;

	private String url;

	/**
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 * @return this deploy key
	 */
	public Key setId(int id) {
		this.id = id;
		return this;
	}

	/**
	 * @return key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 * @return this deploy key
	 */
	public Key setKey(String key) {
		this.key = key;
		return this;
	}

	/**
	 * @return title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 * @return this deploy key
	 */
	public Key setTitle(String title) {
		this.title = title;
		return this;
	}

	/**
	 * @return url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 * @return this deploy key
	 */
	public Key setUrl(String url) {
		this.url = url;
		return this;
	}
}
