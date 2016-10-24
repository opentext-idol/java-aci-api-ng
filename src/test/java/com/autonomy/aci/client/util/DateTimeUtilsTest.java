/*
 * Copyright 2006-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.util;

import org.junit.Test;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class DateTimeUtilsTest {

    private final String dateString = "12 Mar 09 16:48:27";

    private final Date dateDate = new Date(1236876507000L);

    @Test
    public void testParseDate() throws ParseException {
        final DateTimeUtils instance = DateTimeUtils.getInstance();

        assertThat("Date wrong", instance.parseDate(dateString, "dd MMM yy HH:mm:ss", Locale.ENGLISH), is(equalTo(dateDate)));
        assertThat("Date wrong", instance.parseDate(dateString, "dd MMM yy HH:mm:ss"), is(equalTo(dateDate)));
        assertThat("Date wrong", instance.parseDate(dateString), is(equalTo(dateDate)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseDateNoString() throws ParseException {
        DateTimeUtils.getInstance().parseDate(null, "dd MMM yy HH:mm:ss Z");
        fail("Should have thrown a IllegalArgumentException");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseDateNoFormat() throws ParseException {
        DateTimeUtils.getInstance().parseDate(dateString, null);
        fail("Should have thrown a IllegalArgumentException");
    }

    @Test(expected = ParseException.class)
    public void testParseDateBadFormat() throws ParseException {
        DateTimeUtils.getInstance().parseDate(dateString, "dd MMM yy HH:mm:ss Z");
        fail("Should have thrown a ParseException");
    }

    @Test(expected = ParseException.class)
    public void testParseDateBadDate() throws ParseException {
        DateTimeUtils.getInstance().parseDate("12 Mar 09 16:48:27 GMT");
        fail("Should have thrown a ParseException");
    }

    @Test
    public void testEpochSecondsToDate() {
        final long time = 1236876507L;
        final Date date = DateTimeUtils.getInstance().epochSecondsToDate(time);
        assertThat("Date is wrong", date.getTime(), is(time * 1000L));
    }

}
