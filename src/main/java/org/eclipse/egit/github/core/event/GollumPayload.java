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

import java.util.List;

import org.eclipse.egit.github.core.GollumPage;

/**
 * GollumEvent payload model class.
 */
public class GollumPayload extends EventPayload {

	private static final long serialVersionUID = 7111499446827257290L;

	private List<GollumPage> pages;

	/**
	 * @return pages
	 */
	public List<GollumPage> getPages() {
		return pages;
	}

	/**
	 * @param pages
	 * @return this GollumPayload
	 */
	public GollumPayload setPages(List<GollumPage> pages) {
		this.pages = pages;
		return this;
	}
}
