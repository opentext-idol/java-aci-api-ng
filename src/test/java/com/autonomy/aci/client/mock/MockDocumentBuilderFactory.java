/*
 * Copyright 2006-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.mock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Mock implementation of <tt>DocumentBuilderFactory</tt> that implements all the abstract methods. <tt>newDocumentBuilder()</tt>
 * throws a <tt> ParserConfigurationException</tt> all the rest throw <tt>UnsupportedOperationException</tt>.
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
