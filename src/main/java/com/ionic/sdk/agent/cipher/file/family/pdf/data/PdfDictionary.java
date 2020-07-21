package com.ionic.sdk.agent.cipher.file.family.pdf.data;

import com.ionic.sdk.core.annotation.InternalUseOnly;

import java.util.List;
import java.util.TreeMap;

/**
 * In memory representation of a PDF object dictionary (PDF 32000-1:2008, section 7.3.7).
 */
@InternalUseOnly
public final class PdfDictionary extends TreeMap<String, PdfObject> implements PdfObject {

    /**
     * Look up the string value (if present) associated with the specified key in the PDF object directory.
     *
     * @param key the map key to look up
     * @return the value (if present) associated with the specified key
     */
    public String getStringValue(final String key) {
        String value = null;
        final PdfObject pdfObject = get(key);
        if (pdfObject instanceof PdfString) {
            value = ((PdfString) pdfObject).getValue();
        }
        return value;
    }

    /**
     * Look up the array value (if present) associated with the specified key in the PDF object directory.
     *
     * @param key the map key to look up
     * @return the value (if present) associated with the specified key
     */
    public List<PdfObject> getArrayValue(final String key) {
        List<PdfObject> value = null;
        final PdfObject pdfObject = get(key);
        if (pdfObject instanceof PdfArray) {
            value = (PdfArray) pdfObject;
        }
        return value;
    }

    /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.7.0". */
    private static final long serialVersionUID = -8977593510185292133L;
}
