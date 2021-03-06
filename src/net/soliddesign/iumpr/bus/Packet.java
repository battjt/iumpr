/**
 * Copyright 2017 Equipment & Tool Institute
 */
package net.soliddesign.iumpr.bus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

import net.soliddesign.iumpr.IUMPR;

/**
 * Sends a Packet containing an id with data from a source onto the bus
 *
 * @author Joe Batt (joe@soliddesign.net)
 *
 */
public class Packet {

    /**
     * The indication that a packet was transmitted
     */
    public static final String TX = " (TX)";

    public static Packet create(int id, int source, boolean transmitted, int... data) {
        return new Packet(6, id, source, transmitted, data);
    }

    /**
     * Creates an instance of Packet
     *
     * @param id
     *            the ID of the packet
     * @param source
     *            the source address of the packet
     * @param bytes
     *            the data bytes of the packet
     * @return Packet
     */
    public static Packet create(int id, int source, byte... bytes) {
        return create(6, id, source, false, bytes);
    }

    /**
     * Creates an instance of Packet
     *
     * @param id
     *            the ID of the packet
     * @param source
     *            the source address of the packet
     * @param data
     *            the data of the packet
     * @return Packet
     */
    public static Packet create(int id, int source, int... data) {
        return create(id, source, false, data);
    }

    /**
     * Creates an instance of Packet
     *
     * @param priority
     *            the priority of the packet
     * @param id
     *            the ID of the packet
     * @param source
     *            the source address of the packet
     * @param transmitted
     *            indicates the packet was sent by the application
     * @param bytes
     *            the data bytes of the packet
     * @return Packet
     */
    public static Packet create(int priority, int id, int source, boolean transmitted, byte... bytes) {
        int[] data = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            data[i] = bytes[i];
        }
        return new Packet(priority, id, source, transmitted, data);
    }

    /**
     * Converts the value produced by Packet.toString() back into a Packet
     *
     * @param string
     *            the {@link String} to parse
     * @return a Packet or null if the string could not be parsed
     */
    public static Packet parse(String string) {
        try {
            boolean tx = string.contains(TX);
            if (tx) {
                string = string.replace(TX, "");
            }
            String[] parts = string.split(" ");
            int header = Integer.parseInt(parts[0].trim(), 16);
            int priority = (header & 0xFF000000) >> 26;
            int id = (header & 0xFFFF00) >> 8;
            int source = header & 0xFF;

            byte[] bytes = new byte[parts.length - 1];
            for (int i = 1; i < parts.length; i++) {
                bytes[i - 1] = (byte) (Integer.parseInt(parts[i].trim(), 16) & 0xFF);
            }

            return Packet.create(priority, id, source, tx, bytes);
        } catch (Exception e) {
            IUMPR.getLogger().log(Level.SEVERE, string + " could not be parsed into a Packet", e);
        }
        return null;
    }

    private final int[] data;

    private final int id;

    private final int priority;

    private final int source;

    private final LocalDateTime timestamp;

    private final boolean transmitted;

    /**
     * Creates a Packet
     *
     * @param priority
     *            the priority of the packet
     * @param id
     *            the ID of the packet
     * @param source
     *            the source address of the packet
     * @param transmitted
     *            indicates the packet was sent by the application
     * @param data
     *            the data of the packet
     */
    private Packet(int priority, int id, int source, boolean transmitted, int... data) {
        timestamp = LocalDateTime.now();
        this.priority = priority;
        this.id = id;
        this.source = source;
        this.transmitted = transmitted;
        this.data = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            this.data[i] = (0xFF & data[i]);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Packet)) {
            return false;
        }
        if (this == obj) {
            return true;
        }

        Packet that = (Packet) obj;
        return id == that.id && priority == that.priority && source == that.source && transmitted == that.transmitted
                && Objects.deepEquals(data, that.data);
    }

    /**
     * Returns one byte (8-bits) from the data at the given index
     *
     * @param i
     *            the index
     * @return int
     */
    public int get(int i) {
        return data[i];
    }

    /**
     * Returns two bytes (16-bits) from the data at the given index and index+1
     *
     * @param i
     *            the index
     * @return int
     */
    public int get16(int i) {
        return (data[i + 1] << 8) | data[i];
    }

    /**
     * Returns two bytes (16-bits) from the data in Big-endian format at the
     * given index and index+1
     *
     * @param i
     *            the index
     * @return int
     */
    public int get16Big(int i) {
        return (data[i] << 8) | data[i + 1];
    }

    /**
     * Returns three bytes (24-bits) from the data at the given index, index+1
     * and index+2
     *
     * @param i
     *            the index
     * @return int
     */
    public int get24(int i) {
        return (data[i + 2] << 16) | (data[i + 1] << 8) | data[i];
    }

    /**
     * Returns three bytes (24-bits) from the data in Big-endian format at the
     * given index, index+1, and index+2
     *
     * @param i
     *            the index
     * @return int
     */
    public int get24Big(int i) {
        return (data[i] << 16) | (data[i + 1] << 8) | data[i + 2];
    }

    /**
     * Returns four bytes (32-bits) from the data at the given index, index+1,
     * index+2, and index+3
     *
     * @param i
     *            the index
     * @return int
     */
    public long get32(int i) {
        return (data[i + 3] << 24) | (data[i + 2] << 16) | (data[i + 1] << 8) | data[i];
    }

    /**
     * Returns four bytes (32-bits) from the data in Big-endian format at the
     * given index, index+1, index+2, and index+3
     *
     * @param i
     *            the index
     * @return int
     */
    public long get32Big(int i) {
        return (data[i] << 24) | (data[i + 1] << 16) | (data[i + 2] << 8) | data[i + 3];
    }

    /**
     * Returns the data as an array of bytes
     *
     * @return byte[]
     */
    public byte[] getBytes() {
        byte[] bytes = new byte[data.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) data[i];
        }
        return bytes;
    }

    /**
     * Returns the data from the beginIndex to the endIndex (inclusive).
     *
     * @param beginIndex
     *            the first data value to return
     * @param endIndex
     *            the last data value to return
     * @return int[]
     */
    public int[] getData(int beginIndex, int endIndex) {
        return Arrays.copyOfRange(data, beginIndex, endIndex);
    }

    /**
     * Returns the ID of the packet
     *
     * @return int
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the total number of data bytes in the packet
     *
     * @return int
     */
    public int getLength() {
        return data.length;
    }

    /**
     * Returns the priority of the packet
     *
     * @return int
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns the source address of the packet
     *
     * @return int
     */
    public int getSource() {
        return source;
    }

    /**
     * Returns the Time the packet was received
     *
     * @return {@link LocalDateTime}
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, priority, source, transmitted, Arrays.hashCode(data));
    }

    /**
     * Returns true if this packet was transmitted by the application
     *
     * @return boolean
     */
    public boolean isTransmitted() {
        return transmitted;
    }

    @Override
    public String toString() {
        return toString(null);
    }

    /**
     * Creates the {@link String} of the Packet including the time received
     * formatted by the {@link DateTimeFormatter}. If the formatter is null, the
     * time is not included
     *
     * @param formatter
     *            the {@link DateTimeFormatter} to format the time received
     * @return a {@link String}
     */
    public String toString(DateTimeFormatter formatter) {
        return (formatter == null ? "" : (formatter.format(timestamp) + " "))
                + String.format("%06X%02X %s", priority << 18 | id, source,
                        Arrays.stream(data).mapToObj(x -> String.format("%02X", x)).collect(Collectors.joining(" "))
                                + (transmitted ? TX : ""));
    }
}
