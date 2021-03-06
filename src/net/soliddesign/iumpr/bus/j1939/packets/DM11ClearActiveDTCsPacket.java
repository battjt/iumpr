/**
 * Copyright 2017 Equipment & Tool Institute
 */
package net.soliddesign.iumpr.bus.j1939.packets;

import net.soliddesign.iumpr.bus.Packet;

/**
 * The DM11 DTC Reset/Clear Active DTCs packet won't be received by the vehicle.
 * The response is a NACK or ACK instead.
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM11ClearActiveDTCsPacket extends ParsedPacket {

    /**
     * The possible responses to the DM11 request
     */
    public enum Response {
        ACK(0, "Acknowledged"), BUSY(3, "Busy"), DENIED(2, "Denied"), NACK(1, "NACK"), UNKNOWN(-1, "Unknown");

        private static Response find(int value) {
            for (Response r : Response.values()) {
                if (r.value == value) {
                    return r;
                }
            }
            return Response.UNKNOWN;
        }

        private final String string;

        private final int value;

        private Response(int value, String string) {
            this.value = value;
            this.string = string;
        }

        @Override
        public String toString() {
            return string;
        }
    }

    public static final int PGN = 65235;

    private final Response response;

    public DM11ClearActiveDTCsPacket(Packet packet) {
        super(packet);
        response = parseResponse();
    }

    @Override
    public String getName() {
        return "DM11";
    }

    /**
     * Returns the response
     *
     * @return the response
     */
    public Response getResponse() {
        return response;
    }

    private Response parseResponse() {
        int responseByte = getPacket().get(0);
        return Response.find(responseByte);
    }

    @Override
    public String toString() {
        return getStringPrefix() + "Response is " + getResponse();
    }

}
