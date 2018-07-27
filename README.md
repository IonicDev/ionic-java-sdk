# ionic-java-sdk

[Ionic](https://ionic.com) Java software development kit (SDK).

### Getting The SDK

The built Java SDK JAR can be downloaded from [Ionic Downloads](https://dev-dashboard.ionic.com/#/downloads?tenant=5640bb430ea2684423e0655c).
In general, you should not need to build the SDK from this source.

## Developer documentation

Additional [Ionic developer](https://dev.ionic.com) resources are available, as are a general introduction to the Ionic platform [introduction](https://dev.ionic.com/platform/intro).

Visit [Getting Started Tutorial](https://dev.ionic.com/tutorials/getting-started/sdk-setup) for a guide.

## Release Notes

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

