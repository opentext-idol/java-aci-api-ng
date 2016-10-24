/*
 * Copyright 2006-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
     * Copy bytes from an <tt>InputStream</tt> to an <tt>OutputStream</tt>.
     * <p/>
     * This method buffers the input internally, so there is no need to use a <tt>BufferedInputStream</tt>.
     * @param input  The <tt>InputStream</tt> to read from
     * @param output The <tt>OutputStream</tt> to write to
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
     * Closes the stream and ignores any <tt>IOExceptions</tt> that are thrown.
     * @param closeable The <tt>Closable</tt> to close
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
