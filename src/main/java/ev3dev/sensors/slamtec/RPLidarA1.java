package ev3dev.sensors.slamtec;

import ev3dev.sensors.slamtec.model.Scan;
import lombok.extern.slf4j.Slf4j;

/**
 * RPLidarA1, is the entry point to use this library.
 *
 * This class provide the mechanism to manage a RPLidarA1
 */
public @Slf4j class RPLidarA1 implements RPLidarProvider {

    private final String USBPort;
    private final RPLidarProvider rpLidarProvider;

    public RPLidarA1(final String USBPort) {
        this.USBPort = USBPort;
        this.rpLidarProvider = RPLidarA1Factory.getInstance(USBPort);
    }

    @Override
    public void init() throws RPLidarA1ServiceException {
        rpLidarProvider.init();
    }

    @Override
    public Scan scan() throws RPLidarA1ServiceException {
        return rpLidarProvider.scan();
    }

    @Override
    public void close() throws RPLidarA1ServiceException {
        rpLidarProvider.close();
    }

    @Override
    public void addListener(RPLidarProviderListener listener) {
        rpLidarProvider.addListener(listener);
    }

    @Override
    public void removeListener(RPLidarProviderListener listener) {
        rpLidarProvider.removeListener(listener);
    }
}
