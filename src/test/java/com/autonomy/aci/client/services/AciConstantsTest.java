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

package com.autonomy.aci.client.services;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Not really necessary, but these constants <strong>must</strong> be these values, so fail the test if they have changed.
 */
public class AciConstantsTest {

    @Test
    public void testConstantValues() {
        new AciConstants();

        // Test the actions...
        assertEquals("Encrypted", AciConstants.ACTION_ENCRYPTED);
        assertEquals("GetChildren", AciConstants.ACTION_GET_CHILDREN);
        assertEquals("GetLicenseInfo", AciConstants.ACTION_GET_LICENSE_INFO);
        assertEquals("GetPid", AciConstants.ACTION_GET_PID);
        assertEquals("GetRequestLog", AciConstants.ACTION_GET_REQUEST_LOG);
        assertEquals("GetStatus", AciConstants.ACTION_GET_STATUS);
        assertEquals("GetVersion", AciConstants.ACTION_GET_VERSION);
        assertEquals("ThreadStatus", AciConstants.ACTION_THREAD_STATUS);

        // Test the pParameters...
        assertEquals("Action", AciConstants.PARAM_ACTION);
        assertEquals("Data", AciConstants.PARAM_DATA);
        assertEquals("EncryptResponse", AciConstants.PARAM_ENCRYPT_RESPONSE);
        assertEquals("FileName", AciConstants.PARAM_FILENAME);
        assertEquals("ForceTemplateRefresh", AciConstants.PARAM_FORCE_TEMPLATE_REFRESH);
        assertEquals("Format", AciConstants.PARAM_FORMAT);
        assertEquals("OpenLinks", AciConstants.PARAM_OPEN_LINKS);
        assertEquals("Output", AciConstants.PARAM_OUTPUT);
        assertEquals("Refresh", AciConstants.PARAM_REFRESH);
        assertEquals("ResponseFormat", AciConstants.PARAM_RESPONSE_FORMAT);
        assertEquals("Tail", AciConstants.PARAM_TAIL);
        assertEquals("Template", AciConstants.PARAM_TEMPLATE);
        assertEquals("TemplateParamsCSVs", AciConstants.PARAM_TEMPLATE_PARAMS_CSVS);
    }

}
