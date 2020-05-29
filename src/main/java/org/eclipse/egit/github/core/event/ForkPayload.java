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

import org.eclipse.egit.github.core.Repository;

/**
 * ForkEvent payload model class.
 */
public class ForkPayload extends EventPayload {

	private static final long serialVersionUID = 2110456722558520113L;

	private Repository forkee;

	/**
	 * @return forkee
	 */
	public Repository getForkee() {
		return forkee;
	}

	/**
	 * @param forkee
	 * @return this ForkPayload
	 */
	public ForkPayload setForkee(Repository forkee) {
		this.forkee = forkee;
		return this;
	}
}
