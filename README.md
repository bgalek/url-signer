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
- a link containing parameters that should not be tempered with
- a signed link, that can be checked if it was issued by the author
- time expiring links that cannot be extended simply by editng link

## Usage
Add library dependency:
```groovy
compile "com.github.bgalek.utils:url-signer:1.0.1"
```

Add checksum to url using SHA256 signature:
```java
UrlSigner signer = new SHA256ChecksumUrlSigner().sign(URI.create("https://github.com"))
signer.verify("https://github.com?checksum=996e1f714b08e971ec79e3bea686287e66441f043177999a13dbc546d8fe402a")
```

Sign url using:
```java
UrlSigner signer = new HMACChecksumUrlSigner(HmacAlgorithms.HMAC_MD5, "secret").sign(URI.create("https://github.com"))
signer.verify("https://github.com?checksum=996e1f714b08e971ec79e3bea686287e66441f043177999a13dbc546d8fe402a")
```

Expiring url: 
```java
UrlSigner signer = new TimeExpirationUrlSigner(Duration.ofMinutes(15), clock)
urlSigner.verify("https://github.com?checksum=8d7bdc5fe9dd7791a9dda4c78621bfea")
```

## Extending/Customization

Simply implement UrlSigner interface to create 
Your own signature/verification algorithm:

To generate url signed
using 3 stars like `https://github.com?signature=★★★` you can use:

```java
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

```

