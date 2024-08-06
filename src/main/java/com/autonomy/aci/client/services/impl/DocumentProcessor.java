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

package com.autonomy.aci.client.services.impl;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import com.autonomy.aci.client.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

/**
 * <code>Processor</code> implementation that converts an ACI response into a DOM <code>Document</code> for further processing
 * either directly via DOM or via XPath.
 */
public class DocumentProcessor implements Processor<Document> {
    private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private static final XPathFactory xpathFactory = XPathFactory.newInstance();

    static {
        // Should speed things up a bit...
        documentBuilderFactory.setNamespaceAware(false);
        documentBuilderFactory.setValidating(false);
        documentBuilderFactory.setXIncludeAware(false);
        // https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#jaxp-documentbuilderfactory-saxparserfactory-and-dom4j
        try {
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private static final long serialVersionUID = -1757174558649421058L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentProcessor.class);

    /**
     * Converts the <code>InputStream</code> into a DOM <code>Document</code>.
     * @param response The <code>InputStream to convert</code>
     * @return The resulting DOM <code>Document</code>
     * @throws ProcessorException If there was an exception while parsing the input stream or configuring the
     *                            <code>DocumentBuilderFactory</code>.
     */
    private Document convertACIResponseToDOM(final InputStream response) {
        LOGGER.trace("convertACIResponseToDOM() called...");

        try {
            // Get a document builder and convert the document...
            return documentBuilderFactory.newDocumentBuilder().parse(new InputSource(response));
        } catch (final ParserConfigurationException | IOException | SAXException pce) {
            throw new ProcessorException("Unable to parse the ACI response into a DOM document.", pce);
        }
    }

    /**
     * Checks the DOM <code>Document</code> to see if it is an ACI Server error response. If it is, it pulls all the
     * information contained in the response into an <code>AciErrorException</code> and throws it, otherwise it does
     * nothing.
     * @param response The DOM <code>Document</code> to check.
     * @throws AciErrorException  If an error response was detected.
     * @throws ProcessorException If there was any other kind of exception caught during processing.
     */
    private void checkACIResponseForError(final Document response) {
        LOGGER.trace("checkACIResponseForError() called...");

        // Create an xpath object for getting stuff with...
        final XPath xpath = xpathFactory.newXPath();

        try {
            // Get the value of the <response> tag...
            final String value = xpath.evaluate("/autnresponse/response", response);

            LOGGER.debug("Response tag has value - {}...", value);

            if ("ERROR".equals(value)) {
                LOGGER.debug("Error response detected, creating an AciErrorException...");

                // Create an exception to throw...
                final AciErrorException error = new AciErrorException();

                // Get the error properties...
                error.setErrorId(xpath.evaluate("/autnresponse/responsedata/error/errorid", response));
                error.setRawErrorId(xpath.evaluate("/autnresponse/responsedata/error/rawerrorid", response));
                error.setErrorString(xpath.evaluate("/autnresponse/responsedata/error/errorstring", response));
                error.setErrorDescription(xpath.evaluate("/autnresponse/responsedata/error/errordescription", response));
                error.setErrorCode(xpath.evaluate("/autnresponse/responsedata/error/errorcode", response));
                try {
                    error.setErrorTime(DateTimeUtils.getInstance().parseDate(xpath.evaluate("/autnresponse/responsedata/error/errortime", response), "dd MMM yy HH:mm:ss"));
                } catch (final ParseException pe) {
                    LOGGER.error("ParseException caught while trying to convert errortime tag into java.util.Date.", pe);
                }

                // Throw the error...
                throw error;
            }
        } catch (final XPathExpressionException xpee) {
            // Throw a wobbler, as this should never happen...
            throw new ProcessorException("XPathExpressionException caught while trying to check ACI response for ERROR flag.", xpee);
        }
    }

    /**
     * Process the ACI response input into a DOM <code>Document</code>.
     * @param aciResponse The ACI response to process
     * @return A DOM <code>Document</code>
     * @throws AciErrorException  If the ACI response was an error response
     * @throws ProcessorException If an error occurred during the processing of the ACI response
     */
    public Document process(final AciResponseInputStream aciResponse) {
        LOGGER.trace("process() called...");

        // Convert the stream into a DOM Document...
        final Document document = convertACIResponseToDOM(aciResponse);

        LOGGER.debug("Checking for ERROR response...");

        // Check the response for an error flag...
        checkACIResponseForError(document);

        LOGGER.debug("Returning the DOM Document to caller...");

        // Return the document...
        return document;
    }

}
