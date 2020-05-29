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
 *    Kevin Sawicki (GitHub Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.egit.github.core;

import java.util.List;

/**
 * Interface for container classes that can provide a collection of resources of
 * the same type.
 * 
 * @param <V>
 */
public interface IResourceProvider<V> {

	/**
	 * Get collection of resources
	 * 
	 * @return non-null but possibly empty collection
	 */
	List<V> getResources();

}
