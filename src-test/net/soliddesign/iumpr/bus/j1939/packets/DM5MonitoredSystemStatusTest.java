/**
 * Copyright 2017 Equipment & Tool Institute
 */
package net.soliddesign.iumpr.bus.j1939.packets;

import static net.soliddesign.iumpr.bus.j1939.packets.DM5MonitoredSystemStatus.NOT_SUPPORTED_COMPLETE;
import static net.soliddesign.iumpr.bus.j1939.packets.DM5MonitoredSystemStatus.NOT_SUPPORTED_NOT_COMPLETE;
import static net.soliddesign.iumpr.bus.j1939.packets.DM5MonitoredSystemStatus.SUPPORTED_COMPLETE;
import static net.soliddesign.iumpr.bus.j1939.packets.DM5MonitoredSystemStatus.SUPPORTED_NOT_COMPLETE;
import static net.soliddesign.iumpr.bus.j1939.packets.DM5MonitoredSystemStatus.valueOf;
import static net.soliddesign.iumpr.bus.j1939.packets.DM5MonitoredSystemStatus.values;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests the {@link DM5MonitoredSystemStatus} enum
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM5MonitoredSystemStatusTest {

    @Test
    public void testToString() {
        assertEquals("    supported,     complete", SUPPORTED_COMPLETE.toString());
        assertEquals("    supported, not complete", SUPPORTED_NOT_COMPLETE.toString());
        assertEquals("not supported,     complete", NOT_SUPPORTED_COMPLETE.toString());
        assertEquals("not supported, not complete", NOT_SUPPORTED_NOT_COMPLETE.toString());
    }

    @Test
    public void testValueOf() {
        assertEquals(SUPPORTED_COMPLETE, valueOf("SUPPORTED_COMPLETE"));
        assertEquals(SUPPORTED_NOT_COMPLETE, valueOf("SUPPORTED_NOT_COMPLETE"));
        assertEquals(NOT_SUPPORTED_COMPLETE, valueOf("NOT_SUPPORTED_COMPLETE"));
        assertEquals(NOT_SUPPORTED_NOT_COMPLETE, valueOf("NOT_SUPPORTED_NOT_COMPLETE"));
    }

    @Test
    public void testValues() {
        assertEquals(4, values().length);
    }
}
