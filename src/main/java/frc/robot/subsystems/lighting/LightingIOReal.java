package frc.robot.subsystems.lighting;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.SerialPort.Port;
import frc.robot.subsystems.lighting.Lighting.LightState;

public class LightingIOReal implements LightingIO {
    /**
     * The I2C device for the lighting arduino.
     */
    private static SerialPort serialLightingArduino = new SerialPort(14400, Port.kUSB2);

    public LightingIOReal() {
    }

    /**
     * Gets a lighting update with the current lighting state.  
     * Message format is 1 byte for the light state, 1 byte for the alliance, and 1 byte for the robot speed
     * (as a ratio of the full speed).
     * Checks and casts are done on both sides.
     * 
     * @param robotSpeed The robot speed in meters per second.
     */
    private byte[] getUpdate(double robotSpeed, LightState lightState) {
        byte[] data = new byte[3];
        data[0] = (byte)lightState.ordinal();

        // This value matches the Arduino's enum definitions for the alliance.
        data[1] = (byte)(DriverStation.getAlliance().orElse(Alliance.Blue) == Alliance.Blue ? 0 : 1);
        // 5.4 meters per second is our maximum speed.
        // This is just a rough conversion to 0-255, which is what we send to the Arduino.
        data[2] = (byte)(robotSpeed / 5.4 * 255);
        return data;
    }

    private int failedCount = 0;

    @Override
    public void setLightState(double robotSpeed, LightState lightState) {
        var data = getUpdate(robotSpeed, lightState);
        int bytesWritten = serialLightingArduino.write(data, data.length);
        if(bytesWritten != data.length && failedCount < 10) {
            failedCount++;
            System.out.println("Failed to write to lighting Arduino");
        }
    }
}
