package com.ionic.sdk.agent.cipher.file.family.openxml.input;

import com.ionic.sdk.core.annotation.InternalUseOnly;

import org.w3c.dom.Node;

/**
 * Represents a found portion marked node.
 */
@InternalUseOnly
public final class OpenXmlPortionMarkNode {

    /**
     * The color mark on this node.
     */
    private final String color;

    /**
     * The raw text on this node.
     */
    private final String text;

    /**
     * The XML node containing the text that will need decrypted / encrypted.
     */
    private final Node node;

    /**
     * Constructor.
     * @param color the color found on the node
     * @param text the raw text found on the node
     * @param node the node identified as containing the portion mark text.
    */
    public OpenXmlPortionMarkNode(final String color, final String text, final Node node) {

        this.color = color;
        this.text = text;
        this.node = node;
    }

    /**
     * Color Getter.
     *
     * @return the color found on the node
    */
    public String getColor() {
        return color;
    }

    /**
     * Text Getter.
     *
     * @return the raw text found on the node
    */
    public String getText() {
        return text;
    }

    /**
     * Node Getter.
     *
     * @return the node identified as containing the portion mark text.
    */
    public Node getNode() {
        return node;
    }
}
