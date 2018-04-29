package ev3dev.sensors.arduino.bn055.model;

import lombok.Data;

/**
 * Created by jabrena on 12/8/17.
 */
public @Data class Euler {

    private final float heading, roll, pitch;

}
