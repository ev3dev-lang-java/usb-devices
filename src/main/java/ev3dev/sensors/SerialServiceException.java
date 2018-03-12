package ev3dev.sensors;

/**
 * Created by jabrena on 12/8/17.
 */
public class SerialServiceException extends Exception {

    public SerialServiceException(Exception e) {
        super(e);
    }

    public SerialServiceException(String cause) {
        super(cause);
    }
}
