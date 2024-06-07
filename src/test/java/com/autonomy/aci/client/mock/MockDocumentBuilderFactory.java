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

package com.autonomy.aci.client.mock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Mock implementation of <code>DocumentBuilderFactory</code> that implements all the abstract methods. <code>newDocumentBuilder()</code>
 * throws a <code> ParserConfigurationException</code> all the rest throw <code>UnsupportedOperationException</code>.
 */
@SuppressWarnings("unused") // Referenced by classpath in DocumentProcessorTest
public class MockDocumentBuilderFactory extends DocumentBuilderFactory {

    @Override
    public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        throw new ParserConfigurationException("JUnit mock object.");
    }
    
    @Override
    public void setAttribute(final String name, final Object value) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public Object getAttribute(final String name) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void setFeature(final String name, final boolean value) throws ParserConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public boolean getFeature(final String name) throws ParserConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
