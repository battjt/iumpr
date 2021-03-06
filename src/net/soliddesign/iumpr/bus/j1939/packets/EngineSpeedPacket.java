/**
 * Copyright 2017 Equipment & Tool Institute
 */
package net.soliddesign.iumpr.bus.j1939.packets;

import net.soliddesign.iumpr.bus.Packet;

/**
 * The {@link ParsedPacket} responsible for translating Engine Speed (SPN 190)
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class EngineSpeedPacket extends ParsedPacket {

    public static final int PGN = 61444;

    private final double engineSpeed;

    public EngineSpeedPacket(Packet packet) {
        super(packet);
        engineSpeed = getScaledShortValue(3, 8.0);
    }

    /**
     * Returns the Engine Speed as Revolutions Per Minute (RPM)
     *
     * @return RPMs of the Engine
     */
    public double getEngineSpeed() {
        return engineSpeed;
    }

    @Override
    public String getName() {
        return "Engine Speed";
    }

    /**
     * Return true if the value returned indicates the Engine Speed Signal is
     * Errored
     *
     * @return boolean
     */
    public boolean isError() {
        return isError(getEngineSpeed());
    }

    /**
     * Returns true if the value returned indicate the Engine could not read the
     * Engine Speed
     *
     * @return boolean
     */
    public boolean isNotAvailable() {
        return isNotAvailable(getEngineSpeed());
    }

    @Override
    public String toString() {
        return getStringPrefix() + getValueWithUnits(getEngineSpeed(), "RPM");
    }

}
