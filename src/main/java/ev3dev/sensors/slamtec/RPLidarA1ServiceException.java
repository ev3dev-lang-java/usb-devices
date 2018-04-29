package ev3dev.sensors.slamtec;

public class RPLidarA1ServiceException extends Exception {

    public RPLidarA1ServiceException(Exception e) {
        super(e);
    }

    public RPLidarA1ServiceException(String message) {
        super(message);
    }
}
