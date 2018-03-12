package examples;

import ev3dev.sensors.arduino.bn055.BNO055;
import ev3dev.sensors.arduino.bn055.BNO055Listener;
import ev3dev.sensors.arduino.bn055.model.BNO055Response;
import ev3dev.sensors.gps.SerialGPS;
import lombok.extern.slf4j.Slf4j;

public @Slf4j class SerialGPSEventsTest {

	public static void main(String[] args) throws Exception {

		final String port = "/dev/ttyACM0";
		final SerialGPS bno055 = new SerialGPS(port);
		bno055.init();
		bno055.addListener(new BNO055Listener() {

			@Override
			public void dataReceived(BNO055Response response) {
				log.info("{}", response);
			}

		});

		log.info("Start reading");
		for(int x= 0; x<=10; x++){
			log.info("Iteration: {}", x);
			Thread.sleep(1000);
		}

		bno055.close();
		log.info("Closing connection with Arduino");

	}
}