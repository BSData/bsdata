/******************************************************************************
 *  Copyright (c) 2011, 2018 GitHub Inc. and others
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *    Kevin Sawicki (GitHub Inc.) - initial API and implementation
 *    Singaram Subramanian (Capital One) - (Bug: 529850)
 *    			 User teams across GitHub organizations implementation
 *****************************************************************************/
package org.eclipse.egit.github.core;

import java.io.Serializable;

/**
 * Team model class.
 */
public class Team implements Serializable {

	/** serialVersionUID */
	private static final long serialVersionUID = -1844276044857264413L;

	private int id;

	private int membersCount;

	private int reposCount;

	private String name;

	private String permission;

	private String url;

	private Organization organization;

	/**
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 * @return this team
	 */
	public Team setId(int id) {
		this.id = id;
		return this;
	}

	/**
	 * @return membersCount
	 */
	public int getMembersCount() {
		return membersCount;
	}

	/**
	 * @param membersCount
	 * @return this team
	 */
	public Team setMembersCount(int membersCount) {
		this.membersCount = membersCount;
		return this;
	}

	/**
	 * @return reposCount
	 */
	public int getReposCount() {
		return reposCount;
	}

	/**
	 * @param reposCount
	 * @return this team
	 */
	public Team setReposCount(int reposCount) {
		this.reposCount = reposCount;
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
	 * @return this team
	 */
	public Team setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * @return permission
	 */
	public String getPermission() {
		return permission;
	}

	/**
	 * @param permission
	 * @return this team
	 */
	public Team setPermission(String permission) {
		this.permission = permission;
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
	 * @return this team
	 */
	public Team setUrl(String url) {
		this.url = url;
		return this;
	}

	/**
	 * @return the organization
	 */
	public Organization getOrganization() {
		return organization;
	}

	/**
	 * @param organization the organization to set
	 */
	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

}
