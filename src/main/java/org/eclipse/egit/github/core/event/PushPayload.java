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

import com.google.gson.annotations.SerializedName;

import java.util.List;

import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.Organization;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

/**
 * PushEvent payload model class.
 */
public class PushPayload extends EventPayload {

	private static final long serialVersionUID = -1542484898531583478L;

	private String after;

	private String before;

	private boolean created;

	private boolean deleted;

	private boolean forced;

	private String head;

	private String ref;

	@SerializedName("base_ref")
	private String baseRef;

	private String compare;

	private int size;

	@SerializedName("distinct_size")
	private int distinctSize;

	private List<Commit> commits;

	@SerializedName("head_commit")
	private Commit headCommit;

	private Repository repository;

	private User pusher;

	private Organization organization;

	private User sender;

	/**
	 * @return before
	 */
	public String getBefore() {
		return before;
	}

	/**
	 * @param before
	 * @return this payload
	 */
	public PushPayload setBefore(String before) {
		this.before = before;
		return this;
	}

	/**
	 * @return after
	 */
	public String getAfter() {
		return after;
	}

	/**
	 * @param after
	 * @return this payload
	 */
	public PushPayload setAfter(String after) {
		this.after = after;
		return this;
	}

	/**
	 * @return created
	 */
	public boolean isCreated() {
		return created;
	}

	/**
	 * @param created
	 * @return this PushEvent
	 */
	public PushPayload setCreated(boolean created) {
		this.created = created;
		return this;
	}

	/**
	 * @return deleted
	 */
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * @param deleted
	 * @return this PushEvent
	 */
	public PushPayload setDeleted(boolean deleted) {
		this.deleted = deleted;
		return this;
	}

	/**
	 * @return forced
	 */
	public boolean isForced() {
		return forced;
	}

	/**
	 * @param forced
	 * @return this PushEvent
	 */
	public PushPayload setForced(boolean forced) {
		this.forced = forced;
		return this;
	}

	/**
	 * @return head
	 */
	public String getHead() {
		return head;
	}

	/**
	 * @param head
	 * @return this PushEvent
	 */
	public PushPayload setHead(String head) {
		this.head = head;
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
	 * @return this PushEvent
	 */
	public PushPayload setRef(String ref) {
		this.ref = ref;
		return this;
	}

	/**
	 * @return baseRef
	 */
	public String getBaseRef() {
		return baseRef;
	}

	/**
	 * @param baseRef
	 * @return this PushEvent
	 */
	public PushPayload setBaseRef(String baseRef) {
		this.baseRef = baseRef;
		return this;
	}

	/**
	 * @return compare
	 */
	public String getCompare() {
		return compare;
	}

	/**
	 * @param compare
	 * @return this PushEvent
	 */
	public PushPayload setCompare(String compare) {
		this.compare = compare;
		return this;
	}

	/**
	 * @return size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size
	 * @return this PushEvent
	 */
	public PushPayload setSize(int size) {
		this.size = size;
		return this;
	}

	/**
	 * @return distinctSize
	 */
	public int getDistinctSize() {
		return distinctSize;
	}

	/**
	 * @param distinctSize
	 * @return this PushEvent
	 */
	public PushPayload setDistinctSize(int distinctSize) {
		this.distinctSize = distinctSize;
		return this;
	}

	/**
	 * @return commits
	 */
	public List<Commit> getCommits() {
		return commits;
	}

	/**
	 * @param commits
	 * @return this PushEvent
	 */
	public PushPayload setCommits(List<Commit> commits) {
		this.commits = commits;
		return this;
	}

	/**
	 * @return headCommit
	 */
	public Commit getHeadCommit() {
		return headCommit;
	}

	/**
	 * @param headCommit
	 * @return this PushEvent
	 */
	public PushPayload setHeadCommit(Commit headCommit) {
		this.headCommit = headCommit;
		return this;
	}

	/**
	 * @return repository
	 */
	public Repository getRepository() {
		return repository;
	}

	/**
	 * @param repository
	 * @return this PushEvent
	 */
	public PushPayload setRepository(Repository repository) {
		this.repository = repository;
		return this;
	}

	/**
	 * @return pusher
	 */
	public User getPusher() {
		return pusher;
	}

	/**
	 * @param pusher
	 * @return this PushEvent
	 */
	public PushPayload setPusher(User pusher) {
		this.pusher = pusher;
		return this;
	}

	/**
	 * @return organization
	 */
	public Organization getOrganization() {
		return organization;
	}

	/**
	 * @param organization
	 * @return this PushEvent
	 */
	public PushPayload setOrganization(Organization organization) {
		this.organization = organization;
		return this;
	}

	/**
	 * @return sender
	 */
	public User getSender() {
		return sender;
	}

	/**
	 * @param sender
	 * @return this PushEvent
	 */
	public PushPayload setSender(User sender) {
		this.sender = sender;
		return this;
	}
}
