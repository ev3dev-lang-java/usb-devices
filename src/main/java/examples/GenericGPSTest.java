package examples;

import ev3dev.sensors.gps.GenericGPS;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenericGPSTest {

	public static void main(String[] args) throws Exception {

		final String port = "/dev/ttyACM0";
		final GenericGPS gps = new GenericGPS(port);
		gps.init();

		//This method block main thread 10 seconds
		timeCounter(10);

		gps.close();
		log.info("Closing connection with the USB GPS Device");

		log.info("LAT: {} {}, LAT: {} {}, ALT: {}", gps.getLatitude(), gps.getLatitudeDirection(), gps.getLongitude(), gps.getLongitudeDirection(), gps.getAltitude());
		log.info("DATE: {}, TIMESTAMP: {}", gps.getDate(), gps.getTimeStamp());
		log.info("NSTAT: {}", gps.getSatellitesTracked());

	}

	private static void timeCounter(final int seconds) throws InterruptedException {
		log.info("Start reading");
		for(int x= 0; x<=seconds; x++){
			log.info("Iteration: {} \n\n", x);
			Thread.sleep(1000);
		}
	}
}