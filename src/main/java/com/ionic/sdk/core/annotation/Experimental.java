package com.ionic.sdk.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Program elements annotated with {@link Experimental} are intended for narrowly defined usage scenarios.
 * <p>
 * Such elements are not intended for broad usage, and care should be given to guard against misuse.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Experimental {
}
