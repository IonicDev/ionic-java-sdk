# ionic-java-sdk

[Ionic](https://ionic.com) Java software development kit (SDK).

## Getting The SDK

Machina Java SDK releases (including source and unit tests) are published to the [Maven Central repository](https://repo.maven.apache.org/maven2/com/ionic/ionic-sdk/).

Additional Maven Central publishing information can be found [here](https://mvnrepository.com/artifact/com.ionic/ionic-sdk).

## Developer documentation

Additional [Ionic developer](https://dev.ionic.com) resources are available, as are a general introduction to the Ionic platform [introduction](https://dev.ionic.com/platform/intro).

Visit [Getting Started Tutorial](https://dev.ionic.com/getting-started) for a guide.

## Release Notes

### 2.9.0

### New Features

#### Deterministic Encryption Support
The SDK has added support for deterministic encryption, allowing users to perform equality comparisons on encrypted 
data values.

#### Space-Efficient Encryption of Small Binary Data Values
A new `BinaryCipher` family of cipher implementations has been added to the SDK.  These implementations allow for 
the protection of small binary data values like the existing `ChunkCipher` implementations, but without the 
additional space needed to encode ciphertexts using the base64 algorithm.

#### GraalVM Support
The SDK library JAR now includes additional metadata to enable usage in [GraalVM](https://www.graalvm.org/) 
native binary applications.

#### Support for Machina Identity Assertion Functions
The class `com.ionic.sdk.agent.Agent` now includes additional APIs in support of Machina identity provider operations:
- `CreateIdentityAssertion` allows a Machina-enabled device to generate an assertion, which can be used 
to prove that it has a valid enrollment in the given keyspace.
- `ValidateIdentityAssertion` allows a non-Machina-enabled device to verify the assertion.

#### KeyServices Implementations
Additional implementations of the interface `KeyServices` are available in the distributable 
source (including the test source).
- `KeyServicesMinimal` is a partial implementation of `KeyServices`, with default methods.
- `KeyServicesSingleKey` wraps a single cryptography key, useful for deterministic encryption scenarios.
- `LoopbackAgent` implements `KeyServices`, with a `KeyVaultBase` as a backing persistent store of key data.
- `TestKeyServices` wraps access to a single fixed key, with no network access.  Useful in unit test scenarios.

#### Additional Documentation Included with Release Distributable
The SDK release distributable now includes the following documents, in markdown and html formats:
- `README`, describing high-level SDK project functionality
- `LICENSE`, providing the Machina license agreement for Ionic resources
- `CHANGELOG`, with line items providing summary information about the issues included in each release
- `RELEASE_NOTES`, detailing the features and fixes included in the release 

#### Documentation Improvements
- Additional detail describes the data model associated with the APIs `Agent.loadProfiles()` 
and `Agent.saveProfiles()`.

### Corrected Issues
- `GetKey` transaction responses now include any `KeyObligations` specified by the Machina service.
- Extraneous information about the data associated with a serialized `ProfilePersistor` is now logged at an 
appropriate log level.
- `DeviceProfilePersistorPassword` now documents the minimum password length requirement.

### 2.8.0

#### Abstract Class KeyServicesMinimal
The new class ```KeyServicesMinimal``` has been added, providing default implementations of most ```KeyServices```
interfaces.  This enables users to provide custom KeyServices implementations with minimal boilerplate code.

#### BatchCipher Implementations
The new abstract class ```BatchCipherAbstract``` has been added, with implementations using the AES-CTR and AES-GCM
algorithms.  Users may employ these classes to perform cryptography operations on discrete sets of logically related
plaintexts (for example, multiple columns of a single database table row).

#### Documentation Improvements
- Additional detail describes the use and purpose of the ```GetKeys``` external id data structures.
- The error code definitions are linked in more places within the API documentation.
- Expected return values from ```get``` operations are better documented, detailing expectations with regard to null
values and exceptions.

#### Code Improvements
- Additional SDK classes implement the ```java.io.Serializable``` interface, facilitating usage within external
frameworks such as [Apache Spark](https://spark.apache.org/).
- The source code has been updated to comply with up-to-date versions of the code quality tools
[checkstyle](https://checkstyle.sourceforge.io/), [SpotBugs](https://spotbugs.github.io/), and
[pmd](https://pmd.github.io/).
- Additional trace has been added to help diagnose issues with the available ```ProfilePersistor``` implementations.
- Provide method to specify a desired quantity of keys that match an External ID in a ```GetKeysRequest```.

#### Service Endpoint Updates
- All SDK calls to the Machina service layer use the v2.4 service HTTP endpoints.

### 2.7.0

### New Features

#### ProfilePersistor file specification version 1.1
The Machina ```ProfilePersistor``` file format version 1.1 (currently implemented in the C++ SDK) is now available in 
the Java SDK.  This file format includes a JSON metadata header in the file content.

#### ProfilePersistor (DPAPI) default filesystem location
The DPAPI implementation of the Machina ```ProfilePersistor``` has been updated to use the same algorithm as 
the C++ SDK for determining the default filesystem location of DPAPI profiles.

#### ProfilePersistor (DPAPI) local machine context
The DPAPI addon library now handles usage in the context of a restricted OS user.  The LOCAL_MACHINE context is now 
available when using ```DeviceProfilePersistorWindows``` by constructing it with parameter (isUser=false).
- ```com.ionic.sdk.addon.dpapi.device.profile.persistor.DeviceProfilePersistorWindows(boolean isUser)```

#### Keyspace Name Service additions
Initial support for Machina Keyspace Name Service (KNS) has been added.
- Calls to the API ```Agent.getKeyspace()``` are supported when using an ```Agent``` with no profiles.
- The API ```Agent.updateProfileFromKNS()``` has been added.  This call will update a profile with the latest URL 
available from the KNS provider. You may also pass in a different KNS provider URL instead of using the Ionic server.

#### Documentation Improvements
- Additional detail provided for common use cases:
  - cryptography algorithm choice (AES-CTR, AES-GCM),
  - chunk cryptography (strings),
  - file cryptography (binary content),
  - key operations (key create, key fetch),
  - client accessor configuration (HTTP headers),
  - persistence of client device profile information (plaintext, protected).
- Sample code snippets included in documentation for many user-facing code classes.
- Contextually relevant links provided from code to Machina developer website.
- Javadoc generation corrected to provide links to classes used by the SDK (for example, to the Java 
Runtime Environment).
- References corrected to dependencies on Bouncy Castle library.
- Clarifications added to treatment of Machina client and service time zones.
- Usage information added to the "overview summary" front page of the Javadoc.

#### Machina service transaction code cleanup
Machina service transaction code has been refactored for simplicity and consistency.

#### Agent copy constructor
A copy constructor for the class ```com.ionic.sdk.agent.Agent``` has been implemented, in order to improve 
interface consistency with the C++ reference implementation.  The copy constructor copies the in-memory state of an 
existing Agent instance, so that filesystem access during initialization is unnecessary. 

#### IonicException
The class ```IonicException``` has been enhanced to provide the following additional information: version, build, 
git-commit, and CID.  This additional information will help diagnose and address SDK issues.

#### DeviceProfile validity checks
The SDK alerts on a user attempt to perform an service operation using an invalid ```DeviceProfile```.  This includes
supplying shared secret keys in the DeviceProfile with an invalid length.

### Known Issues
- None

### Corrected Issues
- Correct issue with handling of key fetch failure (C++ reference implementation consistency).

### Deprecations
| Old | New |
| --- | --- |
| ```com.ionic.sdk.agent.Agent.clone()``` | ```com.ionic.sdk.agent.Agent(Agent)``` |

### Additional Notes
- None

### 2.6.1

### New Features
- No new software features have been added to this release.

### Documentation Updates 
- The process of documentation generation has been updated to include additional HTML meta tags, in order to improve 
the search engine optimization (SEO) posture of the SDK documentation.

### 2.6.0

### New Features

#### Use JRE Built-In Provider by Default
The Java interface [java.security.Provider](https://docs.oracle.com/javase/7/docs/api/java/security/Provider.html) 
represents a "provider" for the Java Security API.  Implementations include support for cryptography primitives, such
as algorithms and cryptography key management.  The JRE includes a built-in provider.

In versions 2.0 through 2.5, the Java SDK used the third 
party [Bouncy Castle](https://www.bouncycastle.org/java.html) library implementation of cryptography algorithms.  In 
version 2.6, the SDK uses the built-in provider by default.  This change eliminates a project dependency, avoids some 
compatibility problems, and reduces the memory, disk, and network footprint of Java SDK-enabled applications.

#### New Constructor for Agent Class
An additional constructor has been added for the class ```com.ionic.sdk.agent.Agent```.  This constructor accepts the 
serialized representation of the JSON associated with a set of ```com.ionic.sdk.device.profile.DeviceProfile``` 
objects.  This constructor facilitates use of the SDK in Amazon Web Services Lambda functions, and other cloud contexts. 

#### Support for Device Enrollment Using *Ionic Authentication* Enrollment Method
Software developers may use the [Machina Start for Free](https://ionic.com/start-for-free/) workflow to provision a 
dedicated web service tenant, allowing them to try out the Machina platform and engine.  The *Start for Free* program 
uses the *Ionic Authentication* enrollment method by default to enroll new devices.

The class ```com.ionic.sdk.device.create.EnrollIonicAuth``` provides SDK *Start for Free* users with a means to enroll 
devices.  See the github sample 
[Create Profile Start for Free](https://github.com/IonicDev/samples/tree/master/java/create-profile-start-for-free) 
for more information.

### Known Issues
- None

### Corrected Issues
- The Maven project descriptor files have been adjusted to provide correct information.
- The build distributable has been corrected to include missing build content.
- Several incompatibilities between various Bouncy Castle library versions and various JREs have been addressed.
- The ```com.ionic.sdk.agent.Agent.clone()``` method has been corrected to copy all source data to the new Object.

### Additional Notes

#### Bouncy Castle Compatibility
The cryptography provider built into the JRE is used by default in Java SDK 2.6.  To use the Bouncy Castle cryptography 
provider, add the following statement in your code to execute in your process before any usage of Ionic cryptography:
```
com.ionic.sdk.agent.AgentSdk.initialize(new org.bouncycastle.jce.provider.BouncyCastleProvider());
```

If you choose to use the Bouncy Castle provider in your application, please make sure to declare the dependency as 
needed for your development / runtime environment.  The Maven declaration is as follows:
```
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk15to18</artifactId>
    <version>1.63</version>
</dependency>
```

Due to size constraints, and to the complexity inherent in a project dependency, we recommend the use of the 
Bouncy Castle library only if it is needed by your application.

- If your JRE is version 11+, use the built in cryptography provider.
- If your JRE is version 8+, and if enrollment is handled external to your application, use the built in cryptography 
provider.
- If your JRE is version 8+, and if enrollment is handled by your application, use the Bouncy Castle cryptography 
provider.
- If your JRE is version 7, use the Bouncy Castle cryptography provider.

### 2.5.0

### New Features

#### Automatic SEP selection
Ionic-protected documents may be decrypted if a Secure Enrollment Profile exists on the device with a matching
keyspace (the document keyspace need not associated with the active profile).

#### Agent clone
The SDK now provides an API to clone a new Agent object from an existing Agent object.  This provides an
order-of-magnitude improvement in instantiation performance, and enables single-use Agent objects.

#### GenericFileCipher v1.3
The core SDK generic file format, version 1.3 is now supported.

#### CryptoAbstract
The SDK now supports the use of alternative cryptography library implementations.  See the following javadoc resources
for more information:
- [package com.ionic.sdk.agent](./Doc/javadoc/com/ionic/sdk/agent/package-summary.html)
- [class com.ionic.sdk.crypto.jce.CryptoAbstract](./Doc/javadoc/com/ionic/sdk/crypto/jce/CryptoAbstract.html)

The Java SDK no longer depends on the BouncyCastle library to function correctly.  The SDK may be initialized to use
the JRE-native JCE implementation.

#### Additional Features
- A password-protected KeyVault implementation is now available.
- Test cases for the Java SDK are now being included in the source and binary distributable packages being published to
[Maven Central](https://repo1.maven.org/maven2/com/ionic/ionic-sdk/) and [Github](https://github.com/IonicDev/ionic-java-sdk/).
- DeviceProfilePersistor may now be initialized from a URL or an InputStream.
- An Ionic raw cipher is now available, which uses the Ionic KeyServices interface and underlying AES cipher implementations.
- The server-enforced limit of 1,000 keys per request is now documented.
- A new API is available to expose "com.ionic.sdk.agent.key.AgentKey" as "javax.crypto.SecretKey", aiding in ease of use.

### Known Issues
None

### Corrected Issues
- An issue was corrected in the parsing of files when using PdfFileCipher.
- An issue was corrected in the handling of DPAPI DeviceProfilePersistor when no data file exists.
- An issue was corrected in the handling of the DPAPI DeviceProfilePersistor default file location.
- An issue was corrected in the handling of ChunkCryptoEncryptAttributes constructor null input.

### Additional Notes
#### New default version of Generic Cipher
As part of the effort to improve file cipher capabilities, the SDK has introduced a new version of the "generic" file
encryption format, v1.3.  This format differs from the earlier v1.2 format in that it no longer requires a validation
code (HMAC) to be computed and stored in the file header.   This avoids the need for the writer to retroactively seek
back to the beginning of the file to store a computed hash.

Applications that make use of the Generic FileCipher can choose to continue using an older version (e.g., v1.2) by
specifying the desired version as part of the ```FileCryptoEncryptAttributes.setVersion``` method.

It is important to note, however, that if no version is explicitly specified, the SDK will use the latest version of
the format to create new files.  Opening and decrypting files in this format will require the consuming application to
also be aware of this new format.  *Applications that were built with older versions of the SDK will not be able to
process files in this newer format until they have been rebuilt with the updated SDK.*

There is no issue with newer applications reading files from older versions -- the information in the file header
includes an indication of the format in use.

| Action | Java SDK 2.4 or older | Java SDK 2.5 |
|------------|:----------------:|:----------------------:|
|Create 1.2	 | default        | requires explicit version|
|Read 1.2	 | supported      | supported                |
|Create 1.3	 | not supported  | default                  |
|Read 1.3    | error returned | supported                |

### 2.4.0
The 2.4 release of the Ionic Java (JVM) SDK introduces support for cryptographic operations on two new file formats: "OpenXML" and "PDF".  With the addition of these formats to the support for "generic" and "CSV" formats introduced in the previous (2.3) release, the "JVM" SDK now fully supports all Ionic file and document formats.  It does not currently address the "CMS" format which is designed for secure email communications.

The implementation also uses a "streaming" approach to reading and writing the content allowing files of arbirary size to be processed in a fixed amount of memory.

In addition to the file format support, the release adds new capabilities including
- Support for custom coverpages
- Support for an offline "key vault"

Finally, the release corrects a few defects that were identified in earlier releases.

### New Features
#### File Crypto Support
The Ionic SDK implements a canonical encryption format for several common file types: OpenXML, PDF, and CSV.   In addition, the SDK defines a "Generic" format that can be used to encode any filetype.  The "CSV" and "Generic" formats were introduced as part of the 2.3 release.  This release completes the file format coverage with support for "OpenXML" and "PDF" formats.

File encryption operations are accessed through a set of "encrypt" and "decrypt" methods stemming from the `FileCipherAbstract` base class.  Specifically, four instantiable classes are now available:

```public GenericFileCipher(KeyServices agent)```
```public CsvFileCipher(KeyServices agent)```
```public OpenXmlFileCipher(KeyServices agent)```
```public PdfFileCipher(KeyServices agent)```

#### Custom Coverpages
Custom cover pages were introduced into the core SDK several months ago to provide a more tailored experience for users that receive a protected document but have not yet become "Ionic-aware".

#### Offline Keyvault
Java developers can access the contents of the secure "key vault" on a client device .   This allows, for example, access to cached keys while the device may be offline.

### Known Issues
None

### Corrected Issues

#### WinDPAPI KeyVault header is different from C++ header
The format of the "keyvault" was inconsistent between the Java JVM and C++ versions.  This has been addressed by having both systems use the value "dpapi" for the "cipherId" field.

#### Memory Usage by GenericCipher
Changes introduced as part of the support for the OpenXML and PDF formats have also improved the memory usage for the Generic format implementation from the prior release.

### 2.3.0
The 2.3 release of the Ionic Java (JVM) SDK introduces support for cryptographic operations on selected file formats.  This version allows encryption and decryption of the Ionic "generic" and "csv" formats.    Subsequent releases will extend support to include "OpenXML" and "PDF" formats.

The release also adds new capabilities including
- The ability to transmit messages to the Ionic server through the Ionic messagin API,
- Support for running the DPAPI persistor in the context of a 32-bit Java runtime hosted in a 64-bit operating system.

Finally, the release corrects a few defects that were identified in the 2.2 release.

#### New Features
##### File Crypto Support
The Ionic SDK implements a canonical encryption format for several common file types: OpenXML, PDF, and CSV.   In addition, the SDK defines a "Generic" format that can be used to encode any filetype.   This release provides support for two of these formats, *Generic*, and *CSV* from the Java SDK.  The remaining two formats, *OpenXML* and *PDF* will be supported in a subsequent release.

File encryption operations are accessed through a set of "encrypt" and "decrypt" methods stemming from the `FileCipherAbstract` base class.  Specifically, two instantiable classes are now available:

```public GenericFileCipher(KeyServices agent)```

```public CsvFileCipher(KeyServices agent)```

##### Messaging API
Java applications can now choose to record arbitrary messages in the Ionic back-end server.  These messages can subsequently be retrieved programmatically through a separate RESTful API and can be used for various types of down-stream analysis of the application's behavior. See

```agent.logMessage```.

##### Mixed-environment capability
In the previous (2.2.1) release, Java applications  using the "DPAPI" (Windows-specific) persistor would not operate correctly if deployed in a particular operating environment, namely, a 32-bit Java runtime operating in a 64-bit Windows system.   This combination is now supported.

#### Known Issues
None

#### Corrected Issues

##### Plaintext Persistor Error
In prior releases, an attempt to open a persistor that failed due to an inability to find or open the specified file, resulted in the following error message: "An active device profile has not been set".   This condition now yields a more appropriate message, "Resource not found".

##### Error message when encountering invalid device information
An attempt to load an existing persistor can fail if any of the referenced device identifiers are invalid.  The exception thrown in this condition now includes a more specific error message containing the invalid device id.

##### Null-pointer exception when loading persistor without active profile
Attempting to load a persistor in which no active profile was defined could lead to a null-pointer exception.  This issue has been addressed.

#### Other notes

##### Error reported when opening an empty persistor file
When an application attempts to load an empty persistor file, Java will return a "parse failed" error condition.   Note that this is a slightly different behavior than is found in the older "JNI" implementation in which this condition results in a "failure to open file" message.


### 2.2.1

#### New Features
None

#### Issues Addressed with this Release
The "Password Persistor" implementation provided in the 2.2 version of the Java SDK was incompatible with the equivalent class in other Ionic SDKs (for example, the C++ SDK).  This meant that a Java application built with this release would not successfully open a Password Persistor file created by another application that had used a different SDK.  Similarly, Password Persistors created by the Java SDK could only be accessed by other applications that also used the Java 2.2 SDK.

This issue was specific to applications using a "Password Persistor". Other persistor formats remained compatible across SDK versions.

### 2.2.0

This build extends Ionic's Java 2.x SDK with the addition of a second ".jar" file intended specifically for use in Windows-based applications.
This new module provides support for opening and saving "secure enrollment profiles" stored on a Windows system using the Microsoft Windows DPAPI encryption method.

In addition, the release includes support for two features that had not yet been implemented in the Java 2.x architecture: protected attributes and external IDs.

Finally, the release corrects a few defects that were identified in the 2.1 release.

#### New Features

*Windows Persistor Support*
It is now possible for a Java application running in the context of the Windows operating system to open existing "secure enrollment profiles" that had been encrypted on disk using the Windows persistor.  Because this encryption library is specific to the Windows operating system, the SDK includes this implementation in a separate ".jar" file.  In addition to opening existing SEP files, applications importing this .jar can also update or save profiles with DPAPI encryption.

*Protected Attributes*
Java now supports the access of "protected attributes" on a data protection key.  These attributes are distinguished by a name beginning with `ionic-protected-` and have their values encrypted before being sent to the Ionic server.

*External Identifier Support*
This release of the Java SDK allows developers to associate an external identifier with keys and use that identifier in key retrieval.

#### Issues Addressed with this Release

*Accessing Error Constants in Java Applications*
The underlying type of the error constants associated with the SDK has been changed from an `enum` in the 2.1 release to `int` in 2.2.   Any explicit references to these values no longer require the "`.value()`" method to access the underlying integer value of the enum.

*File Persistor Exception when Path Missing Parent Directory*
An issue was noted when saving profiles to a file whose path did not include parent directory information.  Specifically, filenames of the form "profile.pt" would fail to save, while a name "./profile.pt" would succeed.  This issue is corrected in the 2.2 release.

*Error 50001 when Accessing Password Persistor*
Accessing the "Password Persistor" in the 2.1 release would result in a 50001 error code.  This issue was related to the initialization of the underlying cipher and has been corrected in the 2.2 release.

*Exception thrown when AecGcm Cipher Invoked without AuthData*
The implementation of the AesGcm Cipher in the Java SDK requires the calling function to provide non-empty "authdata".  The function will now throw an IonicException if this value is missing.


### Background
The 2.x releases of the Ionic Java SDK mark an important change in the implementation strategy for Ionic’s Java support. In previous releases, developers working in the Java programming language made use of an SDK in which the published Java classes and methods were thin wrappers over an underlying C++ based implementation. This implementation approach required that deployments include the corresponding C++ libraries on a target system and hence could introduce complications in some environments. The 2.0 implementation of the Ionic Java SDK rewrote the core logic that had been provided through the C++ libraries using only standard Java primitives enabling simpler and more consistent application deployments. This new SDK release can coexist with older (1.x) versions of the Java SDK. If you have an older build, you can simply install this new release in a separate directory. If coexistence is not important, you can simply remove the older build and copy this newer version in its place.

This release uses com.ionic.sdk as the classpath instead of the 1.x line of Java JNI SDKs which use com.ionicsecurity.sdk.

### Migrating Existing 1.x Java Applications

#### Unsupported Classes
This initial release of the updated Java support includes signature-compatible versions of many of the classes that were available in the earlier SDK release. There are, however, some capabilities that are not yet supported in the newer architecture. If you make use of these features in an existing application, it will not be possible to migrate to the 2.x version at this time. These functional areas include

* File Crypto
* Chunk Crypto format 4
* Logging through the ISLog facility. (Java itself provides an alternate logging mechanism. See https://dev.ionic.com/language-examples/next_steps_logging.html for more detail.)

### Required Source Changes
* Applications using this newer implementation will also need to change module references from "com.ionicsecurity" to "com.ionic".
* In the 2.x releases, SdkException has been renamed to "IonicException" and has been converted to a checked exception. Functions that call SDK APIs will typically need to add SdkException to the list of declared exceptions. So, for example, `static Agent initializeAgent() throws IOException` becomes `static Agent initializeAgent() throws IOException, IonicException`.

