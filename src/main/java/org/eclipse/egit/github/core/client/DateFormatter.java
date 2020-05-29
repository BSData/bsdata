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
package org.eclipse.egit.github.core.client;

import static org.eclipse.egit.github.core.client.IGitHubConstants.DATE_FORMAT;
import static org.eclipse.egit.github.core.client.IGitHubConstants.DATE_FORMAT_V2_1;
import static org.eclipse.egit.github.core.client.IGitHubConstants.DATE_FORMAT_V2_2;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Formatter for date formats present in the GitHub v2 and v3 API.
 */
public class DateFormatter implements JsonDeserializer<Date>,
		JsonSerializer<Date> {

	private final DateFormat[] formats;

	/**
	 * Create date formatter
	 */
	public DateFormatter() {
		formats = new DateFormat[3];
		formats[0] = new SimpleDateFormat(DATE_FORMAT);
		formats[1] = new SimpleDateFormat(DATE_FORMAT_V2_1);
		formats[2] = new SimpleDateFormat(DATE_FORMAT_V2_2);
		final TimeZone timeZone = TimeZone.getTimeZone("Zulu"); //$NON-NLS-1$
		for (DateFormat format : formats)
			format.setTimeZone(timeZone);
	}

	@Override
	public Date deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonParseException exception = null;
		final String value = json.getAsString();
		for (DateFormat format : formats) {
			try {
				synchronized (format) {
					return format.parse(value);
				}
			} catch (ParseException e) {
				exception = new JsonParseException(e);
			}
		}
		if (exception != null) { // Always true here.
			throw exception;
		}
		// We'll never get here, but JDT's null analysis get's confused.
		return null;
	}

	@Override
	public JsonElement serialize(Date date, Type type,
			JsonSerializationContext context) {
		final DateFormat primary = formats[0];
		String formatted;
		synchronized (primary) {
			formatted = primary.format(date);
		}
		return new JsonPrimitive(formatted);
	}
}
