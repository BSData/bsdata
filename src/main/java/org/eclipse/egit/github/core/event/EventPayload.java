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

import java.io.Serializable;

/**
 * Parent class for event payloads
 *
 * @see <a href="http://developer.github.com/v3/events/types">GitHub Event types
 *      API documentation</a>
 */
public class EventPayload implements Serializable {

	private static final long serialVersionUID = 1022083387039340606L;
}
