package com.github.bgalek.utils.urlsigner;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public interface UrlSigner {

    URI sign(URI uri);

    boolean verify(URI uri);

    String signatureParameterName();

    default URI sign(String uri) {
        return sign(URI.create(uri));
    }

    default URI sign(URL url) throws URISyntaxException {
        return sign(url.toURI());
    }

    default boolean verify(String uri) {
        return verify(URI.create(uri));
    }
}
