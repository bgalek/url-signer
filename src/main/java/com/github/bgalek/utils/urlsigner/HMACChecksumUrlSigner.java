package com.github.bgalek.utils.urlsigner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * {@link HashChecksumUrlSigner} that uses an HMAC algorithm and a shared secret to produce a tamper-proof checksum.
 *
 * @see javax.crypto.Mac
 */
public class HMACChecksumUrlSigner implements HashChecksumUrlSigner {

    private final String algorithm;
    private final String secret;

    HMACChecksumUrlSigner(String algorithm, String secret) {
        this.algorithm = algorithm;
        this.secret = secret;
    }

    @Override
    public Checksum createChecksum(URI uri) {
        return Checksum.valueOf(hmacHex(algorithm, secret, uri.toString()));
    }

    private static String hmacHex(String algorithm, String secret, String data) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(keySpec);
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC algorithm not available: %s".formatted(algorithm), e);
        }
    }
}
