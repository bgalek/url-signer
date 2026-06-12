package com.github.bgalek.utils.urlsigner;

import java.net.URI;

/** {@link HashChecksumUrlSigner} using the MD5 digest algorithm. */
public class MD5ChecksumUrlSigner implements HashChecksumUrlSigner {

    /** Creates a new instance. */
    public MD5ChecksumUrlSigner() {}

    @Override
    public Checksum createChecksum(URI uri) {
        return Checksum.valueOf(HashChecksumUrlSigner.digestHex("MD5", uri.toString()));
    }
}
