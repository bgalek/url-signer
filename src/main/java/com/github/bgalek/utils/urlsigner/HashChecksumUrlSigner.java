package com.github.bgalek.utils.urlsigner;

import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.web.util.UriComponentsBuilder.fromUri;

public interface HashChecksumUrlSigner extends UrlSigner {

    String SIGNATURE_PARAMETER_NAME = "checksum";

    Checksum createChecksum(URI uri);

    default Checksum getChecksum(URI uri) {
        return Optional.ofNullable(fromUri(uri)
                .build()
                .getQueryParams()
                .getFirst(signatureParameterName()))
                .map(Checksum::valueOf)
                .orElse(Checksum.empty());
    }

    @Override
    default URI sign(URI uri) {
        final String signatureParameterName = signatureParameterName();
        return fromUri(uri)
                .replaceQueryParam(signatureParameterName, Collections.emptyList())
                .queryParam(signatureParameterName, createChecksum(uri))
                .build()
                .toUri();
    }

    @Override
    default boolean verify(URI uri) {
        final UriComponentsBuilder uriComponentsBuilder = fromUri(uri);
        uriComponentsBuilder.replaceQueryParam(signatureParameterName(), Collections.emptyList());
        final URI sourceURI = uriComponentsBuilder.build().toUri();
        return createChecksum(sourceURI).equals(getChecksum(uri));
    }

    @Override
    default String signatureParameterName() {
        return SIGNATURE_PARAMETER_NAME;
    }

    class Checksum {

        private final String[] value;

        private Checksum(String[] value) {
            this.value = value;
        }

        static Checksum empty() {
            return new Checksum(null);
        }

        static Checksum valueOf(String... parameters) {
            return new Checksum(parameters);
        }

        @Override
        public String toString() {
            return String.join("-", value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Checksum checksum = (Checksum) o;
            return Arrays.equals(value, checksum.value);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(value);
        }
    }
}
