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

package com.autonomy.aci.client.transport.gss;

import com.autonomy.aci.client.services.AciConstants;
import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.services.impl.AbstractStAXProcessor;
import com.autonomy.aci.client.services.impl.ErrorProcessor;
import com.autonomy.aci.client.transport.AciHttpException;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.transport.ActionParameter;
import com.autonomy.aci.client.transport.impl.AciHttpClientImpl;
import com.autonomy.aci.client.util.ActionParameters;
import com.autonomy.aci.client.util.IOUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.hc.client5.http.classic.HttpClient;
import org.ietf.jgss.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Set;

/**
 * Implementation of the <code>AciHttpClient</code> interface for use with ACI servers that are secured via GSS-API. It
 * sets up a <code>GSSContext</code> before passing the parameters and connection details to the <code>AciHttpClientImpl</code>
 * super class to execute the actual action.
 */
public class GssAciHttpClientImpl extends AciHttpClientImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(GssAciHttpClientImpl.class);

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    public GssAciHttpClientImpl(final HttpClient httpClient) {
        super(httpClient);
    }

    private GSSContext getGSSContext(final GssAciServerDetails serverDetails) throws AciHttpException, IOException {
        LOGGER.trace("getGSSContext() called...");

        try {
            LOGGER.debug("Setting up to try and create a GSSContext...");

            // Krb5 Oids, see RFC 1964...
            final Oid krb5Mechanism = new Oid("1.2.840.113554.1.2.2");
            final Oid krb5PrincipalNameType = new Oid("1.2.840.113554.1.2.2.1");

            // Prepare stuff for setting up the context...
            final GSSManager manager = GSSManager.getInstance();
            final GSSName serverName = manager.createName(serverDetails.getServiceName(), krb5PrincipalNameType);

            // Set up the context...
            final GSSContext context = manager.createContext(serverName, krb5Mechanism, null, GSSContext.DEFAULT_LIFETIME);
            context.requestConf(true);
            context.requestMutualAuth(true);
            context.requestReplayDet(true);
            context.requestSequenceDet(true);

            // Do the context establishment loop...
            byte[] token = EMPTY_BYTE_ARRAY;

            while (!context.isEstablished()) {
                // token is ignored on the first call
                token = context.initSecContext(token, 0, token.length);

                if (token != null) {
                    LOGGER.debug("Sending GSS action to the ACI server for context token...");

                    // Build the parameter set...
                    final ActionParameters parameters = new ActionParameters();
                    parameters.add(AciConstants.PARAM_ACTION, "GSS");
                    // org.apache.hc.client5.http.utils.Base64;
                    parameters.add("gssServiceName", new String(Base64.encodeBase64(token), "UTF-8"));

                    // Execute the action and process the response...
                    final AciResponseInputStream response = super.executeAction(serverDetails, parameters);
                    token = new GssContextTokenProcessor().process(response);

                    // Ensure that we close the stream to release the connection, otherwise another will be used and the
                    // subsequent action will fail as it wasn't made on the same connection as this token exchange...
                    IOUtils.getInstance().closeQuietly(response);
                }
            }

            // display context information
            LOGGER.debug("Successfully established a GSSContext...");
            LOGGER.debug("Remaining lifetime in seconds = {}", context.getLifetime());
            LOGGER.debug("Context mechanism             = {}", context.getMech());
            LOGGER.debug("Initiator                     = {}", context.getSrcName());
            LOGGER.debug("Acceptor                      = {}", context.getTargName());

            // Return the context...
            return context;
        } catch (final GSSException gsse) {
            throw new AciHttpException("Unable to establish a GSSContext.", gsse);
        } catch (final UnsupportedEncodingException uee) {
            throw new AciHttpException("Unable to establish a GSSContext due to an unsupported encoding.", uee);
        } catch (final ProcessorException pe) {
            throw new AciHttpException("Unable to parse the context response.", pe);
        } catch (final AciErrorException aee) {
            throw new AciHttpException("Unable to establish a GSSContext with the ACI Server.", aee);
        }
    }

    /**
     * Sets up a <code>GSSContext</code> for communicating with the GSS-API protected ACI server and then sends the
     * action. The <code>serverDetails</code> are copied and a <code>GssEncryptionCodec</code> set on the copy that has the
     * <code>GSSContext</code> in it. Any excising <code>EncryptionCodec</code> will be removed as only the
     * <code>GssEncryptionCodec</code> can be used when communicating with Kerberos protected ACI servers.
     * @param serverDetails A <code>GssAciServerDetails</code> containing the service name and connection details.
     * @param parameters    The parameters to send with the ACI action.
     * @return A <code>AciResponseInputStream</code> containing the ACI response.
     * @throws java.io.IOException                                If an I/O (transport) error occurs. Some transport exceptions can be recovered from.
     * @throws com.autonomy.aci.client.transport.AciHttpException If a protocol exception occurs. Usually protocol
     *                                                            exceptions cannot be recovered from.
     * @throws java.lang.IllegalArgumentException                 if <code>serverDetails</code> isn't an instance of
     *                                                            <code>GssAciServerDetails</code> or there is no <code>serviceName</code> set in those details.
     */
    @Override
    public AciResponseInputStream executeAction(final AciServerDetails serverDetails, final Set<? extends ActionParameter<?>> parameters) throws IOException, AciHttpException {
        LOGGER.trace("executeAction() called...");

        // Validate that the server details are of the right type...
        Validate.isTrue((serverDetails instanceof GssAciServerDetails), "The serverDetails must be an instance of GssAciServerDetails.");
        Validate.isTrue(StringUtils.isNotBlank(((GssAciServerDetails) serverDetails).getServiceName()), "No serviceName set in serverDetails.");

        // Create the GSSContext...
        final GSSContext gssContext = getGSSContext((GssAciServerDetails) serverDetails);

        LOGGER.debug("Copying ACI server details and adding a GssEncryptionCodec...");

        // Copy the server details and add the GSSEncryptionCodec... We don't need the serviceName at this point...
        final AciServerDetails copyServerDetails = new AciServerDetails(serverDetails);
        copyServerDetails.setEncryptionCodec(new GssEncryptionCodec(gssContext));

        LOGGER.debug("Letting the superclass execute the action...");

        // Execute the action...
        return super.executeAction(copyServerDetails, parameters);
    }

    /**
     * ACI response processor for getting the GSSContext response from an ACI server.
     */
    private class GssContextTokenProcessor extends AbstractStAXProcessor<byte[]> {

        private static final long serialVersionUID = 2681714185012250398L;

        @Override
        public byte[] process(final XMLStreamReader idolResponse) throws AciErrorException, ProcessorException {
            try {
                if (isErrorResponse(idolResponse)) {
                    setErrorProcessor(new ErrorProcessor());
                    processErrorResponse(idolResponse);
                }

                while (idolResponse.hasNext()) {
                    // Get the event type...
                    final int eventType = idolResponse.next();

                    if ((XMLEvent.START_ELEMENT == eventType) && "context".equals(idolResponse.getLocalName())) {
                        return Base64.decodeBase64(idolResponse.getElementText().getBytes());
                    }
                }

                throw new ProcessorException("Unable to establish the GSSContext as no context was returned from the ACI Server.");
            } catch (final XMLStreamException xmlse) {
                throw new ProcessorException("Unable to get context information.", xmlse);
            }
        }
    }

}
