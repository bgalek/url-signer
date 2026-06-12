package com.github.bgalek.utils.urlsigner;

import java.net.URI;

/** {@link HashChecksumUrlSigner} using the SHA-384 digest algorithm. */
public class SHA384ChecksumUrlSigner implements HashChecksumUrlSigner {

    /** Creates a new instance. */
    public SHA384ChecksumUrlSigner() {}

    @Override
    public Checksum createChecksum(URI uri) {
        return Checksum.valueOf(HashChecksumUrlSigner.digestHex("SHA-384", uri.toString()));
    }
}
