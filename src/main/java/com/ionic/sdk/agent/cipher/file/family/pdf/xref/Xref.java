package com.ionic.sdk.agent.cipher.file.family.pdf.xref;

import com.ionic.sdk.agent.cipher.file.family.pdf.data.Pdf;
import com.ionic.sdk.agent.cipher.file.family.pdf.stream.ObjStmEntry;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.value.Value;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Object encapsulating the information associated with a PDF file cross-reference table entry
 * (PDF 32000-1:2008, section 7.5.4).
 */
@InternalUseOnly
public final class Xref {

    /**
     * Object number, implicitly declared via the ordinal range in the enclosing cross-reference section.
     */
    private final int objectNumber;

    /**
     * Offset in the channel of the associated PDF object.  Mutable to handle cross reference stream / ObjStm case.
     */
    private int offset;

    /**
     * Generation number of the associated PDF object.
     */
    private final int generationNum;

    /**
     * Flag indicating the state of the associated PDF object.
     *
     * <ul>
     * <li>n - in use</li>
     * <li>f - free</li>
     * </ul>
     */
    private final String inUse;

    /**
     * Object number of the ObjStm (object stream) containing the data for this object.
     */
    private final ObjStmEntry objStmEntry;

    /**
     * @return the object number of the associated PDF object
     */
    public int getObjectNumber() {
        return objectNumber;
    }

    /**
     * @return the offset in the channel of the associated PDF object
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Set the offset in the file of the associated PDF object.
     *
     * @param offset the offset in the channel of the associated PDF object
     */
    public void setOffset(final int offset) {
        this.offset = offset;
    }

    /**
     * @return the generation number of the associated PDF object
     */
    public int getGenerationNum() {
        return generationNum;
    }

    /**
     * @return the flag indicating the state of the associated PDF object
     */
    public String getInUse() {
        return inUse;
    }

    /**
     * @return true, iff the associated PDF object is marked as being in use
     */
    public boolean isInUse() {
        return Pdf.XREF_IN_USE.equals(inUse);
    }

    /**
     * @return the container object stream in which this object is embedded, if applicable
     */
    public ObjStmEntry getObjStmEntry() {
        return objStmEntry;
    }

    /**
     * Constructor.
     *
     * @param entry        the twenty character text representation of the serialized xref record
     * @param objectNumber the implicit index of the xref record
     */
    public Xref(final String entry, final int objectNumber) {
        this.objectNumber = objectNumber;
        final Matcher matcher = Pdf.Regex.XREF_ENTRY.matcher(entry);
        if (matcher.matches()) {
            int group = 0;
            this.offset = Value.toInt(matcher.group(++group), 0);
            this.generationNum = Value.toInt(matcher.group(++group), 0);
            this.inUse = matcher.group(++group);
        } else {
            throw new IllegalArgumentException(entry);
        }
        this.objStmEntry = null;
    }

    /**
     * Constructor.
     *
     * @param objectNumber  the object ordinal associated with this index record
     * @param offset        the offset to the data record, either in the file or in the container object stream
     * @param generationNum the generation number of the associated PDF object
     * @param inUse         the flag indicating the state of the associated PDF object
     * @param objStmEntry   if present, this reference is to a PDF object contained within an object stream
     */
    public Xref(final int objectNumber, final int offset, final int generationNum,
                final String inUse, final ObjStmEntry objStmEntry) {
        this.objectNumber = objectNumber;
        this.offset = offset;
        this.generationNum = generationNum;
        this.inUse = inUse;
        this.objStmEntry = objStmEntry;
    }

    /**
     * @return the string representation of an indirect reference to this PDF object
     */
    public String toReference() {
        return Value.join(Pdf.Token.SPACER, objectNumber, generationNum, Pdf.REFERENCE);
    }

    /**
     * Given an indirect reference (PDF 32000-1:2008, section 7.5.4), extract the xref identifier token.
     *
     * @param reference the indirect reference to a PDF object
     * @return the ordinal identifier from the indirect reference, or null if not present
     */
    public static int getIdentifier(final String reference) {
        final String input = (reference == null) ? "" : reference;
        final Matcher matcherIndirect = Pattern.compile(REGEX_REFERENCE_INDIRECT).matcher(input);
        final String token = matcherIndirect.matches() ? matcherIndirect.group(1) : null;
        return (token == null) ? Integer.MIN_VALUE : Value.toInt(token, Integer.MIN_VALUE);
    }

    /**
     * Regular expression used to describe an indirect reference to an PDF object.
     */
    private static final String REGEX_REFERENCE_INDIRECT = "(\\d+) (\\d+) R";
}
