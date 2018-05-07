# ionic-java-sdk

[Ionic](https://ionic.com) Java software development kit (SDK).

## Release Notes

The 2.1 release also includes several features that continue to move it closer to parity with the earlier (JNI-based) Java implementation. Newly added features include:

* Support for device enrollment
* Adding version information to communication sessions with IDC
* Support for the Chunk “v3” format
* Shared secret persistor logic

### Background
The 2.x releases of the Ionic Java SDK mark an important change in the implementation strategy for Ionic’s Java support. In previous releases, developers working in the Java programming language made use of an SDK in which the published Java classes and methods were thin wrappers over an underlying C++ based implementation. This implementation approach required that deployments include the corresponding C++ libraries on a target system and hence could introduce complications in some environments. The 2.0 implementation of the Ionic Java SDK rewrote the core logic that had been provided through the C++ libraries using only standard Java primitives enabling simpler and more consistent application deployments. This new SDK release can coexist with older (1.x) versions of the Java SDK. If you have an older build, you can simply install this new release in a separate directory. If coexistence is not important, you can simply remove the older build and copy this newer version in its place.

This release uses com.ionic.sdk as the classpath instead of the 1.x line of Java JNI SDKs which use com.ionicsecurity.sdk.

### New Features
Support for Device Enrollment
The “createDevice” function provides the ability to programatically enroll users in an Ionic tenant.

#### SDK Version Information
All communications between an Ionic SDK application and the Ionic backend server include additional metadata about the version of the SDK in use.

#### Chunk “v3” Format
Support has been extended to include the “v3” format for chunk ciphers.

#### Shared Secret Persistors
A new interface, SecretShareData, has been introduced. Implementations of this interface are used to generate a cryptography key from data supplied by the implementation. The key is protected by splitting it to shares using the Shamir Secret Sharing algorithm. The buckets define groups of attributes which must remain the same across usages, up to the threshold defined in each bucket.

##### Prerequisites
This version of the Ionic Java SDK is compatible with versions 1.7, 1.8, or 1.9 of the Java Runtime Environment. (see, e.g., http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html).
The cryptographic primitives used in this release require “Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files”. See https://stackoverflow.com/a/39889731 for additional information.
The system requires the presence of javax.json.jar (version 1.04). This dependency is expressed in the “pom” file that is included with the release.
The system requires the “bouncycastle” module (bcprov-jdk15on-1.58.jar) to support certain cryptographic primitives.

### Known Issues
None

### Migrating Existing 1.x Java Applications

#### Unsupported Classes
This initial release of the updated Java support includes signature-compatible versions of many of the classes that were available in the earlier SDK release. There are, however, some capabilities that are not yet supported in the newer architecture. If you make use of these features in an existing application, it will not be possible to migrate to the 2.x version at this time. These functional areas include

* File Crypto
* Chunk Crypto format 4
* Logging through the ISLog facility. (Java itself provides an alternate logging mechanism. See https://dev.ionic.com/language-examples/next_steps_logging.html for more detail.)

### Required Source Changes
* Applications using this newer implementation will also need to change module references from "com.ionicsecurity" to "com.ionic".
* In the 2.x releases, SdkException has been renamed to "IonicException" and has been converted to a checked exception. Functions that call SDK APIs will typically need to add SdkException to the list of declared exceptions. So, for example, `static Agent initializeAgent() throws IOException` becomes `static Agent initializeAgent() throws IOException, IonicException`.

## Developer documentation

Additional [Ionic developer](https://dev.ionic.com) resources are available, as are a general introduction to the Ionic platform [fundamentals](https://dev.ionic.com/fundamentals.html).

Visit [Getting Started with the Java SDK](https://dev.ionic.com/getting-started/java-20.html) for a guide.

### Getting The SDK

The built Java SDK JAR can be downloaded from [Ionic Downloads](https://dev-dashboard.ionic.com/#/downloads?tenant=5640bb430ea2684423e0655c).