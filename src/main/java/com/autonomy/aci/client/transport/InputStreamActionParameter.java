package com.autonomy.aci.client.transport;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * {@link ActionParameter} for a sequence of {@link InputStream}s that should be uploaded under a single parameter name.
 * For example, training images to be sent to ImageServer.
 */
public class InputStreamActionParameter implements ActionParameter<List<InputStream>> {

    private final String name;
    private final List<InputStream> value;

    /**
     * Creates an InputStreamActionParameter representing a single stream
     * @param name The name of the parameter
     * @param value The stream representing the value of the parameter
     */
    public InputStreamActionParameter(final String name, final InputStream value) {
        this(name, Collections.singletonList(value));
    }

    /**
     * Creates an InputStreamActionParameter representing multiple streams
     * @param name The name of the parameter
     * @param value The streams representing the value of the parameter
     */
    public InputStreamActionParameter(final String name, final List<InputStream> value) {
        if(StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Parameter names cannot be null.");
        }

        this.name = name;
        this.value = value;
    }

    /**
     * @return The name of the parameter
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @return The stream values of the parameter
     */
    @Override
    public List<InputStream> getValue() {
        return value;
    }

    /**
     * Add
     * @param builder The entity builder used to construct the entity
     * @param charset The character encoding to use for the part
     */
    @Override
    public void addToEntity(final MultipartEntityBuilder builder, final Charset charset) {
        for(final InputStream inputStream : value) {
            builder.addBinaryBody(name, inputStream);
        }
    }

    /**
     * Always returns true as {@code InputStreamActionParameter}s are always assumed to require post requests
     *
     * @return True
     */
    @Override
    public boolean requiresPostRequest() {
        return true;
    }

    /**
     * An {@code InputStreamActionParameter} is deemed to be equal to another if the name of the parameter is the same. This means
     * that you will be unable to add another parameter with the same name to the {@code Set} to be sent when an action
     * is executed, if one of the same name already exists in the {@code Set}.
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {
        boolean returnValue = false;

        if (this == obj) {
            returnValue = true;
        } else if (obj instanceof InputStreamActionParameter) {
            final InputStreamActionParameter that = (InputStreamActionParameter) obj;

            // Check the name of this parameter with the passed in parameter...
            returnValue = this.name.toLowerCase(Locale.ENGLISH).equals(that.name.toLowerCase(Locale.ENGLISH));
        }

        return returnValue;
    }

    /**
     * Returns a hash code value for the object.
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return name.toLowerCase(Locale.ENGLISH).hashCode();
    }
}
