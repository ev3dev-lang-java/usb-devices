package examples;

import ev3dev.actuators.Sound;
import ev3dev.arduino.sensors.bn055.BNO055;
import ev3dev.arduino.sensors.bn055.BNO055Listener;
import ev3dev.arduino.sensors.bn055.model.BNO055Response;
import ev3dev.arduino.sensors.bn055.model.Euler;
import ev3dev.arduino.sensors.bn055.model.Euler;
import ev3dev.sensors.Button;
import lombok.extern.slf4j.Slf4j;

public @Slf4j class BNO055TurnTest2 {

	public static void main(String[] args) throws Exception {

		final String port = "/dev/ttyACM0";
		final BNO055 bno055 = new BNO055(port);
		bno055.init();

		log.info("Start reading");
		for(int x= 0; x<=100; x++){

			final Euler euler = bno055.getResponse().getEuler();

			log.debug("Iteration: {}", x);
			log.debug("Heading: {}", euler.getHeading());

			if(euler.getHeading() == 0.0f){
				log.debug("Detected 0.0");
			}

			if(euler.getHeading() >= 90.0f){
				log.debug("Detected 90.00");
				Sound.getInstance().beep();
				break;
			}

			Thread.sleep(100);
		}

		log.info("Closing connection with Arduino");
		bno055.close();
		System.exit(0);

	}
}