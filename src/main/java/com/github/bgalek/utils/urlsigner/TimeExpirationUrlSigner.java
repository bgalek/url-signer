package com.github.bgalek.utils.urlsigner;

import org.springframework.util.DigestUtils;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class TimeExpirationUrlSigner implements HashChecksumUrlSigner {

    private final Duration duration;
    private final Clock clock;
    private final Instant signedAt;

    public TimeExpirationUrlSigner(Duration duration, Clock clock) {
        this.duration = duration;
        this.clock = clock;
        this.signedAt = clock.instant();
    }

    public TimeExpirationUrlSigner(Duration duration) {
        this(duration, Clock.systemUTC());
    }

    @Override
    public Checksum createChecksum(URI uri) {
        final List<String> checksumComponents = List.of(
                Boolean.toString(clock.instant().isBefore(signedAt.plus(duration))),
                uri.toString()
        );
        return Checksum.valueOf(DigestUtils.md5DigestAsHex(String.join("", checksumComponents).getBytes()));
    }
}
