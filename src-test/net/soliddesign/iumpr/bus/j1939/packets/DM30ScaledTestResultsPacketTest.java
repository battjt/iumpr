/**
 * Copyright 2017 Equipment & Tool Institute
 */
package net.soliddesign.iumpr.bus.j1939.packets;

import static net.soliddesign.iumpr.IUMPR.NL;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import net.soliddesign.iumpr.bus.Packet;

/**
 * Unit test for the {@link DM30ScaledTestResultsPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM30ScaledTestResultsPacketTest {

    @Test
    public void testPGN() {
        assertEquals(41984, DM30ScaledTestResultsPacket.PGN);
    }

    @Test
    public void testWithOne() {
        int[] data = new int[] { 0xF7, 0x22, 0x0D, 0x1F, 0xD0, 0x00, 0xB7, 0x03, 0xE8, 0x03, 0x20, 0x03 };
        Packet packet = Packet.create(0, 0, data);
        DM30ScaledTestResultsPacket instance = new DM30ScaledTestResultsPacket(packet);
        final List<ScaledTestResult> testResults = instance.getTestResults();
        assertEquals(1, testResults.size());
        {
            ScaledTestResult testResult = testResults.get(0);
            String expected = "Test 247: Aftertreatment 1 Diesel Exhaust Fluid Dosing Unit 1 Input Lines (3362), Condition Exists (31), Result: Test Passed. Min: 800, Value: 951, Max: 1,000";
            assertEquals(expected, testResult.toString());
        }

        String expected = "DM30 from Engine #1 (0): Test 247: Aftertreatment 1 Diesel Exhaust Fluid Dosing Unit 1 Input Lines (3362), Condition Exists (31), Result: Test Passed. Min: 800, Value: 951, Max: 1,000";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testWithThree() {
        int[] data = new int[] { 0xF7, 0xD4, 0x02, 0x14, 0x3E, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF7, 0xD4,
                0x02, 0x15, 0x3E, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF7, 0xD4, 0x02, 0x02, 0x84, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00 };
        Packet packet = Packet.create(0, 0, data);
        DM30ScaledTestResultsPacket instance = new DM30ScaledTestResultsPacket(packet);
        final List<ScaledTestResult> testResults = instance.getTestResults();
        assertEquals(3, testResults.size());
        {
            ScaledTestResult testResult = testResults.get(0);
            String expected = "Test 247: O2 Sensor (724), Data Drifted High (20), Result: Test Passed. Min: -3.92, Value: -3.92, Max: -3.92";
            assertEquals(expected, testResult.toString());
        }
        {
            ScaledTestResult testResult = testResults.get(1);
            String expected = "Test 247: O2 Sensor (724), Data Drifted Low (21), Result: Test Passed. Min: -3.92, Value: -3.92, Max: -3.92";
            assertEquals(expected, testResult.toString());
        }
        {
            ScaledTestResult testResult = testResults.get(2);
            String expected = "Test 247: O2 Sensor (724), Data Erratic, Intermittent Or Incorrect (2), Result: Test Passed. Min: 0 ms, Value: 0 ms, Max: 0 ms";
            assertEquals(expected, testResult.toString());
        }

        String expected = "DM30 from Engine #1 (0): [" + NL
                + "  Test 247: O2 Sensor (724), Data Drifted High (20), Result: Test Passed. Min: -3.92, Value: -3.92, Max: -3.92"
                + NL
                + "  Test 247: O2 Sensor (724), Data Drifted Low (21), Result: Test Passed. Min: -3.92, Value: -3.92, Max: -3.92"
                + NL
                + "  Test 247: O2 Sensor (724), Data Erratic, Intermittent Or Incorrect (2), Result: Test Passed. Min: 0 ms, Value: 0 ms, Max: 0 ms"
                + NL + "]";
        assertEquals(expected, instance.toString());
    }

}
