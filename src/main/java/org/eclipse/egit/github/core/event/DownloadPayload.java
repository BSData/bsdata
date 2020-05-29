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

import org.eclipse.egit.github.core.Download;

/**
 * DownloadEvent payload model class.
 */
public class DownloadPayload extends EventPayload {

	private static final long serialVersionUID = 4246935370658381214L;

	private Download download;

	/**
	 * @return download
	 */
	public Download getDownload() {
		return download;
	}

	/**
	 * @param download
	 * @return this DownloadPayload
	 */
	public DownloadPayload setDownload(Download download) {
		this.download = download;
		return this;
	}
}
