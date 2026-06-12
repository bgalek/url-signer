package com.github.bgalek.utils.urlsigner;

import java.net.URI;

/** {@link HashChecksumUrlSigner} using the SHA3-224 digest algorithm. */
public class SHA3_224ChecksumUrlSigner implements HashChecksumUrlSigner {

    /** Creates a new instance. */
    public SHA3_224ChecksumUrlSigner() {}

    @Override
    public Checksum createChecksum(URI uri) {
        return Checksum.valueOf(HashChecksumUrlSigner.digestHex("SHA3-224", uri.toString()));
    }
}
