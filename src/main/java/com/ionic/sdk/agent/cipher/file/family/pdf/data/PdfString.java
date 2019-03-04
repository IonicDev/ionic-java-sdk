package com.ionic.sdk.agent.cipher.file.family.pdf.data;

import com.ionic.sdk.core.annotation.InternalUseOnly;

/**
 * In memory representation of a PDF object string (PDF 32000-1:2008, section 7.3.4).
 */
@InternalUseOnly
public final class PdfString implements PdfObject {

    /**
     * The value associated with this object instance.
     */
    private final String value;

    /**
     * Constructor.
     *
     * @param value the value associated with this object instance
     */
    public PdfString(final String value) {
        this.value = value;
    }

    /**
     * @return the value associated with this object instance
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
