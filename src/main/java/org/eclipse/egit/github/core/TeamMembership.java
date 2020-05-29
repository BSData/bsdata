/******************************************************************************
 *  Copyright (c) 2014, 2015 Arizona Board of Regents
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *    Michael Mathews (Arizona Board of Regents) - (Bug: 447419)
 *    			 Team Membership API implementation
 *****************************************************************************/
package org.eclipse.egit.github.core;

import java.io.Serializable;

/**
 * Team Membership model class.
 */
public class TeamMembership implements Serializable {

	private static final long serialVersionUID = -8207728181588115431L;

	/**
	 * The possible states of a Team Membership
	 */
	public static enum TeamMembershipState {
		/** Active member. */
		ACTIVE,
		/** Not yet active member. */
		PENDING;
	}

	private TeamMembershipState state;

	private String url;

	/**
	 * @return state
	 */
	public TeamMembershipState getState() {
		return state;
	}

	/**
	 * @param state
	 */
	public void setState(TeamMembershipState state) {
		this.state = state;
	}

	/**
	 * @return url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 */
	public void setUrl(String url) {
		this.url = url;
	}
}
