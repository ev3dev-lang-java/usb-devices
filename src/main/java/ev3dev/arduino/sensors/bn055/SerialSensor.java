package ev3dev.arduino.sensors.bn055;

public interface SerialSensor {

    void init() throws BNO055ServiceException;
    void close();

    void addListener(BNO055Listener listener);
    void removeListener(BNO055Listener listener);
}
