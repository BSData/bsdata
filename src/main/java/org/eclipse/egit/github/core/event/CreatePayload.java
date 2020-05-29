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
 * CreateEvent payload model class.
 */
public class CreatePayload extends EventPayload {

	private static final long serialVersionUID = -7033027645721954674L;

	private String refType;

	private String ref;

	private String masterBranch;

	private String description;

	/**
	 * @return refType
	 */
	public String getRefType() {
		return refType;
	}

	/**
	 * @param refType
	 * @return this CreatePayload
	 */
	public CreatePayload setRefType(String refType) {
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
	 * @return this CreatePayload
	 */
	public CreatePayload setRef(String ref) {
		this.ref = ref;
		return this;
	}

	/**
	 * @return masterBranch
	 */
	public String getMasterBranch() {
		return masterBranch;
	}

	/**
	 * @param masterBranch
	 * @return this CreatePayload
	 */
	public CreatePayload setMasterBranch(String masterBranch) {
		this.masterBranch = masterBranch;
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
	 * @return this CreatePayload
	 */
	public CreatePayload setDescription(String description) {
		this.description = description;
		return this;
	}
}
