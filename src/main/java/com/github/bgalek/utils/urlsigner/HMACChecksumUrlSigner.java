package com.github.bgalek.utils.urlsigner;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

import java.net.URI;

public class HMACChecksumUrlSigner implements HashChecksumUrlSigner {

    private final HmacUtils hmacUtils;

    HMACChecksumUrlSigner(HmacAlgorithms hmacAlgorithm, String secret) {
        hmacUtils = new HmacUtils(hmacAlgorithm, secret);
    }

    @Override
    public Checksum createChecksum(URI uri) {
        return Checksum.valueOf(hmacUtils.hmacHex(uri.toString()));
    }
}
