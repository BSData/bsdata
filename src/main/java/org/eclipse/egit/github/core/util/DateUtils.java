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

import java.util.Date;

/**
 * Date utilities
 */
public final class DateUtils {

	private DateUtils() {
		// utility class
	}

	/**
	 * Clone date if non-null
	 *
	 * @param date
	 * @return copied date
	 */
	public static Date clone(final Date date) {
		if (date == null)
			return null;
		return new Date(date.getTime());
	}
}
