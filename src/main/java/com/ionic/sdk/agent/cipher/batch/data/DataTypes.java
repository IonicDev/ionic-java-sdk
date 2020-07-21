package com.ionic.sdk.agent.cipher.batch.data;

import java.nio.ByteBuffer;
import java.util.Date;

/**
 * Utility class for converting data instances to / from their byte[] representation.
 */
public final class DataTypes {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private DataTypes() {
    }

    /**
     * Convert the input value to a byte array representation.
     *
     * @param value the value to convert
     * @return the byte array representation of the value
     */
    public static byte[] toBytes(final int value) {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
        byteBuffer.putInt(value);
        return byteBuffer.array();
    }

    /**
     * Convert the input value to a byte array representation.
     *
     * @param value the value to convert
     * @return the byte array representation of the value
     */
    public static byte[] toBytes(final long value) {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
        byteBuffer.putLong(value);
        return byteBuffer.array();
    }

    /**
     * Convert the input value to a byte array representation.
     *
     * @param value the value to convert
     * @return the byte array representation of the value
     */
    public static byte[] toBytes(final Date value) {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
        byteBuffer.putLong(value.getTime());
        return byteBuffer.array();
    }

    /**
     * Convert the input value to a byte array representation.
     *
     * @param value the value to convert
     * @return the byte array representation of the value
     */
    public static byte[] toBytes(final float value) {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(Float.SIZE / Byte.SIZE);
        byteBuffer.putFloat(value);
        return byteBuffer.array();
    }

    /**
     * Convert the input value to a byte array representation.
     *
     * @param value the value to convert
     * @return the byte array representation of the value
     */
    public static byte[] toBytes(final double value) {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(Double.SIZE / Byte.SIZE);
        byteBuffer.putDouble(value);
        return byteBuffer.array();
    }

    /**
     * Convert an byte array to its integer (32-bit numeric) representation.
     *
     * @param value the bytes to convert
     * @return the integer representation of the value
     */
    public static int toInt(final byte[] value) {
        return ByteBuffer.wrap(value).getInt();
    }

    /**
     * Convert an byte array to its long (64-bit numeric) representation.
     *
     * @param value the bytes to convert
     * @return the long representation of the value
     */
    public static long toLong(final byte[] value) {
        return ByteBuffer.wrap(value).getLong();
    }

    /**
     * Convert an byte array to its {@link Date} representation.
     *
     * @param value the bytes to convert
     * @return the long representation of the value
     */
    public static Date toDate(final byte[] value) {
        return new Date(ByteBuffer.wrap(value).getLong());
    }

    /**
     * Convert an byte array to its float (32-bit fractional) representation.
     *
     * @param value the bytes to convert
     * @return the float representation of the value
     */
    public static float toFloat(final byte[] value) {
        return ByteBuffer.wrap(value).getFloat();
    }

    /**
     * Convert an byte array to its double (64-bit fractional) representation.
     *
     * @param value the bytes to convert
     * @return the double representation of the value
     */
    public static double toDouble(final byte[] value) {
        return ByteBuffer.wrap(value).getDouble();
    }
}
