# Cosmian Java Lib


The library provides a Java friendly API to the Cosmian Ubiquitous Encryption platform:

 - perform [Confidential Data Access](#confidential-data-access) advanced encryption routines 
 - build and run [Secure Computations](#secure-computations) on the Cosmian Confidential Cloud 
 - managed keys with the [Cosmian Confidential Key Management Service (KMS)](#confidential-kms) 


:warning: This is the public release of the java library for Cosmian Ubiquitous Encryption. Only a limited set of the operations is currently publicly supported. Ask us for details.



This library is available on Maven Central

```xml
<dependency>
    <groupId>com.cosmian</groupId>
    <artifactId>cosmian_java_lib</artifactId>
    <version>0.6.1</version>
</dependency>
```


  - [Confidential Data Access](#confidential-data-access)
    - [Versions Correspondence](#versions-correspondence)
    - [Quick Start ABE+AES](#quick-start-abeaes)
    - [Local ABE+AES encryption and decryption](#local-abeaes-encryption-and-decryption)
      - [Building the the ABE GPSW native lib](#building-the-the-abe-gpsw-native-lib)
      - [Using the native library](#using-the-native-library)
        - [encryption](#encryption)
        - [decryption](#decryption)
      - [Locally cached encryption and decryption](#locally-cached-encryption-and-decryption)
        - [encryption](#encryption-1)
        - [decryption](#decryption-1)
  - [Secure Computations](#secure-computations)
  - [Confidential KMS](#confidential-kms)





## Confidential Data Access

Cosmian Ubiquitous Encryption provides the ability to encrypt data - locally or inside the KMS -  using policy attributes. The only users able to decrypt the data are those possessing a key holding the correct access policy.

Attributes Based Encryption (ABE) allows building secure data lakes, repositories, directories... of data in zero trust environments, such as the cloud, where users and client applications can only read the data they are allowed to access.

In addition, Cosmian Confidential Data Access allows building secure indexes on the data, to efficiently search the encrypted data, without the cloud learning anything about the search query, the response or the underlying data itself.


### Versions Correspondence

This table shows the minimum versions correspondances between the various components

KMS Server | Java Lib | abe_gpsw lib
-----------|----------|--------------
1.2.0      | 0.5.0    | 0.3.0
1.2.1      | 0.5.2    | 0.4.0
1.2.1      | ~~0.6.0~~| ~~0.6.0~~ (yanked)
1.2.1      | 0.6.1    | 0.6.1


### Quick Start ABE+AES

Head for [demo.java](./src/test/java/com/cosmian/Demo.java) which demonstrates the use of the Abe class to exercise the Cosmian KMS server to create keys, encrypt and decrypt messages.

This demo creates a Policy which combines two policy axes, 

 - a hierarchical policy axis `Security Level: Protected, Low Secret,..., Top Secret` 
 - and a non hierarchical axis `Department: MKG, FIN, HR,...`. 
 
Data is encrypted with two values, one from each axis, say: `[MKG, Low Secret]`

A user is able to decrypt data only if it possesses a key with an access policy with sufficient security level and the code for the department, say ` Top Secret && ( MKG || FIN )`


### Local ABE+AES encryption and decryption

In addition to KMS encryption and decryption, the library offers the ability to perform a local, and hence faster and more secure, hybrid encryption ABE + AES 256 GCM. This requires having the native dynamic library [abe_gpsw](https://github.com/Cosmian/abe_gpsw) deployed on the system.

#### Building the the ABE GPSW native lib

1. Rust must be installed on the system using [rustup](https://rustup.rs/)

```sh
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
```

2. Check out the [ABE GPSW library](https://github.com/Cosmian/abe_gpsw)

```sh
git clone https://github.com/Cosmian/abe_gpsw.git && \
cd abe_gpsw && \
git checkout v0.6.1
```

3. Build the native library, which will be available as `libabe_gpsw.so` (linux) or `libabe_gpsw.dylib` (macos) in the `target` directory

```sh
cargo build --release --all-features
```

  *Note for MacOS M1 machines*
  If java is running in intel x86_64 emulation mode, the library must be built for an x86_64 target, i.e.

  ```sh
  cargo build --release --all-features --target x86_64-apple-darwin
  ```

4. Place the library on the dynamic libraries path of your system, or a path indicated by `LD_LIBRARY_PATH` on Linux. Alternatively, If you are using tis library in a java project, you can place the library in 


- `src/main/resources/linux-x86-64` folder for a Linux Intel machine
- `src/main/resources/linux-amd64` folder for a Linux AMD machine
- `src/main/resources/darwin` folder for a Mac running MacOS (M1 and Intel)
- `src/main/resources/win32-x86` folder for a Windows machine (untested)

#### Using the native library

To learn how to use the local ABE+AES hybrid encryption facilities, check [the Hybrid ABE AES tests](src/test/java/com/cosmian/TestLocalABE_AES.java)

A typical workflow will be as follows

##### encryption

```java
// The data we want to encrypt/decrypt
byte[] data = "This s a test message".getBytes(StandardCharsets.UTF_8);

// A unique ID associated with this message. The unique id is used to
// authenticate the message in the AES encryption scheme.
// Typically this will be a hash of the content if it is unique, a unique
// filename or a database unique key
byte[] uid = MessageDigest.getInstance("SHA-256").digest(data);

// Access the Cosmian KMS server. 
// Change the Cosmian Server Server URL and API key as appropriate
Abe abe = new Abe(new RestClient([KMS SEVER URL], [API KEY]);

//
// Encryption
//

// Extract the Public Key with the Policy from the KMS server
PublicKey publicKey = abe.retrievePublicMasterKey([MASTER PUBLIC KEY IDENTIFIER]);

/// The policy attributes that will be used to encrypt the content. They must
/// exist in the policy associated with the Public Key
Attr[] attributes = new Attr[] { new Attr("Department", "FIN"), new Attr("Security Level", "Confidential") };

// Now generate the header which contains the ABE encryption of the randomly
// generated AES key.
// This example assumes that the Unique ID can be recovered at time of
// decryption, and is thus not stored as part of the encrypted header.
// If that is not the case check the other signature of #Ffi.encryptedHeader()
// to inject the unique id.
EncryptedHeader encryptedHeader = Ffi.encryptHeader(publicKey, attributes);

// The data can now be encrypted with the generated key
// The block number is also part of the authentication of the AES scheme
byte[] encryptedBlock = Ffi.encryptBlock(encryptedHeader.getSymmetricKey(), uid, 0, data);

// Create a full message with header+encrypted data. The length of the header
// is pre-pended.
ByteBuffer headerSize = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN)
    .putInt(encryptedHeader.getEncryptedHeaderBytes().length);
// Write the message
ByteArrayOutputStream bao = new ByteArrayOutputStream();
bao.write(headerSize.array());
bao.write(encryptedHeader.getEncryptedHeaderBytes());
bao.write(encryptedBlock);
bao.flush();
byte[] ciphertext = bao.toByteArray();
```
##### decryption

```java
//
// Decryption
//

// Extract an existing User Decryption Key from the KMS server
PublicKey publicKey = abe.retrieveUserDecryptionKey([USER DECRYPTION KEY IDENTIFIER]);

// Parse the message by first recovering the header length
int headerSize_ = ByteBuffer.wrap(ciphertext).order(ByteOrder.BIG_ENDIAN).getInt(0);
// Then recover the encrypted header and encrypted content
byte[] encryptedHeader_ = Arrays.copyOfRange(ciphertext, 4, 4 + headerSize_);
byte[] encryptedContent = Arrays.copyOfRange(ciphertext, 4 + headerSize_, ciphertext.length);

// Decrypt he header to recover the symmetric AES key
DecryptedHeader decryptedHeader = Ffi.decryptHeader(userDecryptionKey, encryptedHeader_);

// decrypt the content, passing the unique id and block number
byte[] data_ = Ffi.decryptBlock(decryptedHeader.getSymmetricKey(), uid, 0, encryptedContent);

// Verify everything is correct
assertTrue(Arrays.equals(data, data_));        
```

#### Locally cached encryption and decryption

When doing multiple encryptions/decryptions in a row using the same key, a significant speed increase can be achieved using a local cache of the keys (10x on encryption, 2.5x on decryption). 

##### encryption

```java

PublicKey publicKey = PublicKey.fromJson(publicKeyJson);
// Create a cache of the Public Key and Policy
int cacheHandle = Ffi.createEncryptionCache(publicKey);

// process multiple messages in a loop
try {
  for (...) {
    byte[] clearText = ...;
    // encrypt the hybrid header using the cache
    Attr[] attributes = ...;
    EncryptedHeader encryptedHeader = Ffi.encryptHeaderUsingCache(cacheHandle, attributes);
    // encrypt the block of bytes as before
    byte[] encryptedBlock = Ffi.encryptBlock(encryptedHeader.getSymmetricKey(), uid, 0, clearText);
  }
finally {
  // The cache should be destroyed to reclaim memory
  Ffi.destroyEncryptionCache(cacheHandle);
}

```

##### decryption

```java
PrivateKey decryptionKey = ...;
// Create a cache of the Decryption Key
int cacheHandle = Ffi.createDecryptionCache(decryptionKey);

// process multiple cipher texts in a loop
try {
  for (...) {
    byte[] encryptedHeader = ...;
    byte[] encryptedContent = ...;
    // decrypt the hybrid header using the cache
    DecryptedHeader decryptedHeader = Ffi.decryptHeaderUsingCache(cacheHandle, encryptedHeader);
    // decrypt the block of bytes as before
    byte[] clearText = Ffi.decryptBlock(decryptedHeader.getSymmetricKey(), uid, 0, encryptedContent);
  }
finally {
  // The cache should be destroyed to reclaim memory
  Ffi.destroyDecryptionCache(cacheHandle);
}

```

## Secure Computations

*Not publicly available yet. Call Cosmian for early access*

Cosmian Secure Computations allows building micro services in Python (soon Java) that can be deployed on the Cosmian Confidential Cloud. 

The code, the data and the results are encrypted at all times, so the Cosmian Cloud does not learn anything about the data or the algorithm. 

Immediate benefits:

 - on-premise confidential data computing can now be safely delegated to the cloud
 - SaaS providers can now offer their services while keeping their customers data private and protected at all times

Also, data sources, code and results can be encrypted under different keys enabling new collaborative confidential computing scenarios:

- two companies wanting to learn their common customers without revealing to each other their full customers list.
- a FinTech having developed a breakthrough algorithm that needs to be run on a bank data. However the FinTech does not want to share its algorithm  with the bank in clear text, and the bank does not want to share its data with the FinTech in clear text either. 


## Confidential KMS

Cosmian offers a confidential KMS in the Cosmian Confidential Cloud. The KMS operations are protected with the same technology used for the Secure Computations, so Cosmian never learns anything about the keys stored in the KMS or the operations performed with those keys inside the KMS (encryption, decryption, signature,...).

Use of Cosmian KMS is included with the services above.

The KMS offers a KMIP 2.1 interface.

*Only the KMS operations required to enable the Confidential Data Access and Secure Computations are publicly available for now. Contact Cosmian for full KMS access*
