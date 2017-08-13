package ev3dev.sensors.slamtec.service;

/**
 * Listener for client of {@link RpLidarLowLevelDriver}
 *
 * @author Peter Abeles
 */
public interface RpLidarListener {

	void handleMeasurement(RpLidarMeasurement measurement);

	void handleDeviceHealth(RpLidarHeath health);

	void handleDeviceInfo(RpLidarDeviceInfo info);
}
