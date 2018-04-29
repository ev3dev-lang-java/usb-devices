package ev3dev.sensors.slamtec.service;

import lombok.extern.slf4j.Slf4j;

/**
 * Packet which describes the sensor's health
 *
 * @author Peter Abeles
 * @author Juan Antonio Breña Moral
 */
public @Slf4j class RpLidarHeath {

	public int status;
	public int error_code;

	public void print() {
		log.info("HEALTH:");
		switch (status) {
			case 0:
				log.info("  Good");
				break;
			case 1:
				log.info("  Warning");
				break;
			case 2:
				log.info("  Error");
				break;
			default:
				log.info("  unknown = " + status);
		}
		log.info("  error_code = " + error_code);
	}
}
