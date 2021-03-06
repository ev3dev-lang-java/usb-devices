package ev3dev.sensors.arduino.bn055;

import ev3dev.sensors.arduino.bn055.model.BNO055Response;

/**
 * This interface model the event when a Scan finished
 */
public interface BNO055Listener {

    void dataReceived(BNO055Response response);

}

