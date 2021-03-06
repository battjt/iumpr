/**
 * Copyright 2017 Equipment & Tool Institute
 */
package net.soliddesign.iumpr.bus.j1939.packets;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.soliddesign.iumpr.bus.Packet;

/**
 * Unit tests for the {@link DM7CommandTestsPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM7CommandTestsPacketTest {

    @Test
    public void testGetName() {
        Packet packet = Packet.create(0, 0, 0x00);
        DM7CommandTestsPacket instance = new DM7CommandTestsPacket(packet);
        assertEquals("DM7", instance.getName());
    }

    @Test
    public void testPGN() {
        assertEquals(58112, DM7CommandTestsPacket.PGN);
    }

}
