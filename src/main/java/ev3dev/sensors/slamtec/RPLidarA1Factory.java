package ev3dev.sensors.slamtec;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

public @Slf4j class RPLidarA1Factory {

    public static final String RPLIDARA1_ENV_KEY = "RPLIDAR_MODE";

    public static RPLidarProvider getInstance(final String USBPort) {

        final String VALUE = System.getProperty(RPLIDARA1_ENV_KEY);
        if(Objects.nonNull(VALUE)){
            return new RPLidarA1Fake(USBPort);
        }

        return new RPLidarA1Driver2(USBPort);
    }
}
