# Cloudproof Java Library

![workflow](https://github.com/Cosmian/cloudproof_java/actions/workflows/maven.yml/badge.svg)

The Cloudproof Java library provides a Java-friendly API to [Cosmian's Cloudproof Encryption](https://docs.cosmian.com/cloudproof_encryption/use_cases_benefits/).

Cloudproof Encryption secures data repositories and applications in the cloud with advanced application-level encryption and encrypted search.

<!-- toc -->

- [Getting started](#getting-started)
- [Using in Java projects](#using-in-java-projects)
- [Versions Correspondence](#versions-correspondence)
- [Updating the native libraries](#updating-the-native-libraries)

<!-- tocstop -->

## Licensing

The library is available under a dual licensing scheme Affero GPL/v3 and commercial. See [LICENSE.md](LICENSE.md) for details.

## Getting started

Please [check the online documentation](https://docs.cosmian.com/cloudproof_encryption/use_cases_benefits/) for details on using the CloudProof APIs

In addition, please have a look at the following tests for implementation examples:

- [TestCoverCrypt](./src/test/java/com/cosmian/TestKmsCoverCrypt.java) for using the CoverCrypt scheme with Cosmian KMS
- [TestFfiCoverCrypt](./src/test/java/com/cosmian/TestNativeCoverCrypt.java) for using the CoverCrypt scheme with the local native library
- [TestKmip](./src/test/java/com/cosmian/TestKmip.java) for using the KMIP 2.1 interface with the Cosmian KMS
- [TestSqliteFindex](./src/test/java/com/cosmian/findex/TestSqlite.java) for using the Encrypted Search Findex scheme using Sqlite (or other SQL DBs) as a backend
- [TestRedisFindex](./src/test/java/com/cosmian/findex/TestRedis.java) for using the Encrypted Search Findex scheme using Redis as a backend

## Using in Java projects

This library is open-source software and is available on Maven Central.

```xml
<dependency>
    <groupId>com.cosmian</groupId>
    <artifactId>cloudproof_java</artifactId>
    <version>3.0.0</version>
</dependency>
```

## Versions Correspondence

This table shows the compatible versions of the various components

| This lib | KMS Server | CoverCrypt | Findex |
| -------- | ---------- | ---------- | ------ |
| 3.0.0    | 4.0.1      | 8.0.1      | 0.12.0 |

## Updating the native libraries

The Cloudproof Java lib uses JNA to access functions of the 2 native cryptographic libraries:

- `CoverCrypt`
- `Findex`

Those libraries must be found either in the classpath or in subfolders of `src/resources/`

- src/resources/
  - `linux-x86-64` for Linux 64bit architecture
  - `darwin-x86-64` for MacOS Intel
  - `win32-x86-64` for windows

For tests, it is possible to override these libraries by placing them in the equivalent sub-folders of `src/test/resources`

Please check the CoverCrypt and Findex projects on Github.
Their `build` directory contains instructions on how to build the native libraries for your system.
