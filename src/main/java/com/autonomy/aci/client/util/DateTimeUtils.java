/*
 * Copyright 2006-2018 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.autonomy.aci.client.util;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This utility class contains methods that are used by some the the APIs processors that need to convert date strings
 * into {@link java.util.Date} objects.
 */
public class DateTimeUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeUtils.class);

    /**
     * Thread safe singleton instance of our class.
     */
    private static final DateTimeUtils INSTANCE = new DateTimeUtils();

    /**
     * Returns the thread safe singleton instance of this class.
     * @return The thread safe singleton instance of this class
     */
    public static DateTimeUtils getInstance() {
        return INSTANCE;
    }

    /**
     * Parses a string representing a date, using the pattern <code>dd MMM yy HH:mm:ss</code> and the <code>ENGLISH</code>
     * locale.
     * <p>
     * A parse is only deemed successful if it parses the whole of the input string. If the parse pattern didn't match, a
     * ParseException is thrown.
     * @param string The date to parse, not null
     * @return The parsed date
     * @throws IllegalArgumentException If the date string is null
     * @throws ParseException           If the date pattern was unsuitable
     */
    public Date parseDate(final String string) throws ParseException {
        return parseDate(string, "dd MMM yy HH:mm:ss");
    }

    /**
     * Parses a string representing a date, using the supplied pattern and the <code>ENGLISH</code> locale.
     * <p>
     * A parse is only deemed successful if it parses the whole of the input string. If the parse pattern didn't match, a
     * ParseException is thrown.
     * @param string The date to parse, not null
     * @param format The date format pattern to use, see {@link java.text.SimpleDateFormat}, not null
     * @return The parsed date
     * @throws IllegalArgumentException If the date string is null
     * @throws ParseException           If the date pattern was unsuitable
     */
    public Date parseDate(final String string, final String format) throws ParseException {
        return parseDate(string, format, Locale.ENGLISH);
    }

    /**
     * Parses a string representing a date, using the supplied pattern and locale.
     * <p>
     * A parse is only deemed successful if it parses the whole of the input string. If the parse pattern didn't match, a
     * ParseException is thrown.
     * @param string The date to parse, not null
     * @param format The date format pattern to use, see {@link java.text.SimpleDateFormat}, not null
     * @param locale The locale whose date format symbols should be used
     * @return The parsed date
     * @throws IllegalArgumentException If the date <code>string</code> or <code>format</code> are null
     * @throws ParseException           If the date pattern was unsuitable
     */
    public Date parseDate(final String string, final String format, final Locale locale) throws ParseException {
        LOGGER.trace("parseDate() called...");

        Validate.notEmpty(string, "Date string must not be null");
        Validate.notEmpty(format, "Date string format must not be null");

        final SimpleDateFormat parser = new SimpleDateFormat(format, locale);
        final ParsePosition pos = new ParsePosition(0);

        final Date date = parser.parse(string, pos);
        if (date != null && pos.getIndex() == string.length()) {
            return date;
        }

        throw new ParseException("Unable to parse the date: " + string, -1);
    }

    /**
     * ACI Servers generally work in epoch seconds when it comes to durations and times. This method add in milliseconds
     * by multiplying by 1000.
     * @param epochSeconds The epoch seconds to convert to a <code>Date</code>
     * @return The <code>epochSeconds</code> with milliseconds added and converted
     */
    public Date epochSecondsToDate(final long epochSeconds) {
        return new Date(epochSeconds * 1000L);
    }

}
