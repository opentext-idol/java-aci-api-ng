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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Contains some IO utilities that are used by the API.
 */
public class IOUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(IOUtils.class);

    // Thread safe singleton instance of our class.
    private static final IOUtils INSTANCE = new IOUtils();

    /**
     * Returns the thread safe singleton instance of this class.
     * @return The thread safe singleton instance of this class
     */
    public static IOUtils getInstance() {
        return INSTANCE;
    }

    /**
     * Copy bytes from an <code>InputStream</code> to an <code>OutputStream</code>.
     * <p>
     * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
     * @param input  The <code>InputStream</code> to read from
     * @param output The <code>OutputStream</code> to write to
     * @return The number of bytes copied
     * @throws NullPointerException If the input or output is null
     * @throws IOException          If an I/O error occurs
     */
    public long copy(final InputStream input, final OutputStream output) throws IOException {
        final byte[] buffer = new byte[8192];
        long count = 0;

        int number = input.read(buffer);
        while (number != -1) {
            output.write(buffer, 0, number);
            count += number;
            number = input.read(buffer);
        }

        return count;
    }

    /**
     * Closes the stream and ignores any <code>IOExceptions</code> that are thrown.
     * @param closeable The <code>Closable</code> to close
     */
    public void closeQuietly(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException ioe) {
            // Ignore...
            LOGGER.debug("IOException caught while trying to close the stream.", ioe);
        }
    }

}
