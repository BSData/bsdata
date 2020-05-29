/*******************************************************************************
 *  Copyright (c) 2011, 2019 GitHub Inc. and others.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *    Kevin Sawicki (GitHub Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.egit.github.core.service;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_MILESTONES;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.PagedRequest;

/**
 * Milestone service class for listing the {@link Milestone} objects in use by a
 * repository and user accessed via a {@link GitHubClient}.
 *
 * @see <a href="http://developer.github.com/v3/issues/milestones">GitHub
 *      milestones API documentation</a>
 */
public class MilestoneService extends GitHubService {

	private static final String TITLE = "title"; //$NON-NLS-1$

	private static final String DESCRIPTION = "description"; //$NON-NLS-1$

	private static final String DUE_ON = "due_on"; //$NON-NLS-1$

	private static final String STATE = "state"; //$NON-NLS-1$

	/**
	 * Create milestone service
	 */
	public MilestoneService() {
		super();
	}

	/**
	 * Create milestone service
	 *
	 * @param client
	 *            cannot be null
	 */
	public MilestoneService(GitHubClient client) {
		super(client);
	}

	/**
	 * Get milestones
	 *
	 * @param repository
	 * @param state
	 * @return list of milestones
	 * @throws IOException
	 */
	public List<Milestone> getMilestones(IRepositoryIdProvider repository,
			String state) throws IOException {
		String repoId = getId(repository);
		return getMilestones(repoId, state);
	}

	/**
	 * Get milestones
	 *
	 * @param user
	 * @param repository
	 * @param state
	 * @return list of milestones
	 * @throws IOException
	 */
	public List<Milestone> getMilestones(String user, String repository,
			String state) throws IOException {
		verifyRepository(user, repository);

		String repoId = user + '/' + repository;
		return getMilestones(repoId, state);
	}

	private List<Milestone> getMilestones(String id, String state)
			throws IOException {

		String uri = SEGMENT_REPOS + '/' + id + SEGMENT_MILESTONES;
		PagedRequest<Milestone> request = createPagedRequest();
		if (state != null) {
			request.setParams(Collections.singletonMap(
					IssueService.FILTER_STATE, state));
		}
		request.setUri(uri).setType(new TypeToken<List<Milestone>>() {
			// make protected type visible
		}.getType());
		return getAll(request);
	}

	/**
	 * Create a milestone
	 *
	 * @param repository
	 *            must be non-null
	 * @param milestone
	 *            must be non-null
	 * @return created milestone
	 * @throws IOException
	 */
	public Milestone createMilestone(IRepositoryIdProvider repository,
			Milestone milestone) throws IOException {
		String repoId = getId(repository);
		return createMilestone(repoId, milestone);
	}

	/**
	 * Create a milestone
	 *
	 * @param user
	 *            must be non-null
	 * @param repository
	 *            must be non-null
	 * @param milestone
	 *            must be non-null
	 * @return created milestone
	 * @throws IOException
	 */
	public Milestone createMilestone(String user, String repository,
			Milestone milestone) throws IOException {
		verifyRepository(user, repository);

		String repoId = user + '/' + repository;
		return createMilestone(repoId, milestone);
	}

	private Map<String, Object> createParams(Milestone milestone,
			boolean titleRequired) {
		Map<String, Object> params = new LinkedHashMap<>();
		String value = milestone.getTitle();
		if (titleRequired && value == null) {
			throw new IllegalArgumentException(
					"Milestone title must not be null"); //$NON-NLS-1$
		}
		if (value != null) {
			params.put(TITLE, value);
		}
		value = milestone.getState();
		if (value != null) {
			if (!"open".equals(value) && !"closed".equals(value)) { //$NON-NLS-1$ //$NON-NLS-2$
				throw new IllegalArgumentException(
						"Milestone state must be 'open' or 'closed', or null for default ('open')"); //$NON-NLS-1$
			}
			params.put(STATE, value);
		}
		value = milestone.getDescription();
		if (value != null) {
			params.put(DESCRIPTION, value);
		}
		Date date = milestone.getDueOn();
		if (date != null) {
			params.put(DUE_ON, date);
		}
		if (params.isEmpty()) {
			throw new IllegalArgumentException(
					"Milestone operation requires at least one of title, description, state, or due date"); //$NON-NLS-1$
		}
		return params;
	}

	private Milestone createMilestone(String id, Milestone milestone)
			throws IOException {
		if (milestone == null) {
			throw new IllegalArgumentException("Milestone cannot be null"); //$NON-NLS-1$
		}
		String uri = SEGMENT_REPOS + '/' + id + SEGMENT_MILESTONES;

		Map<String, Object> dto = createParams(milestone, true);
		return client.post(uri, dto, Milestone.class);
	}

	/**
	 * Get a milestone
	 *
	 * @param repository
	 *            must be non-null
	 * @param number
	 * @return created milestone
	 * @throws IOException
	 */
	public Milestone getMilestone(IRepositoryIdProvider repository, int number)
			throws IOException {
		return getMilestone(repository, Integer.toString(number));
	}

	/**
	 * Get a milestone
	 *
	 * @param repository
	 *            must be non-null
	 * @param number
	 *            must be non-null
	 * @return created milestone
	 * @throws IOException
	 */
	public Milestone getMilestone(IRepositoryIdProvider repository,
			String number) throws IOException {
		String repoId = getId(repository);
		return getMilestone(repoId, number);
	}

	/**
	 * Get a milestone
	 *
	 * @param user
	 *            must be non-null
	 * @param repository
	 *            must be non-null
	 * @param number
	 * @return created milestone
	 * @throws IOException
	 */
	public Milestone getMilestone(String user, String repository, int number)
			throws IOException {
		return getMilestone(user, repository, Integer.toString(number));
	}

	/**
	 * Get a milestone
	 *
	 * @param user
	 *            must be non-null
	 * @param repository
	 *            must be non-null
	 * @param number
	 *            must be non-null
	 * @return created milestone
	 * @throws IOException
	 */
	public Milestone getMilestone(String user, String repository, String number)
			throws IOException {
		verifyRepository(user, repository);

		String repoId = user + '/' + repository;
		return getMilestone(repoId, number);
	}

	private Milestone getMilestone(String id, String number) throws IOException {
		if (number == null || number.isEmpty()) {
			throw new IllegalArgumentException(
					"Milestone cannot be null or empty"); //$NON-NLS-1$
		}
		String uri = SEGMENT_REPOS + '/' + id + SEGMENT_MILESTONES + '/'
				+ number;
		GitHubRequest request = createRequest();
		request.setUri(uri);
		request.setType(Milestone.class);
		return (Milestone) client.get(request).getBody();
	}

	/**
	 * Delete a milestone with the given id from the given repository
	 *
	 * @param repository
	 * @param milestone
	 * @throws IOException
	 */
	public void deleteMilestone(IRepositoryIdProvider repository, int milestone)
			throws IOException {
		deleteMilestone(repository, Integer.toString(milestone));
	}

	/**
	 * Delete a milestone with the given id from the given repository
	 *
	 * @param repository
	 * @param milestone
	 * @throws IOException
	 */
	public void deleteMilestone(IRepositoryIdProvider repository,
			String milestone) throws IOException {
		String repoId = getId(repository);
		deleteMilestone(repoId, milestone);
	}

	/**
	 * Delete a milestone with the given id from the given repository
	 *
	 * @param user
	 * @param repository
	 * @param milestone
	 * @throws IOException
	 */
	public void deleteMilestone(String user, String repository, int milestone)
			throws IOException {
		deleteMilestone(user, repository, Integer.toString(milestone));
	}

	/**
	 * Delete a milestone with the given id from the given repository
	 *
	 * @param user
	 * @param repository
	 * @param milestone
	 * @throws IOException
	 */
	public void deleteMilestone(String user, String repository, String milestone)
			throws IOException {
		verifyRepository(user, repository);

		String repoId = user + '/' + repository;
		deleteMilestone(repoId, milestone);
	}

	private void deleteMilestone(String id, String milestone)
			throws IOException {
		if (milestone == null || milestone.isEmpty()) {
			throw new IllegalArgumentException(
					"Milestone cannot be null or empty"); //$NON-NLS-1$
		}
		String uri = SEGMENT_REPOS + '/' + id + SEGMENT_MILESTONES + '/'
				+ milestone;
		client.delete(uri);
	}

	/**
	 * Edit the given milestone in the given repository
	 *
	 * @param repository
	 * @param milestone
	 * @return edited milestone
	 * @throws IOException
	 */
	public Milestone editMilestone(IRepositoryIdProvider repository,
			Milestone milestone) throws IOException {
		String repoId = getId(repository);
		if (milestone == null) {
			throw new IllegalArgumentException("Milestone cannot be null"); //$NON-NLS-1$
		}
		String uri = SEGMENT_REPOS + '/' + repoId + SEGMENT_MILESTONES + '/'
				+ milestone.getNumber();

		Map<String, Object> dto = createParams(milestone, false);
		return client.post(uri, dto, Milestone.class);
	}
}
