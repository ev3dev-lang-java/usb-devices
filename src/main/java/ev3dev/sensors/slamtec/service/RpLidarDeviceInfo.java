package ev3dev.sensors.slamtec.service;

import lombok.extern.slf4j.Slf4j;

/**
 * Contains information about the device
 *
 * @author Peter Abeles
 * @author Juan Antonio Breña Moral
 */
public @Slf4j class RpLidarDeviceInfo {

	public int model;
	public int firmware_minor;
	public int firmware_major;
	public int hardware;
	public byte[] serialNumber = new byte[16];

	public void print() {

		log.info("DEVICE INFO");
		log.info("  model = " + model);
		log.info("  firmware_minor = " + firmware_minor);
		log.info("  firmware_major = " + firmware_major);
		log.info("  hardware = " + hardware);

		final StringBuilder sb = new StringBuilder();
		sb.append("  Serial = ");
		for (int i = 0; i < serialNumber.length; i++) {
			sb.append(String.format("%02X", serialNumber[i]));
			if ((i + 1) % 4 == 0)
				sb.append(" ");
		}
		log.info(sb.toString());
	}
}
