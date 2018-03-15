package ev3dev.sensors;

public interface SerialSensor {

    void init() throws SerialServiceException;
    void close();

}
