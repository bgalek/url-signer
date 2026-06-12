package com.github.bgalek.utils.urlsigner;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Signs URLs with a tamper-evident signature and verifies that a given URL has not been modified since signing.
 */
public interface UrlSigner {

    /**
     * Signs the given URI by appending a signature parameter.
     *
     * @param uri the URI to sign
     * @return a new URI with the signature parameter appended
     */
    URI sign(URI uri);

    /**
     * Verifies that the URI has not been tampered with since it was signed.
     *
     * @param uri the URI to verify
     * @return {@code true} if the signature is valid, {@code false} otherwise
     */
    boolean verify(URI uri);

    /**
     * Returns the name of the query parameter used to carry the signature.
     *
     * @return the signature parameter name
     */
    String signatureParameterName();

    /**
     * Signs the given URI string.
     *
     * @param uri the URI string to sign
     * @return a new URI with the signature parameter appended
     * @see #sign(URI)
     */
    default URI sign(String uri) {
        return sign(URI.create(uri));
    }

    /**
     * Signs the given URL.
     *
     * @param url the URL to sign
     * @return a new URI with the signature parameter appended
     * @throws URISyntaxException if the URL cannot be converted to a URI
     * @see #sign(URI)
     */
    default URI sign(URL url) throws URISyntaxException {
        return sign(url.toURI());
    }

    /**
     * Verifies that the URI string has not been tampered with since it was signed.
     *
     * @param uri the URI string to verify
     * @return {@code true} if the signature is valid, {@code false} otherwise
     * @see #verify(URI)
     */
    default boolean verify(String uri) {
        return verify(URI.create(uri));
    }
}
