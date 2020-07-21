package com.ionic.sdk.agent.cipher.file.family.pdf.data;

import com.ionic.sdk.core.annotation.InternalUseOnly;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * In memory representation of a PDF object array (PDF 32000-1:2008, section 7.3.6).
 */
@InternalUseOnly
public final class PdfArray extends ArrayList<PdfObject> implements PdfObject {

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(PROLOGUE);
        final Iterator<PdfObject> iterator = iterator();
        while (iterator.hasNext()) {
            final PdfObject pdfObject = iterator.next();
            buffer.append(pdfObject.toString());
            if (iterator.hasNext()) {
                buffer.append(DELIMITER);
            }
        }
        buffer.append(EPILOGUE);
        return buffer.toString();
    }

    /**
     * The delimiter used to separate entries in a serialized {@link PdfArray} object.
     */
    private static final String DELIMITER = " ";

    /**
     * The delimiter used to mark the beginning of a serialized {@link PdfArray} object.
     */
    private static final String PROLOGUE = "[" + DELIMITER;

    /**
     * The delimiter used to mark the end of a serialized {@link PdfArray} object.
     */
    private static final String EPILOGUE = DELIMITER + "]";

    /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.7.0". */
    private static final long serialVersionUID = -2559784859251852677L;
}
