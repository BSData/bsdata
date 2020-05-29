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

/**
 * DeleteEvent payload model class.
 */
public class DeletePayload extends EventPayload {

	private static final long serialVersionUID = -7571623946339106873L;

	private String refType;

	private String ref;

	/**
	 * @return refType
	 */
	public String getRefType() {
		return refType;
	}

	/**
	 * @param refType
	 * @return this DeletePayload
	 */
	public DeletePayload setRefType(String refType) {
		this.refType = refType;
		return this;
	}

	/**
	 * @return ref
	 */
	public String getRef() {
		return ref;
	}

	/**
	 * @param ref
	 * @return this DeletePayload
	 */
	public DeletePayload setRef(String ref) {
		this.ref = ref;
		return this;
	}
}
