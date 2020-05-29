/******************************************************************************
 *  Copyright (c) 2012 GitHub Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *    Kevin Sawicki (GitHub Inc.) - initial API and implementation
 *****************************************************************************/
package org.eclipse.egit.github.core.service;

import static org.eclipse.egit.github.core.client.IGitHubConstants.CHARSET_UTF8;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_MARKDOWN;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.client.GitHubClient;

/**
 * Service to request Markdown text to be rendered as HTML
 *
 * @see <a href="http://developer.github.com/v3/markdown/">GitHub Markdown API
 *      documentation</a>
 */
public class MarkdownService extends GitHubService {

	/**
	 * GitHub-flavored Markdown mode
	 */
	public static final String MODE_GFM = "gfm"; //$NON-NLS-1$

	/**
	 * Default Markdown mode
	 */
	public static final String MODE_MARKDOWN = "markdown"; //$NON-NLS-1$

	/**
	 * Create Markdown service
	 */
	public MarkdownService() {
		super();
	}

	/**
	 * Create Markdown service for client
	 *
	 * @param client
	 */
	public MarkdownService(final GitHubClient client) {
		super(client);
	}

	private String readStream(final InputStream stream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				stream, CHARSET_UTF8));
		try {
			StringBuilder output = new StringBuilder();
			char[] buffer = new char[8192];
			int read;
			while ((read = reader.read(buffer)) != -1)
				output.append(buffer, 0, read);
			return output.toString();
		} finally {
			try {
				reader.close();
			} catch (IOException ignored) {
				// Ignored
			}
		}
	}

	/**
	 * Get stream of HTML for given Markdown text scoped to given repository
	 * context
	 *
	 * @param repo
	 * @param text
	 * @return stream of HTML
	 * @throws IOException
	 */
	public InputStream getRepositoryStream(final IRepositoryIdProvider repo,
			final String text) throws IOException {
		String context = getId(repo);

		Map<String, String> params = new HashMap<>(3, 1);
		params.put("context", context); //$NON-NLS-1$
		params.put("text", text); //$NON-NLS-1$
		params.put("mode", MODE_GFM); //$NON-NLS-1$

		return client.postStream(SEGMENT_MARKDOWN, params);
	}

	/**
	 * Get HTML for given Markdown text scoped to given repository context
	 *
	 * @param repo
	 * @param text
	 * @return HTML
	 * @throws IOException
	 */
	public String getRepositoryHtml(final IRepositoryIdProvider repo,
			final String text) throws IOException {
		return readStream(getRepositoryStream(repo, text));
	}

	/**
	 * Get stream of HTML for given Markdown text
	 * <p>
	 * Use {@link #getRepositoryStream(IRepositoryIdProvider, String)} if you
	 * want the Markdown scoped to a specific repository.
	 *
	 * @param text
	 * @param mode
	 * @return stream of HTML
	 * @throws IOException
	 */
	public InputStream getStream(final String text, final String mode)
			throws IOException {
		Map<String, String> params = new HashMap<>(2, 1);
		params.put("text", text); //$NON-NLS-1$
		params.put("mode", mode); //$NON-NLS-1$

		return client.postStream(SEGMENT_MARKDOWN, params);
	}

	/**
	 * Get HTML for given Markdown text
	 * <p>
	 * Use {@link #getRepositoryHtml(IRepositoryIdProvider, String)} if you want
	 * the Markdown scoped to a specific repository.
	 *
	 * @param text
	 * @param mode
	 * @return HTML
	 * @throws IOException
	 */
	public String getHtml(final String text, final String mode)
			throws IOException {
		return readStream(getStream(text, mode));
	}
}
