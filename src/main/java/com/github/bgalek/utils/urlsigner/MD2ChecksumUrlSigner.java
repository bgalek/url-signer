package com.github.bgalek.utils.urlsigner;

import java.net.URI;

/** {@link HashChecksumUrlSigner} using the MD2 digest algorithm. */
public class MD2ChecksumUrlSigner implements HashChecksumUrlSigner {

    /** Creates a new instance. */
    public MD2ChecksumUrlSigner() {}

    @Override
    public Checksum createChecksum(URI uri) {
        return Checksum.valueOf(HashChecksumUrlSigner.digestHex("MD2", uri.toString()));
    }
}
