# Cosmian Java Lib


The library provides a Java friendly API to the Cosmian Ubiquitous Encryption platform:

 - perform [Confidential Data Access](#confidential-data-access) advanced encryption routines 
 - build and run [Confidential Micro Services](#confidential-micro-services) on the Cosmian Confidential Cloud 
 - managed keys with the [Cosmian Confidential Key Management Service (KMS)](#confidential-kms) 


:warning: This is an early release of the java library for Cosmian Ubiquitous Encryption. Only a limited set of the operations is publicly supported.


  - [Confidential Data Access](#confidential-data-access)
    - [Quick Start](#quick-start)
    - [Local encryption and decryption](#local-encryption-and-decryption)
  - [Confidential Micro Services](#confidential-micro-services)
  - [Confidential KMS](#confidential-kms)


This library is available on Maven Central

```xml
<dependency>
    <groupId>com.cosmian</groupId>
    <artifactId>cosmian_java_lib</artifactId>
    <version>0.3.3</version>
</dependency>
```

## Confidential Data Access

Cosmian Ubiquitous Encryption provides the ability to encrypt data - locally or inside the KMS -  using policy attributes. The only users able to decrypt the data are those possessing a key holding the correct access policy.

Attributes Based Encryption (ABE) allows building secure data lakes, repositories, directories... of data in zero trust environments, such as the cloud, where users and client applications can only read the data they are allowed to access.

In addition, Cosmian Confidential Data Access allows building secure indexes on the data, to efficiently search the encrypted data, without the cloud learning anything about the search query, the response or the underlying data itself.

### Quick Start

Head for [demo.java](./src/test/java/com/cosmian/Demo.java). 

This demo creates a Policy which combines two policy axes, 

 - a hierarchical policy axis `Security Level: Protected, Low Secret,..., Top Secret` 
 - and a non hierarchical axis `Department: MKG, FIN, HR,...`. 
 
Data is encrypted with two values, one from each axis, say: `[MKG, Low Secret]`

A user is able to decrypt data only if it possesses a key with an access policy with sufficient security level and the code for the department, say ` Top Secret && ( MKG || FIN )`


### Local encryption and decryption

In addition to KMS encryption and decryption, the library offers the ability to perform and hybrid encryption ABE+AES. This requires having the native dynamic library [abe_gpsw](https://github.com/Cosmian/abe_gpsw) deployed on your system.

The native library can also be packaged as part of the jar package by placing a copy in the `src/main/resources/{OS}-{ARCH}` folder (`linux-amd64` for linux) and running `mvn package`.

To learn how to use the local ABE+AES hybrid encryption facilities, check [the Hybrid ABE AES tests](src/test/java/com/cosmian/TestLocalABE_AES.java)

## Confidential Micro Services

*Not publicly available yet. Call Cosmian for early access*

Cosmian Confidential Micro Services allows building micro services in Python (soon Java) that can be deployed on the Cosmian Confidential Cloud. 

The code, the data and the results are encrypted at all times, so the Cosmian Cloud does not learn anything about the data or the algorithm. 

Immediate benefits:

 - on-premise confidential data computing can now be safely delegated to the cloud
 - SaaS providers can now offer their services while keeping their customers data private and protected at all times

Also, data sources, code and results can be encrypted under different keys enabling new collaborative confidential computing scenarios:

- two companies wanting to learn their common customers without revealing to each other their full customers list.
- a FinTech having developed a breakthrough algorithm that needs to be run on a bank data. However the FinTech does not want to share its algorithm  with the bank in clear text, and the bank does not want to share its data with the FinTech in clear text either. 


## Confidential KMS

Cosmian offers a confidential KMS in the Cosmian Confidential Cloud. The KMS operations are protected with the same technology used for the Confidential Micro Services, so Cosmian never learns anything about the keys stored in the KMS or the operations performed with those keys inside the KMS (encryption, decryption, signature,...).

Use of Cosmian KMS is included with the services above.

The KMS offers a KMIP 2.1 interface.

*Only the KMS operations required to enable the Confidential Data Access and Confidential Micro Services are publicly available for now. Contact Cosmian for full KMS access*
