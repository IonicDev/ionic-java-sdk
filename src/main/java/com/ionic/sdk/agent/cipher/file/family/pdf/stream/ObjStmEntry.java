package com.ionic.sdk.agent.cipher.file.family.pdf.stream;

import com.ionic.sdk.core.annotation.InternalUseOnly;

/**
 * Object streams (ObjStm) are stream objects in which a sequence of indirect objects may be stored, as an alternative
 * to storage at the outermost file level (PDF 32000-1:2008, section 7.5.7).
 * <p>
 * The prologue of the object stream contains a list of ObjStmEntry records, describing the object numbers of the
 * associated PDF objects, as well as their offsets within the object stream.
 */
@InternalUseOnly
public final class ObjStmEntry {

    /**
     * Object number of this object stream entry.
     */
    private final int objectNumber;

    /**
     * Index of this object stream entry.
     */
    private final int index;

    /**
     * Constructor.
     *
     * @param objectNumber the object number of this object stream entry
     * @param index        the index of this object stream entry
     */
    public ObjStmEntry(final int objectNumber, final int index) {
        this.objectNumber = objectNumber;
        this.index = index;
    }

    /**
     * @return the object number of this object stream entry
     */
    public int getObjectNumber() {
        return objectNumber;
    }

    /**
     * @return the index of this object stream entry
     */
    public int getIndex() {
        return index;
    }
}
