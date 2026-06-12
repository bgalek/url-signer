package com.github.bgalek.utils.urlsigner;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.util.List;

import static org.springframework.web.util.UriComponentsBuilder.fromUri;

/**
 * {@link HashChecksumUrlSigner} that embeds a per-URL expiry timestamp and authenticates it with HMAC-SHA256.
 *
 * <p>The expiry epoch second is appended as {@value #EXPIRY_PARAMETER_NAME} before signing, so the HMAC covers
 * the timestamp — an attacker cannot extend the TTL without knowing the secret.
 * Each {@link #sign} call captures the current time independently, making this class safe to use as a singleton bean.
 */
public class TimeExpirationUrlSigner implements HashChecksumUrlSigner {

    /** Name of the query parameter that carries the expiry epoch second. */
    static final String EXPIRY_PARAMETER_NAME = "expires";

    private final Duration duration;
    private final Clock clock;
    private final HMACChecksumUrlSigner hmacSigner;

    /**
     * Creates a new instance with a custom clock (useful for testing).
     *
     * @param duration how long a signed URL remains valid
     * @param secret   HMAC-SHA256 signing secret
     * @param clock    clock used to determine the current time
     */
    public TimeExpirationUrlSigner(Duration duration, String secret, Clock clock) {
        this.duration = duration;
        this.clock = clock;
        this.hmacSigner = new HMACChecksumUrlSigner("HmacSHA256", secret);
    }

    /**
     * Creates a new instance using the system UTC clock.
     *
     * @param duration how long a signed URL remains valid
     * @param secret   HMAC-SHA256 signing secret
     */
    public TimeExpirationUrlSigner(Duration duration, String secret) {
        this(duration, secret, Clock.systemUTC());
    }

    @Override
    public URI sign(URI uri) {
        URI uriWithExpiry = fromUri(uri)
                .replaceQueryParam(EXPIRY_PARAMETER_NAME, List.of())
                .replaceQueryParam(signatureParameterName(), List.of())
                .queryParam(EXPIRY_PARAMETER_NAME, clock.instant().plus(duration).getEpochSecond())
                .build()
                .toUri();
        return HashChecksumUrlSigner.super.sign(uriWithExpiry);
    }

    @Override
    public boolean verify(URI uri) {
        if (!HashChecksumUrlSigner.super.verify(uri)) return false;
        String expiresParam = fromUri(uri).build().getQueryParams().getFirst(EXPIRY_PARAMETER_NAME);
        if (expiresParam == null) return false;
        try {
            return clock.instant().getEpochSecond() < Long.parseLong(expiresParam);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public Checksum createChecksum(URI uri) {
        return hmacSigner.createChecksum(uri);
    }
}
