package com.ionic.sdk.agent.cipher.file.family.pdf.xref;

import com.ionic.sdk.agent.cipher.file.family.pdf.data.PdfDictionary;
import com.ionic.sdk.core.annotation.InternalUseOnly;

import java.util.TreeMap;

/**
 * Object encapsulating the information associated with a PDF file cross-reference table
 * (PDF 32000-1:2008, section 7.5.4).
 */
@InternalUseOnly
public final class XrefTable extends TreeMap<Integer, Xref> {

    /**
     * The offset in the containing PDF document at which this xref table was found.
     */
    private final int offset;

    /**
     * @return the offset in the containing PDF document at which this xref table was found
     */
    public int getOffset() {
        return offset;
    }

    /**
     * The PDF trailer dictionary, used to store metadata associated with an document.
     */
    private PdfDictionary trailerDictionary;

    /**
     * @return the dictionary used to store metadata associated with a document
     */
    public PdfDictionary getTrailerDictionary() {
        return trailerDictionary;
    }

    /**
     * Set the dictionary used to store metadata associated with a document.
     *
     * @param trailerDictionary the dictionary used to store metadata associated with a document
     */
    public void setTrailerDictionary(final PdfDictionary trailerDictionary) {
        this.trailerDictionary = trailerDictionary;
    }

    /**
     * Constructor.
     *
     * @param offset the offset in the containing PDF document at which this xref table was found
     */
    public XrefTable(final int offset) {
        this.offset = offset;
    }

    /**
     * Given an indirect reference (PDF 32000-1:2008, section 7.5.4), find the corresponding xref table entry.
     *
     * @param ref the indirect reference to a PDF object
     * @return the xref entry corresponding to the indirect reference
     */
    public Xref getXrefIndirect(final String ref) {
        final Integer identifier = Xref.getIdentifier(ref);
        return (identifier == null) ? null : get(identifier);
    }

    /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.7.0". */
    private static final long serialVersionUID = 1864286166075223518L;
}
