package ev3dev.sensors.arduino.bn055.model;

import lombok.Data;

/**
 * Created by jabrena on 12/8/17.
 */
public @Data class BNO055Response {

    private final Quaternion quaternion;
    private final Euler euler;
    private final Acceleration acceleration;
    private final Magnetometer magnetometer;
    private final Gyroscope gyroscope;

}
