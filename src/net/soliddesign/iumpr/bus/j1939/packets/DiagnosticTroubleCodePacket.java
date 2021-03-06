/**
 * Copyright 2017 Equipment & Tool Institute
 */
package net.soliddesign.iumpr.bus.j1939.packets;

import static net.soliddesign.iumpr.IUMPR.NL;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.soliddesign.iumpr.bus.Packet;

/**
 * Class that represents a packet that contains Diagnostic Trouble Codes
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DiagnosticTroubleCodePacket extends ParsedPacket {

    private LampStatus awlStatus;
    private List<DiagnosticTroubleCode> dtcs;
    private LampStatus milStatus;
    private LampStatus plStatus;
    private LampStatus rslStatus;

    /**
     * Constructor
     *
     * @param packet
     *            the {@link Packet} to parse
     */
    public DiagnosticTroubleCodePacket(Packet packet) {
        super(packet);
    }

    /**
     * Returns the Amber Warning Lamp (AWL) Status
     *
     * @return {@link LampStatus}
     */
    public LampStatus getAmberWarningLampStatus() {
        if (awlStatus == null) {
            awlStatus = getLampStatus(0x0C, 2);
        }
        return awlStatus;
    }

    /**
     * Returns the {@link List} of {@link DiagnosticTroubleCode}. If the only
     * "DTC" has an SPN of 0 or 524287, the list will be empty, but never null
     *
     * @return List of DTCs
     */
    public List<DiagnosticTroubleCode> getDtcs() {
        if (dtcs == null) {
            dtcs = parseDTCs();
        }
        return dtcs;
    }

    /**
     * Helper method to get a {@link LampStatus}
     *
     * @param mask
     *            the bit mask
     * @param shift
     *            the number of bits to shift to the right
     * @return the {@link LampStatus} that corresponds to the value
     */
    private LampStatus getLampStatus(int mask, int shift) {
        int onOff = getShaveAndAHaircut(0, mask, shift);
        int flash = getShaveAndAHaircut(1, mask, shift);
        return LampStatus.getStatus(onOff, flash);
    }

    /**
     * Returns the Malfunction Indicator Lamp (MIL) Status
     *
     * @return {@link LampStatus}
     */
    public LampStatus getMalfunctionIndicatorLampStatus() {
        if (milStatus == null) {
            milStatus = getLampStatus(0xC0, 6);
        }
        return milStatus;
    }

    @Override
    public String getName() {
        return "DM";
    }

    /**
     * Returns the Protect Lamp Status
     *
     * @return {@link LampStatus}
     */
    public LampStatus getProtectLampStatus() {
        if (plStatus == null) {
            plStatus = getLampStatus(0x03, 0);
        }
        return plStatus;
    }

    /**
     * Returns the Red Stop Lamp (RSL) Status
     *
     * @return {@link LampStatus}
     */
    public LampStatus getRedStopLampStatus() {
        if (rslStatus == null) {
            rslStatus = getLampStatus(0x30, 4);
        }
        return rslStatus;
    }

    private DiagnosticTroubleCode parseDTC(int i) {
        int[] data = getPacket().getData(i, i + 4);
        return new DiagnosticTroubleCode(data);
    }

    /**
     * Parses the data to create a {@link List} of {@link DiagnosticTroubleCode}
     *
     * @param data
     *            the {@link Packet} data to parse
     * @return List
     */
    private List<DiagnosticTroubleCode> parseDTCs() {
        List<DiagnosticTroubleCode> dtcs = new ArrayList<>();
        // Every 4 bytes is a DTC; there might be two extra bytes
        final int length = getPacket().getLength();
        for (int i = 2; i + 4 <= length; i = i + 4) {
            DiagnosticTroubleCode dtc = parseDTC(i);
            final int spn = dtc.getSuspectParameterNumber();
            if (spn != 0 && spn != 524287) {
                dtcs.add(dtc);
            }
        }
        return dtcs;
    }

    @Override
    public String toString() {
        String result = getStringPrefix() + "MIL: " + getMalfunctionIndicatorLampStatus() + ", RSL: "
                + getRedStopLampStatus() + ", AWL: " + getAmberWarningLampStatus() + ", PL: " + getProtectLampStatus()
                + NL;

        if (getDtcs().isEmpty()) {
            result += "No DTCs";
        } else {
            String joinedDtcs = getDtcs().stream().map(DiagnosticTroubleCode::toString).collect(Collectors.joining(NL));
            result += joinedDtcs;
        }

        return result;
    }
}
