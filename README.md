# Java solution for url signing
> Easy way to ensure that link was not tempered with

[![Build](https://github.com/bgalek/url-signer/actions/workflows/ci.yml/badge.svg?style=flat-square)](https://github.com/bgalek/url-signer/actions/workflows/ci.yml)
[![codecov](https://codecov.io/github/bgalek/url-signer/graph/badge.svg?token=VDUL4P2CLF)](https://codecov.io/github/bgalek/url-signer)
![GitHub Release Date](https://img.shields.io/github/release-date/bgalek/url-signer.svg?style=flat-square)
![Maven Central](https://img.shields.io/maven-central/v/com.github.bgalek.utils/url-signer?style=flat-square)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=bgalek_url-signer&metric=alert_status)](https://sonarcloud.io/dashboard?id=bgalek_url-signer)

## Why?
I can think about several use cases:

- a disposable link, that stops working after user clicks it
- a link containing parameters that should not be tampered with
- a signed link, that can be checked if it was issued by the author
- time-expiring links that cannot be extended simply by editing the link

## Usage
Add library dependency:
```groovy
implementation "com.github.bgalek.utils:url-signer:1.1.0"
```

Sign a URL using SHA-256:
```java
UrlSigner signer = new SHA256ChecksumUrlSigner();
URI signed = signer.sign(URI.create("https://github.com"));
// https://github.com?checksum=996e1f714b08e971ec79e3bea686287e66441f043177999a13dbc546d8fe402a

signer.verify(signed); // true
```

Sign a URL using HMAC (tamper-proof — requires a secret):
```java
UrlSigner signer = new HMACChecksumUrlSigner("HmacSHA256", "my-secret");
URI signed = signer.sign(URI.create("https://github.com"));
signer.verify(signed); // true
```

Sign a URL with a per-URL expiry (safe as a singleton bean):
```java
UrlSigner signer = new TimeExpirationUrlSigner(Duration.ofMinutes(15), "my-secret");
URI signed = signer.sign(URI.create("https://github.com"));
// https://github.com?expires=1234567890&checksum=<hmac-sha256>

signer.verify(signed); // true — before TTL expires
// ... 15 minutes later ...
signer.verify(signed); // false — expired
```

The `expires` parameter is covered by the HMAC, so an attacker cannot extend the TTL without knowing the secret.

## Extending/Customization

Implement `UrlSigner` to provide your own signing algorithm:

```java
UrlSigner urlSigner = new UrlSigner() {
    @Override
    public URI sign(URI uri) {
        return UriComponentsBuilder.fromUri(uri)
                .replaceQueryParam(signatureParameterName(), List.of())
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

URI signed = urlSigner.sign(URI.create("https://github.com"));
// https://github.com?signature=★★★
```
