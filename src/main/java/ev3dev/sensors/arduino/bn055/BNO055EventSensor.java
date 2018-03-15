package ev3dev.sensors.arduino.bn055;

import ev3dev.sensors.SerialSensor;

public interface BNO055EventSensor extends SerialSensor {

    void addListener(BNO055Listener listener);
    void removeListener(BNO055Listener listener);
}
