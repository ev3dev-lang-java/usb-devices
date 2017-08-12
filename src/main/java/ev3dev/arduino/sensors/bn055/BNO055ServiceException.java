package ev3dev.arduino.sensors.bn055;

/**
 * Created by jabrena on 12/8/17.
 */
public class BNO055ServiceException extends Exception {

    public BNO055ServiceException(Exception e) {
        super(e);
    }

    public BNO055ServiceException(String cause) {
        super(cause);
    }
}
