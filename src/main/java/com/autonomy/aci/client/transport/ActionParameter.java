package com.autonomy.aci.client.transport;

import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.nio.charset.Charset;

/**
 * Interface representing an ACI parameter to be sent as part of an ACI request.
 *
 * @param <T> The type of the parameter value
 */
public interface ActionParameter<T> {

    /**
     * @return The parameter name
     */
    String getName();

    /**
     * @return The parameter value
     */
    T getValue();

    /**
     * Adds the parameter to a multipart entity, using the given character encoding if appropriate
     * @param builder The entity builder used to construct the entity
     * @param charset The character encoding to use for the part
     */
    void addToEntity(MultipartEntityBuilder builder, final Charset charset);

    /**
     * @return True if the parameter requires a post request (e.g. it is of type InputStream)
     */
    boolean requiresPostRequest();

}
