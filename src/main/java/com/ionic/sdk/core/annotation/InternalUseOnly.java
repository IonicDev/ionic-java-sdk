package com.ionic.sdk.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Program elements annotated with {@link InternalUseOnly} are intended for Ionic SDK internal use only.
 * <p>
 * Such elements are not public by design and likely to be removed, have their signature change, or have their access
 * level decreased from public to protected, package, or private in future versions of the Ionic SDK without notice.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface InternalUseOnly {
}
