package com.github.bgalek.utils.urlsigner;

import org.apache.commons.codec.digest.DigestUtils;

import java.net.URI;

public class SHA256ChecksumUrlSigner implements HashChecksumUrlSigner {
    public Checksum createChecksum(URI uri) {
        return Checksum.valueOf(DigestUtils.sha256Hex(uri.toString()));
    }
}
