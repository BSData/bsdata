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
package org.eclipse.egit.github.core.event;

import java.io.Serializable;

/**
 * Model class for repository information contained in an {@link Event}
 */
public class EventRepository implements Serializable {

	/** serialVersionUID */
	private static final long serialVersionUID = -8910798454171899699L;

	private long id;

	private String name;

	private String url;

	/**
	 * @return id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 * @return this event repository
	 */
	public EventRepository setId(long id) {
		this.id = id;
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
	 * @return this event repository
	 */
	public EventRepository setName(String name) {
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
	 * @return this event repository
	 */
	public EventRepository setUrl(String url) {
		this.url = url;
		return this;
	}
}
