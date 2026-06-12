package com.github.bgalek.utils.urlsigner;

import org.jspecify.annotations.Nullable;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.springframework.web.util.UriComponentsBuilder.fromUri;

/**
 * {@link UrlSigner} that appends a hash-based checksum query parameter to the URL.
 */
public interface HashChecksumUrlSigner extends UrlSigner {

    /** Name of the query parameter that carries the checksum. */
    String SIGNATURE_PARAMETER_NAME = "checksum";

    /**
     * Computes the checksum for the given URI.
     * Implementations define the hashing algorithm; the URI must not contain the checksum parameter.
     *
     * @param uri the URI to checksum, without the signature parameter
     * @return the computed checksum
     */
    Checksum createChecksum(URI uri);

    /**
     * Extracts the checksum from the URI's query parameters.
     *
     * @param uri the URI to inspect
     * @return the checksum present in the URI, or an empty checksum if the parameter is absent
     */
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
        String signatureParameterName = signatureParameterName();
        return fromUri(uri)
                .replaceQueryParam(signatureParameterName, List.of())
                .queryParam(signatureParameterName, createChecksum(uri))
                .build()
                .toUri();
    }

    @Override
    default boolean verify(URI uri) {
        UriComponentsBuilder uriComponentsBuilder = fromUri(uri);
        uriComponentsBuilder.replaceQueryParam(signatureParameterName(), List.of());
        URI sourceURI = uriComponentsBuilder.build().toUri();
        return createChecksum(sourceURI).equals(getChecksum(uri));
    }

    @Override
    default String signatureParameterName() {
        return SIGNATURE_PARAMETER_NAME;
    }

    /**
     * Returns the lowercase hex digest of {@code input} using the given JCA algorithm name.
     *
     * @param algorithm JCA algorithm name (e.g. {@code "SHA-256"}, {@code "MD5"})
     * @param input     the string to hash, encoded as UTF-8
     * @return lowercase hex-encoded digest
     * @throws IllegalStateException if the algorithm is not available in the current JVM
     */
    static String digestHex(String algorithm, String input) {
        try {
            byte[] hash = MessageDigest.getInstance(algorithm).digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithm not available: %s".formatted(algorithm), e);
        }
    }

    /**
     * Opaque checksum value appended to a signed URL.
     * An empty checksum (no parameter present) never equals a real one, so verification fails safely.
     *
     * @param value the raw checksum string, or {@code null} for an empty/absent checksum
     */
    record Checksum(@Nullable String value) {

        static Checksum empty() {
            return new Checksum(null);
        }

        static Checksum valueOf(String... parameters) {
            return new Checksum(String.join("-", parameters));
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
