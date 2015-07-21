/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.services.impl;

import com.autonomy.aci.client.services.AciConstants;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.AciServiceException;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.transport.AciHttpClient;
import com.autonomy.aci.client.transport.AciHttpException;
import com.autonomy.aci.client.transport.AciParameter;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.util.IOUtils;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the <tt>AciService</tt> interface.
 * <p/>
 * This implementation of the <tt>AciService</tt> interface does no configuration of the <tt>AciHttpClient</tt> or
 * <tt>AciServerDetails</tt> objects that it uses. It expects all the configuration to have been done by the user before
 * passing them to this object. This configuration can be done in normal code, or via an IoC container like Spring.
 */
public class AciServiceImpl implements AciService {

    /**
     * Class logger...
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AciServiceImpl.class);

    /**
     * Used to confirm that a passed in set of parameters contains an action=xxx parameter...
     */
    private static final AciParameter TEST_ACTION_PARAMETER = new AciParameter(AciConstants.PARAM_ACTION, null);

    /**
     * Holds value of property aciHttpClient.
     */
    private AciHttpClient aciHttpClient;

    /**
     * Holds value of property aciServerDetails.
     */
    private AciServerDetails aciServerDetails;

    /**
     * Creates a new instance of AciServiceImpl.
     */
    public AciServiceImpl() {
        super();
    }

    /**
     * Creates a new instance of AciServiceImpl.
     * @param aciHttpClient The configured <tt>AciHttpClient</tt> to use for communication
     */
    public AciServiceImpl(final AciHttpClient aciHttpClient) {
        this.aciHttpClient = aciHttpClient;
    }

    /**
     * Creates a new instance of AciServiceImpl.
     * @param aciHttpClient    The configured <tt>AciHttpClient</tt> to use for communication
     * @param aciServerDetails The details of the ACI Server to contact
     */
    public AciServiceImpl(final AciHttpClient aciHttpClient, final AciServerDetails aciServerDetails) {
        this.aciHttpClient = aciHttpClient;
        this.aciServerDetails = aciServerDetails;
    }

    /**
     * Executes an ACI action and processes the response with the supplied <tt>Processor</tt>. This method relies on the
     * ACI Server connection details being set via the {@link #setAciServerDetails(AciServerDetails)} method.
     * @param parameters The parameters to use with the ACI command. This <strong>should</strong> include an {@code
     *                   Action=<command>} parameter.
     * @param processor  The <tt>Processor</tt> to use for converting the response stream into an object
     * @return The ACI response encoded as an object of type <tt>T</tt>
     * @throws AciServiceException      If an error occurred during the communication with the ACI Server, processing the
     *                                  response or if the response contained an error
     * @throws IllegalArgumentException If connection details haven't bee set, or the <tt>parameters</tt> is <tt>null</tt>,
     *                                  empty or missing an action parameter. Will also be thrown in the <tt>processor</tt>
     *                                  is null.
     */
    public <T> T executeAction(final Set<? extends AciParameter> parameters, final Processor<T> processor) {
        LOGGER.trace("executeAction() called...");

        // Execute and return the result...
        return executeAction(aciServerDetails, parameters, processor);
    }

    /**
     * Executes an ACI action and processes the response with the supplied <tt>Processor</tt>.
     * @param serverDetails The connection details of the ACI Server to execute the action on
     * @param parameters    The parameters to use with the ACI command. This <strong>should</strong> include an {@code
     *                      Action=&lt;command&gt;} parameter
     * @param processor     The <tt>Processor</tt> to use for converting the response stream into an object
     * @return The ACI response encoded as an object of type <tt>T</tt>
     * @throws AciServiceException      If an error occurred during the communication with the ACI Server, processing the
     *                                  response or if the response contained an error
     * @throws IllegalArgumentException If <tt>serverDetails</tt> is <tt>null</tt>, or the <tt>parameters</tt> is
     *                                  <tt>null</tt>, empty or missing an action parameter. Will also be thrown in the
     *                                  <tt>processor</tt> is null.
     */
    public <T> T executeAction(final AciServerDetails serverDetails, final Set<? extends AciParameter> parameters, final Processor<T> processor) {
        LOGGER.trace("executeAction() called...");

        // Sanity check the HttpClient...
        Validate.notNull(aciHttpClient, "An AciHttpClient implementation must be set before calling this method.");

        // Sanity check the method parameters...
        Validate.notNull(serverDetails, "ACI Server connection details must be set before calling this method.");
        Validate.notEmpty(parameters, "The parameter set must not be null or empty.");
        Validate.isTrue(parameters.contains(TEST_ACTION_PARAMETER), "The parameter set must contain an action=xxx parameter.");
        Validate.notNull(processor, "The processor must not be null.");

        // This is so we can close the response and return the connection to the pool...
        AciResponseInputStream response = null;

        try {
            LOGGER.debug("Sending the ACI parameters and server details to the AciHttpClient...");

            // Execute the action and process the response...
            response = aciHttpClient.executeAction(serverDetails, parameters);
            return processor.process(response);
        } catch (final AciHttpException ahe) {
            LOGGER.trace("AciHttpException caught while executing the ACI action");
            throw new AciServiceException(ahe);
        } catch (final IOException ioe) {
            LOGGER.trace("IOException caught while executing the ACI action");
            throw new AciServiceException(ioe);
        } catch (final ProcessorException pe) {
            LOGGER.trace("ProcessorException caught while parsing ACI response");
            throw new AciServiceException(pe);
        } finally {
            // Close the response as the processor should have dealt with it...
            IOUtils.getInstance().closeQuietly(response);
        }
    }

    /**
     * Getter for property aciHttpClient.
     * @return Value of property aciHttpClient.
     */
    public AciHttpClient getAciHttpClient() {
        return this.aciHttpClient;
    }

    /**
     * Setter for property aciHttpClient.
     * @param aciHttpClient New value of property aciHttpClient.
     */
    public void setAciHttpClient(final AciHttpClient aciHttpClient) {
        this.aciHttpClient = aciHttpClient;
    }

    /**
     * Getter for property aciServerDetails.
     * @return Value of property aciServerDetails.
     */
    public AciServerDetails getAciServerDetails() {
        return this.aciServerDetails;
    }

    /**
     * Setter for property aciServerDetails.
     * @param aciServerDetails New value of property aciServerDetails.
     */
    public void setAciServerDetails(final AciServerDetails aciServerDetails) {
        this.aciServerDetails = aciServerDetails;
    }

}
