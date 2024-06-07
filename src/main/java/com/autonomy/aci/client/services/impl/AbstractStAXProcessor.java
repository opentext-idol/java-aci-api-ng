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
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.services.StAXProcessor;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Locale;

/**
 * Abstract <code>Processor</code> that should be used by all processors wanting to process the ACI response via the
 * <a href="http://jcp.org/en/jsr/detail?id=173">JSR173</a> StAX API. Some of the properties that can be set on the
 * {@link XMLInputFactory} are exposed, so that the implementation can be modified to suit the required situation.
 * For example, setting {@link #namespaceAware} and {@link #validating} to <code>false</code> should theoretically speed
 * up parsing, especially as ACI responses shouldn't require either to be parsed, (both are <code>false</code> by default
 * for this very reason). When a subclass is created, this class checks to see if any of the properties have been set
 * as system properties and if they have it sets the corresponding property to this value, these values can then be
 * overridden by using the appropriate setter method.
 */
public abstract class AbstractStAXProcessor<T> implements StAXProcessor<T> {

    private static final long serialVersionUID = -6678111384531149923L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStAXProcessor.class);

    /**
     * Holds the factory for creating <code>XMLStreamReader</code>'s...
     */
    private transient XMLInputFactory xmlInputFactory;

    /**
     * Turns on/off implementation specific DTD validation.
     */
    private boolean namespaceAware;

    /**
     * Turns on/off namespace processing for XML 1.0 support.
     */
    private boolean validating;

    /**
     * Requires the processor to coalesce adjacent character data.
     */
    private boolean coalescing;

    /**
     * Replace internal entity references with their replacement text and report them as characters.
     */
    private boolean replacingEntityReferences;

    /**
     * Resolve external parsed entities.
     */
    private boolean supportingExternalEntities;

    /**
     * Use this property to request processors that do not support DTDs.
     */
    private boolean supportDtd;

    /**
     * Holds the processor to use when an error response is detected.
     */
    private StAXProcessor<AciErrorException> errorProcessor;

    /**
     * This constructor gets a new {@link XMLInputFactory} instance that is reused every time
     * {@link #process(com.autonomy.aci.client.transport.AciResponseInputStream)} is called, this
     * should be faster than creating a new instance every time this method is called.
     * <p>
     * The properties are set to the following defaults if they are not specified as system properties:
     * <table summary="">
     * <tr><th>Property</th><th>Default</th></tr>
     * <tr><td>XMLInputFactory.IS_NAMESPACE_AWARE</td><td><code>false</code></td></tr>
     * <tr><td>XMLInputFactory.IS_VALIDATING<code>false</code></td></tr>
     * <tr><td>XMLInputFactory.IS_COALESCING<code>false</code></td></tr>
     * <tr><td>XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES<code>true</code></td></tr>
     * <tr><td>XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES<code>false</code></td></tr>
     * <tr><td>XMLInputFactory.SUPPORT_DTD<code>true</code></td></tr>
     * </table>
     */
    protected AbstractStAXProcessor() {
        // See if the various XMLInputFactory properties are set as system properties...
        namespaceAware = BooleanUtils.toBoolean(StringUtils.defaultString(System.getProperty(XMLInputFactory.IS_NAMESPACE_AWARE), "false"));
        validating = BooleanUtils.toBoolean(StringUtils.defaultString(System.getProperty(XMLInputFactory.IS_VALIDATING), "false"));
        coalescing = BooleanUtils.toBoolean(StringUtils.defaultString(System.getProperty(XMLInputFactory.IS_COALESCING), "false"));
        replacingEntityReferences = BooleanUtils.toBoolean(StringUtils.defaultString(System.getProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES), "true"));
        supportingExternalEntities = BooleanUtils.toBoolean(StringUtils.defaultString(System.getProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES), "false"));
        supportDtd = BooleanUtils.toBoolean(StringUtils.defaultString(System.getProperty(XMLInputFactory.SUPPORT_DTD), "true"));

        // Create the XMLStreamReader factory...
        xmlInputFactory = XMLInputFactory.newInstance();
    }

    private void readObject(final ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        // Read in all the serialized properties...
        inputStream.defaultReadObject();

        // Recreate the XMLStreamReader factory...
        xmlInputFactory = XMLInputFactory.newInstance();
    }

    /**
     * This method firstly checks that the content type of the response is text based and can be parsed. If so, it
     * converts the <code>AciResponseInputStream</code> into a StAX <code>XMLStreamReader</code> and calls the the {@link
     * #process(javax.xml.stream.XMLStreamReader)} method that should be implemented in a subclass to do all the work.
     * @param aciResponseInputStream The ACI response to process
     * @return An object of type <code>T</code>
     * @throws AciErrorException  If the ACI response was an error response
     * @throws ProcessorException If an error occurred during the processing of the IDOL response
     */
    public T process(final AciResponseInputStream aciResponseInputStream) {
        LOGGER.trace("process() called...");

        if (!aciResponseInputStream.getContentType().toLowerCase(Locale.ROOT).startsWith("text/xml")) {
            throw new ProcessorException("This processor is unable to process non-text ACI responses. The content type for this response is " + aciResponseInputStream.getContentType());
        }

        // Define this here so we can make sure it's closed when the processor is finished...
        XMLStreamReader xmlStreamReader = null;

        try {
            // Update the factory with the various properties as they might have changed since the last run...
            xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, namespaceAware);
            xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, validating);
            xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, coalescing);
            xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, replacingEntityReferences);
            xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, supportingExternalEntities);
            xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, supportDtd);

            // Convert the input stream..
            xmlStreamReader = xmlInputFactory.createXMLStreamReader(aciResponseInputStream);
            return process(xmlStreamReader);
        } catch (final XMLStreamException xmlse) {
            throw new ProcessorException("Unable to convert the InputStream to a XMLStreamReader", xmlse);
        } finally {
            if (xmlStreamReader != null) {
                try {
                    // This does NOT close the underlying AciResponseInputStream
                    xmlStreamReader.close();
                } catch (final XMLStreamException xmlse) {
                    LOGGER.error("Unable to close the XMLStreamReader.", xmlse);
                }
            }
        }
    }

    /**
     * Process the ACI response input into an object of type <code>T</code>.
     * @param xmlStreamReader The ACI response to process
     * @return An object of type <code>T</code>
     * @throws AciErrorException  If the ACI response was an error response
     * @throws ProcessorException If an error occurred during the processing of the IDOL response
     */
    public abstract T process(final XMLStreamReader xmlStreamReader);

    /**
     * Reads from the XML stream and tries to determine if the ACI response contains an error or not. The stream is left
     * at the end of the body content of the <code>/autnresponse/response</code> element, if one could be found.
     * @param xmlStreamReader The response to process
     * @return <code>true</code> if the response contains an error, <code>false</code> otherwise
     * @throws javax.xml.stream.XMLStreamException If there was a problem reading the IDOL Server response.
     */
    protected boolean isErrorResponse(final XMLStreamReader xmlStreamReader) throws XMLStreamException {
        LOGGER.trace("isErrorResponse() called...");

        // Get the /autnresponse/response element...
        while (xmlStreamReader.hasNext()) {
            // Get the event type...
            final int eventType = xmlStreamReader.next();

            // Check to see if it's a start event...
            if ((XMLEvent.START_ELEMENT == eventType) && ("response".equalsIgnoreCase(xmlStreamReader.getLocalName()))) {
                return "ERROR".equalsIgnoreCase(xmlStreamReader.getElementText());
            }
        }

        // Couldn't find a /autnresponse/response element...
        throw new XMLStreamException("Unable to find /autnresponse/response element.");
    }

    /**
     * Process the remainder of the IDOL response with the configured error processor.
     * @param xmlStreamReader The IDOL response fragment containing the error information
     * @throws AciErrorException        Once the response has been parsed for all the error information it contains
     * @throws ProcessorException       If something went wrong while trying to process the error response
     * @throws IllegalArgumentException if no error processor has been set.
     */
    protected void processErrorResponse(final XMLStreamReader xmlStreamReader) {
        LOGGER.trace("processErrorResponse() ");

        // Sanity check...
        Validate.notNull(errorProcessor, "Unable to process the error response, as no errorProcessor has been configured.");

        // Process the error response and propagate the resulting exception...
        errorProcessor.process(xmlStreamReader);
    }

    /**
     * Move the cursor forward through the XML stream to the next start element.
     * @param xmlStreamReader The XML stream to use
     * @throws XMLStreamException If there was an error using the stream
     */
    protected void forwardToNextStartElement(final XMLStreamReader xmlStreamReader) throws XMLStreamException {
        while (xmlStreamReader.hasNext()) {
            final int eventType = xmlStreamReader.next();
            if (XMLEvent.START_ELEMENT == eventType) {
                return;
            }
        }
        throw new XMLStreamException("No more START_ELEMENT events found");
    }

    /**
     * Move the cursor forward through the XML stream to the next start or end element, which ever comes first.
     * @param xmlStreamReader The XML stream to use
     * @return The type of event forwarded to, i.e. <code>XMLEvent.START_ELEMENT</code> or <code>XMLEvent.END_ELEMENT</code>.
     * @throws XMLStreamException If there was an error using the stream
     */
    protected int forwardToNextStartOrEndElement(final XMLStreamReader xmlStreamReader) throws XMLStreamException {
        while (xmlStreamReader.hasNext()) {
            final int eventType = xmlStreamReader.next();
            if ((XMLEvent.START_ELEMENT == eventType) || (XMLEvent.END_ELEMENT == eventType)) {
                return eventType;
            }
        }
        throw new XMLStreamException("No more START_ELEMENT or END_ELEMENT events found");
    }

    /**
     * Forwards through the stream looking for the an element with <code>elementName</code>
     * @param elementName     The name of the element to find.
     * @param xmlStreamReader The stream to forward through
     * @throws XMLStreamException If there was an error using the stream, or if no element with <code>elementName</code>
     *                            could be found
     */
    protected void forwardToNamedStartElement(final String elementName, final XMLStreamReader xmlStreamReader) throws XMLStreamException {
        while (xmlStreamReader.hasNext()) {
            final int eventType = xmlStreamReader.next();
            if ((eventType == XMLEvent.START_ELEMENT) && (elementName.equals(xmlStreamReader.getLocalName()))) {
                return;
            }
        }
        throw new XMLStreamException("Unable to find a start element for, " + elementName);
    }

    public StAXProcessor<AciErrorException> getErrorProcessor() {
        return errorProcessor;
    }

    public void setErrorProcessor(final StAXProcessor<AciErrorException> errorProcessor) {
        this.errorProcessor = errorProcessor;
    }

    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    public void setNamespaceAware(final boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
    }

    public boolean isValidating() {
        return validating;
    }

    public void setValidating(final boolean validating) {
        this.validating = validating;
    }

    public boolean isCoalescing() {
        return coalescing;
    }

    public void setCoalescing(final boolean coalescing) {
        this.coalescing = coalescing;
    }

    public boolean isReplacingEntityReferences() {
        return replacingEntityReferences;
    }

    public void setReplacingEntityReferences(final boolean replacingEntityReferences) {
        this.replacingEntityReferences = replacingEntityReferences;
    }

    public boolean isSupportingExternalEntities() {
        return supportingExternalEntities;
    }

    public void setSupportingExternalEntities(final boolean supportingExternalEntities) {
        this.supportingExternalEntities = supportingExternalEntities;
    }

    public boolean isSupportDtd() {
        return supportDtd;
    }

    public void setSupportDtd(final boolean supportDtd) {
        this.supportDtd = supportDtd;
    }

}
