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
import org.eclipse.egit.github.core.Team;
import org.eclipse.egit.github.core.User;

/**
 * TeamAddEvent payload model class
 */
public class TeamAddPayload extends EventPayload {

	private static final long serialVersionUID = 7660176723347977144L;

	private Team team;

	private User user;

	private Repository repo;

	/**
	 * @return team
	 */
	public Team getTeam() {
		return team;
	}

	/**
	 * @param team
	 * @return this TeamAddPayload
	 */
	public TeamAddPayload setTeam(Team team) {
		this.team = team;
		return this;
	}

	/**
	 * @return user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user
	 * @return this TeamAddPayload
	 */
	public TeamAddPayload setUser(User user) {
		this.user = user;
		return this;
	}

	/**
	 * @return repo
	 */
	public Repository getRepo() {
		return repo;
	}

	/**
	 * @param repo
	 * @return this TeamAddPayload
	 */
	public TeamAddPayload setRepo(Repository repo) {
		this.repo = repo;
		return this;
	}
}
