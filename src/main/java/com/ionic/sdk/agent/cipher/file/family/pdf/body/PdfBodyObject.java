package com.ionic.sdk.agent.cipher.file.family.pdf.body;

import com.ionic.sdk.agent.cipher.file.family.pdf.data.Pdf;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.PdfDictionary;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.PdfObject;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.PdfString;
import com.ionic.sdk.agent.cipher.file.family.pdf.xref.Xref;
import com.ionic.sdk.core.annotation.InternalUseOnly;

/**
 * Container for metadata about the PDF object.
 */
@InternalUseOnly
public final class PdfBodyObject {

    /**
     * The cross-reference entry associated with this PDF object.
     */
    private final Xref xref;

    /**
     * The PDF value associated with this PDF object.
     */
    private final PdfObject value;

    /**
     * The offset in the object data at which the stream data begins.
     */
    private final int offsetStream;

    /**
     * The leading text at the beginning of the serialized PDF object.
     */
    private final String prologue;

    /**
     * @return the cross-reference entry associated with this PDF object
     */
    public Xref getXref() {
        return xref;
    }

    /**
     * @return the primitive value (if present) associated with this PDF object
     */
    public PdfObject getValue() {
        return value;
    }

    /**
     * @return the PDF dictionary (if present) associated with this PDF object
     */
    public PdfDictionary getDictionary() {
        return (value instanceof PdfDictionary) ? ((PdfDictionary) value) : null;
    }

    /**
     * @return true, iff this PDF object has associated stream content
     */
    public boolean hasStream() {
        final PdfDictionary dictionary = (value instanceof PdfDictionary) ? ((PdfDictionary) value) : null;
        return (dictionary != null) && (dictionary.getStringValue(Pdf.KV.LENGTH) != null);
    }

    /**
     * @return the offset in the object data at which the stream data begins
     */
    public int getOffsetStream() {
        return offsetStream;
    }

    /**
     * @return the leading text at the beginning of the serialized PDF object
     */
    public String getPrologue() {
        return prologue;
    }

    /**
     * Constructor.
     *
     * @param xref         the cross-reference entry associated with this PDF object
     * @param value        the PDF value associated with this PDF object
     * @param offsetStream the offset in the object data at which the stream data begins
     * @param prologue     the object header at the beginning of the serialized PDF object
     */
    public PdfBodyObject(final Xref xref, final PdfObject value, final int offsetStream, final String prologue) {
        this.xref = xref;
        this.value = value;
        this.offsetStream = offsetStream;
        this.prologue = prologue;
    }

    /**
     * @return the string value associated with the PDF object
     */
    public String getStringValue() {
        final String stringValue;
        if (value == null) {
            stringValue = null;
        } else if (value instanceof PdfString) {
            stringValue = ((PdfString) value).getValue();
        } else {
            //stringValue = null;
            throw new IllegalArgumentException(value.getClass().getName());
        }
        return stringValue;
    }

    /**
     * Look up the dictionary value (if present) associated with the specified key in the PDF object directory.
     *
     * @param key the map key to look up
     * @return the value (if present) associated with the specified key
     */
    public String getDictionaryValue(final String key) {
        return (value instanceof PdfDictionary) ? ((PdfDictionary) value).getStringValue(key) : null;
    }
}
