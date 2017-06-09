/**
 * Copyright 2017 Equipment & Tool Institute
 */
package net.soliddesign.iumpr.bus;

import static net.soliddesign.iumpr.bus.RP1210Library.BLOCKING_NONE;
import static net.soliddesign.iumpr.bus.RP1210Library.CMD_ECHO_TRANSMITTED_MESSAGES;
import static net.soliddesign.iumpr.bus.RP1210Library.CMD_PROTECT_J1939_ADDRESS;
import static net.soliddesign.iumpr.bus.RP1210Library.CMD_SET_ALL_FILTERS_STATES_TO_PASS;
import static net.soliddesign.iumpr.bus.RP1210Library.ECHO_OFF;
import static net.soliddesign.iumpr.bus.RP1210Library.NOTIFICATION_NONE;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Stream;

import net.soliddesign.iumpr.IUMPR;

/**
 * The RP1210 implementation of a {@link Bus}
 *
 * @author Joe Batt (joe@soliddesign.net)
 *
 */
public class RP1210Bus implements Bus {

    /**
     * The source address for this tool
     */
    private final int address;

    /**
     * The id of this client for the {@link RP1210Library}
     */
    private final short clientId;

    /**
     * The byte array used to read data from the {@link RP1210Library}
     */
    private final byte[] data = new byte[2048];

    /**
     * The thread pool used for polling
     */
    private final ScheduledExecutorService exec;

    /**
     * The Queue of {@link Packet}s
     */
    private final MultiQueue<Packet> queue;

    /**
     * The {@link RP1210Library}
     */
    private final RP1210Library rp1210Library;

    /**
     * Constructor
     *
     * @param adapter
     *            the {@link Adapter} thats connected to the vehicle
     * @param address
     *            the address of this branch on the bus
     * @throws BusException
     *             if there is a problem connecting to the adapter
     */
    public RP1210Bus(Adapter adapter, int address) throws BusException {
        this(RP1210Library.load(adapter), Executors.newSingleThreadScheduledExecutor(), new MultiQueue<>(), adapter,
                address);
    }

    /**
     * Constructor exposed for testing
     *
     * @param rp1210Library
     *            the {@link RP1210Library} that connects to the adapter
     * @param exec
     *            the {@link ScheduledExecutorService} that will execute tasks
     * @param queue
     *            the {@link MultiQueue} the backs this bus
     * @param adapter
     *            the {@link Adapter} thats connected to the vehicle
     * @param address
     *            the source address of this branch on the bus
     * @throws BusException
     *             if there is a problem connecting to the adapter
     */
    public RP1210Bus(RP1210Library rp1210Library, ScheduledExecutorService exec, MultiQueue<Packet> queue,
            Adapter adapter, int address) throws BusException {
        this.rp1210Library = rp1210Library;
        this.exec = exec;
        this.queue = queue;
        this.address = address;

        clientId = rp1210Library.RP1210_ClientConnect(0, adapter.getDeviceId(), "J1939:Baud=Auto", 0, 0,
                (short) 0);
        verify(clientId);
        try {
            sendCommand(CMD_PROTECT_J1939_ADDRESS, new byte[] { (byte) address, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
            sendCommand(CMD_ECHO_TRANSMITTED_MESSAGES, ECHO_OFF);
            sendCommand(CMD_SET_ALL_FILTERS_STATES_TO_PASS);

            this.exec.scheduleAtFixedRate(() -> poll(), 1, 1, TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            stop();
            throw new BusException("Failed to configure adapter.", e);
        }
    }

    /**
     * Decodes the given byte array into a {@link Packet}
     *
     * @param data
     *            the byte array to decode
     * @param length
     *            the total length of the payload data
     * @return {@link Packet}
     */
    private Packet decode(byte[] data, int length) {
        int pgn = ((data[6] & 0xFF) << 16) | ((data[5] & 0xFF) << 8) | (data[4] & 0xFF);
        int priority = data[7] & 0x07;
        int source = data[8] & 0xFF;
        return Packet.createPriority(priority, pgn, source, Arrays.copyOfRange(data, 10, length));
    }

    /**
     * Transforms the given {@link Packet} into a byte array so it can be sent
     * to the vehicle bus
     *
     * @param packet
     *            the {@link Packet} to encode
     * @return a byte array of the encoded packet
     */
    private byte[] encode(Packet packet) {
        byte[] buf = new byte[packet.getLength() + 6];
        buf[0] = (byte) packet.getId();
        buf[1] = (byte) (packet.getId() >> 8);
        buf[2] = (byte) (packet.getId() >> 16);
        buf[3] = (byte) packet.getPriority();
        buf[4] = (byte) packet.getSource();
        buf[5] = (packet.getId() < 0xF000) ? (byte) (packet.getId() & 0xFF) : 0;
        for (int i = 0; i < packet.getLength(); i++) {
            buf[6 + i] = (byte) packet.get(i);
        }
        return buf;
    }

    @Override
    public int getAddress() {
        return address;
    }

    /**
     * Checks the {@link RP1210Library} for any incoming messages. Any incoming
     * messages are decoded and added to the queue
     */
    private void poll() {
        try {
            while (true) {
                short rtn = rp1210Library.RP1210_ReadMessage(clientId, data, (short) data.length, BLOCKING_NONE);
                if (rtn > 0) {
                    queue.add(decode(data, rtn));
                } else {
                    verify(rtn);
                    break;
                }
            }
        } catch (BusException e) {
            IUMPR.getLogger().log(Level.WARNING, "Failed to read RP1210", e);
        }
    }

    @Override
    public Stream<Packet> read(long timeout, TimeUnit unit) throws BusException {
        return queue.stream(timeout, unit);
    }

    @Override
    public void send(Packet packet) throws BusException {
        try {
            byte[] data = encode(packet);
            Future<Short> rtn = exec
                    .submit(() -> rp1210Library.RP1210_SendMessage(clientId, data, (short) data.length,
                            NOTIFICATION_NONE, BLOCKING_NONE));
            verify(rtn.get());
        } catch (Exception e) {
            throw new BusException("Failed to send: " + packet, e);
        }
    }

    /**
     * Helper method to send a command to the library
     *
     * @param command
     *            the command to send
     * @param data
     *            the data to include in the command
     * @throws BusException
     *             if there result of the command was unsuccessful
     */
    private void sendCommand(short command, byte... data) throws BusException {
        short rtn = rp1210Library.RP1210_SendCommand(command, clientId, data, (short) data.length);
        verify(rtn);
    }

    /**
     * Disconnects from the {@link RP1210Library}
     *
     * @throws BusException
     *             if there is a problem disconnecting
     */
    public void stop() throws BusException {
        try {
            if (rp1210Library != null) {
                exec.submit(() -> rp1210Library.RP1210_ClientDisconnect(clientId)).get();
            }
        } catch (Exception e) {
            throw new BusException("Failed to stop RP1210.", e);
        } finally {
            exec.shutdown();
        }
    }

    /**
     * Checks the code returned from calls to the adapter to determine if it's
     * an error
     *
     * @param rtnCode
     *            the return code to check
     * @throws BusException
     *             if the return code is an error
     */
    private void verify(short rtnCode) throws BusException {
        if (rtnCode > 127 || rtnCode < 0) {
            rtnCode = (short) Math.abs(rtnCode);
            byte[] buffer = new byte[256];
            rp1210Library.RP1210_GetErrorMsg(rtnCode, buffer);
            throw new BusException("Error (" + rtnCode + "): " + new String(buffer, StandardCharsets.UTF_8).trim());
        }
    }

}
