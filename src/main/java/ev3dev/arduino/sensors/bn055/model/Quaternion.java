package ev3dev.arduino.sensors.bn055.model;

import lombok.Data;

/**
 * Created by jabrena on 12/8/17.
 */
public @Data class Quaternion {

    private final float heading, roll, pitch;
    
}
