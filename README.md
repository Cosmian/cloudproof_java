# Cloudproof Java Library

![workflow](https://github.com/Cosmian/cloudproof_java/actions/workflows/maven.yml/badge.svg)

The Cloudproof Java library provides a Java-friendly API to the [Cosmian Cloudproof Encryption product](https://docs.cosmian.com/cloudproof_encryption/use_cases_benefits/).

In summary, Cloudproof Encryption product secures data repositories in the cloud with attributes-based access control encryption and encrypted search.

## Getting started

Please [check the online documentation](https://docs.cosmian.com/cloudproof_encryption/use_cases_benefits/) for details on using the CloudProof APIs

In addition, please have a look at the following tests for implementation examples:

- [TestCoverCrypt](./src/test/java/com/cosmian/TestCoverCrypt.java) for using the CoverCrypt scheme with Cosmian KMS
- [TestFfiCoverCrypt](./src/test/java/com/cosmian/TestFfiCoverCrypt.java) for using the CoverCrypt scheme with the local native library
- [TestGpsw](./src/test/java/com/cosmian/TestAbe.java) for using the ABE GPSW scheme with Cosmian KMS
- [TestFfiGpsw](./src/test/java/com/cosmian/TestFfiAbe.java) for using the ABE GPSW scheme with the local native library
- [TestKmip](./src/test/java/com/cosmian/TestKmip.java) for using the KMIP 2.1 interface with the Cosmian KMS
- [TestFfiFindex](./src/test/java/com/cosmian/TestFfiFindex.java) for using the SSE Findex scheme with the local native library

## Using in Java projects

This library is free software and is available on Maven Central

```xml
<dependency>
    <groupId>com.cosmian</groupId>
    <artifactId>cloudproof_java</artifactId>
    <version>1.11.1</version>
</dependency>
```

## Versions Correspondence

When using local encryption and decryption with [GPSW](https://github.com/Cosmian/abe_gpsw) and [CoverCrypt](https://github.com/Cosmian/cover_crypt), native libraries are required.

Check the main pages of the respective projects to build the native librairies appropriate for your systems. The [test directory](./src/test/resources/linux-x86-64/) provides pre-built libraries for Linux GLIBC 2.17. These librairies should run fine on a system with a more recent GLIBC version.

This table shows the minimum versions correspondences between the various components

| KMS Server | Java Lib  | GPSW lib  | CoverCrypt lib | Findex |
| ---------- | --------- | --------- | -------------- | ------ |
| 1.2.0      | 0.5.0     | 0.3.0     | N/A            | N/A    |
| 1.2.1      | 0.5.2     | 0.4.0     | N/A            | N/A    |
| 1.2.1      | ~~0.6.0~~ | ~~0.6.0~~ | N/A            | N/A    |
| 1.2.1      | 0.6.1     | 0.6.1     | N/A            | N/A    |
| 2.0.1      | 0.7.2     | 0.6.5     | 1.0.2          | N/A    |
| 2.0.2      | 0.7.5     | 0.6.10    | 2.0.0          | N/A    |
| 2.1.0      | 0.7.7     | 1.1.1     | 4.0.0          | 0.2.3  |
| 2.2.0      | 0.8.0     | 2.0.1     | 6.0.1          | 0.4.1  |
| 2.2.0      | 0.9.0     | 2.0.1     | 6.0.1          | 0.5.0  |
| 2.2.0      | 0.10.1    | 2.0.1     | 6.0.1          | 0.6.1  |
| 2.2.0      | 0.11.0    | 2.0.1     | 6.0.1          | 0.7.0  |
| 2.3.0      | 1.11.0    | 2.0.1     | 6.0.1          | 0.7.0  |

## Update native libraries

The Cloudproof Java lib uses JNA to access functions of 3 native shared libraries:

- `CoverCrypt`
- `ABE GPSW`
- `Findex`

On Linux, those libraries must be found in 1 of these folders:

- src/resources/linux-x86-64
- src/test/resources/linux-x86-64

To update those native libraries, the script `src/test/resources/linux-x86-64/update_native_libraries.sh`
