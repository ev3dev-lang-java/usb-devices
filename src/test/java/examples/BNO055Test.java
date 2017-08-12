package examples;

import ev3dev.arduino.sensors.bn055.BNO055;

import lombok.extern.slf4j.Slf4j;

public @Slf4j class BNO055Test {

	public static void main(String[] args) throws Exception {

		final String port = "/dev/ttyACM0";
		final BNO055 bno055 = new BNO055(port);
		bno055.init();

		log.info("Start reading");
		for(int x= 0; x<=100; x++){
			log.info("{}", bno055.getResponse());
		}

		bno055.close();
		log.info("Closing connection with Arduino");

	}
}