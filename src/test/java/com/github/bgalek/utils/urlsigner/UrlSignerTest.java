package com.github.bgalek.utils.urlsigner;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UrlSignerTest {

    @ParameterizedTest
    @MethodSource("provideUrlSignerImplementation")
    @DisplayName("should sign url using provided checksum implementation")
    void hashChecksumUrlSignerTest(UrlSigner urlSigner, String checksum) {
        //when
        URI signed = urlSigner.sign(URI.create("https://github.com"));

        //then
        assertEquals(String.format("https://github.com?checksum=%s", checksum), signed.toString());
        assertTrue(urlSigner.verify(String.format("https://github.com?checksum=%s", checksum)));
        assertFalse(urlSigner.verify(String.format("https://github.com?checksum=3%s0", checksum)));
        assertFalse(urlSigner.verify("https://github.com"));
    }

    private static Stream<Arguments> provideUrlSignerImplementation() {
        return Stream.of(
                Arguments.of(new MD2ChecksumUrlSigner(), "3f2661f43ec46d5c242fcd22ab34d1d8"),
                Arguments.of(new MD5ChecksumUrlSigner(), "3097fca9b1ec8942c4305e550ef1b50a"),
                Arguments.of(new SHA1ChecksumUrlSigner(), "84b7e44aa54d002eac8d00f5bfa9cc93410f2a48"),
                Arguments.of(new SHA256ChecksumUrlSigner(), "996e1f714b08e971ec79e3bea686287e66441f043177999a13dbc546d8fe402a"),
                Arguments.of(new SHA384ChecksumUrlSigner(), "ef5d7eda295e4f5eade267b60062428d01d0d9a0ee1a17fedc319a41fd188931bf838b470f171f544798508ac4323f3d"),
                Arguments.of(new SHA512ChecksumUrlSigner(), "44679c4abfe5ecb67d21e6069ade2745f7607b1ae2d8bf8ad245994917331e8539689f43d2aa0f445e4b2f86875742c751570f6550006673a0a2edbbd8877fb9"),
                Arguments.of(new SHA3_224ChecksumUrlSigner(), "587834d4d372b1c4b2a4bf04a6cc061e77f6d4bddd1371b4235ce9b8"),
                Arguments.of(new SHA3_256ChecksumUrlSigner(), "dfcb585678a42ddd3252c770eb68d34cca87d4873f56ba2d25cdfabb2a1834f3"),
                Arguments.of(new SHA3_384ChecksumUrlSigner(), "0507c6a36d99e6adca66c7dacf9d3eb128b3ff6c39f301bcd4b64c26a27d4d833781205a79f828941161a32c9a44570e"),
                Arguments.of(new SHA3_512ChecksumUrlSigner(), "7cd644cb57c300dfafb75c95e1fef58a846991ce561e8635d86ef7e41e04c17d4096b948acde40410b02e75e99bc92081689e6c8ebd00aecca76bf66e45f6a02"),
                Arguments.of(new HMACChecksumUrlSigner(HmacAlgorithms.HMAC_MD5, "secret"), "8cf5ce416077c41d40275db6577c0273")
        );
    }

    @Test
    @DisplayName("should allow users to create their own signing algorithms easily")
    void customTest() {
        //given
        UrlSigner urlSigner = new UrlSigner() {
            @Override
            public URI sign(URI uri) {
                return UriComponentsBuilder.fromUri(uri)
                        .replaceQueryParam(signatureParameterName(), Collections.emptyList())
                        .replaceQueryParam(signatureParameterName(), "★★★")
                        .build()
                        .toUri();
            }

            @Override
            public boolean verify(URI uri) {
                return uri.toString().contains("★★★");
            }

            @Override
            public String signatureParameterName() {
                return "signature";
            }
        };

        //when
        URI signed = urlSigner.sign(URI.create("https://github.com"));

        //then
        assertEquals("https://github.com?signature=★★★", signed.toString());
        assertTrue(urlSigner.verify("https://github.com?signature=★★★"));
        assertFalse(urlSigner.verify("https://github.com?signature=☆☆☆️"));
        assertFalse(urlSigner.verify("https://github.com"));
    }

    @Test
    @DisplayName("should sign url with expiration time")
    void expirationTest() {
        //given
        TestClock clock = new TestClock(Instant.now(), ZoneOffset.UTC);
        UrlSigner urlSigner = new TimeExpirationUrlSigner(Duration.ofMinutes(15), clock);

        //when
        urlSigner.sign(URI.create("https://github.com"));

        //then
        assertTrue(urlSigner.verify("https://github.com?checksum=8d7bdc5fe9dd7791a9dda4c78621bfea"));

        //when
        clock.fastForward(Duration.ofHours(100));

        //then
        assertFalse(urlSigner.verify("https://github.com?checksum=8d7bdc5fe9dd7791a9dda4c78621bfea"));
    }

    static class TestClock extends Clock {

        private Clock clock;

        TestClock(Instant instant, ZoneId zoneId) {
            this.clock = Clock.fixed(instant, zoneId);
        }

        public void fastForward(Duration duration) {
            this.clock = Clock.fixed(clock.instant().plus(duration), clock.getZone());
        }

        @Override
        public long millis() {
            return clock.millis();
        }

        @Override
        public ZoneId getZone() {
            return clock.getZone();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return clock.withZone(zone);
        }

        @Override
        public Instant instant() {
            return clock.instant();
        }
    }
}
