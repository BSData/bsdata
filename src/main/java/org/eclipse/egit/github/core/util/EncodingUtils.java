/******************************************************************************
 *  Copyright (c) 2011 GitHub Inc.
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
package org.eclipse.egit.github.core.util;

import static org.eclipse.egit.github.core.client.IGitHubConstants.CHARSET_UTF8;

import java.io.UnsupportedEncodingException;

/**
 * Encoding utilities
 */
public final class EncodingUtils {

	private EncodingUtils() {
		// utility class
	}

	/**
	 * Decode base64 encoded string
	 *
	 * @param content
	 * @return byte array
	 */
	public static final byte[] fromBase64(final String content) {
		return Base64.decode(content);
	}

	/**
	 * Base64 encode given byte array
	 *
	 * @param content
	 * @return byte array
	 */
	public static final String toBase64(final byte[] content) {
		return Base64.encodeBytes(content);
	}

	/**
	 * Base64 encode given byte array
	 *
	 * @param content
	 * @return byte array
	 */
	public static final String toBase64(final String content) {
		byte[] bytes;
		try {
			bytes = content.getBytes(CHARSET_UTF8);
		} catch (UnsupportedEncodingException e) {
			bytes = content.getBytes();
		}
		return toBase64(bytes);
	}
}
