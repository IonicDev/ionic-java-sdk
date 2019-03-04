# ionic-java-sdk

[Ionic](https://ionic.com) Java software development kit (SDK).

### Getting The SDK

The built Java SDK JAR can be downloaded from [Ionic Downloads](https://dev-dashboard.ionic.com/#/downloads?tenant=5640bb430ea2684423e0655c).
In general, you should not need to build the SDK from this source.

## Developer documentation

Additional [Ionic developer](https://dev.ionic.com) resources are available, as are a general introduction to the Ionic platform [introduction](https://dev.ionic.com/platform/intro).

Visit [Getting Started Tutorial](https://dev.ionic.com/getting-started) for a guide.

## Release Notes

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

