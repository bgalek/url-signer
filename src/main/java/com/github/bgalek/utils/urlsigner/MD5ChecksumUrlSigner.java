package com.github.bgalek.utils.urlsigner;

import org.apache.commons.codec.digest.DigestUtils;

import java.net.URI;

public class MD5ChecksumUrlSigner implements HashChecksumUrlSigner {
    public Checksum createChecksum(URI uri) {
        return Checksum.valueOf(DigestUtils.md5Hex(uri.toString()));
    }
}
