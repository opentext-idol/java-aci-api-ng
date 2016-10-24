/*
 * Copyright 2006-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.transport;

import java.io.IOException;
import java.util.Set;

/**
 * This interface defines the methods which should be implemented by any class that does the actual communication with
 * an ACI Server.
 */
public interface AciHttpClient {

    /**
     * Execute an ACI action on the ACI Server whose connection details are supplied.
     * @param serverDetails Details of the ACI Server to send the action to
     * @param parameters    The parameters to send with the ACI action
     * @return A {@code com.autonomy.aci.client.transport.AciResponseInputStream} containing the ACI response
     * @throws IOException      If an I/O (transport) error occurs. Some transport exceptions can be recovered from.
     * @throws AciHttpException If a protocol exception occurs. Usually protocol exceptions cannot be recovered from.
     */
    AciResponseInputStream executeAction(AciServerDetails serverDetails, Set<? extends AciParameter> parameters) throws IOException, AciHttpException;

}
