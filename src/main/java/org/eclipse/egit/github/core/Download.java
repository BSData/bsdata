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
 * Download model class
 */
public class Download implements Serializable {

	/** serialVersionUID */
	private static final long serialVersionUID = 6554996867709945406L;

	private int downloadCount;

	private int id;

	private long size;

	private String description;

	private String contentType;

	private String htmlUrl;

	private String name;

	private String url;

	/**
	 * @return downloadCount
	 */
	public int getDownloadCount() {
		return downloadCount;
	}

	/**
	 * @param downloadCount
	 * @return this download
	 */
	public Download setDownloadCount(int downloadCount) {
		this.downloadCount = downloadCount;
		return this;
	}

	/**
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 * @return this download
	 */
	public Download setId(int id) {
		this.id = id;
		return this;
	}

	/**
	 * @return size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * @param size
	 * @return this download
	 */
	public Download setSize(long size) {
		this.size = size;
		return this;
	}

	/**
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 * @return this download
	 */
	public Download setDescription(String description) {
		this.description = description;
		return this;
	}

	/**
	 * @return contentType
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @param contentType
	 * @return this download
	 */
	public Download setContentType(String contentType) {
		this.contentType = contentType;
		return this;
	}

	/**
	 * @return htmlUrl
	 */
	public String getHtmlUrl() {
		return htmlUrl;
	}

	/**
	 * @param htmlUrl
	 * @return this download
	 */
	public Download setHtmlUrl(String htmlUrl) {
		this.htmlUrl = htmlUrl;
		return this;
	}

	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 * @return this download
	 */
	public Download setName(String name) {
		this.name = name;
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
	 * @return this download
	 */
	public Download setUrl(String url) {
		this.url = url;
		return this;
	}
}
