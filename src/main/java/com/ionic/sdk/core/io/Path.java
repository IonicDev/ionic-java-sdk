package com.ionic.sdk.core.io;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities for processing path strings.
 */
public final class Path {

    /**
     * Constructor. http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private Path() {
    }

    /**
     * Decompose input path into path components based on slash ("/") character.  To be used with HTTP URLs.
     *
     * @param path string specifying a path to be decomposed
     * @return a list of path components
     */
    public static List<String> split(final String path) {
        final List<String> tokens = new ArrayList<String>();
        final Matcher matcher = PATH_COMPONENTS.matcher(path);
        int start = 0;
        while (matcher.find(start)) {
            tokens.add(matcher.group(1));
            start = matcher.end(1);
        }
        return tokens;
    }

    /**
     * A regular expression for iterating through an input string to break out path components.
     */
    private static final Pattern PATH_COMPONENTS = Pattern.compile("(/.*?)(?=(?:/)|$)");
}
