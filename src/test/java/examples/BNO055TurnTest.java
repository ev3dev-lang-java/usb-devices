package examples;

import ev3dev.actuators.Sound;
import ev3dev.arduino.sensors.bn055.BNO055;
import ev3dev.arduino.sensors.bn055.BNO055Listener;
import ev3dev.arduino.sensors.bn055.model.BNO055Response;
import ev3dev.arduino.sensors.bn055.model.Euler;
import ev3dev.arduino.sensors.bn055.model.Euler;
import ev3dev.sensors.Battery;
import ev3dev.sensors.Button;
import lombok.extern.slf4j.Slf4j;

public @Slf4j class BNO055TurnTest {

	public static void main(String[] args) throws Exception {

		final String port = "/dev/ttyACM0";
		final BNO055 bno055 = new BNO055(port);
		bno055.init();

		log.debug("{}", Battery.getInstance().getVoltage());

		bno055.addListener(new BNO055Listener() {

			@Override
			public void dataReceived(final BNO055Response response) {

				if(response.getEuler() != null){

					final Euler euler = response.getEuler();

					log.debug("Heading: {}", euler.getHeading());

					if( (euler.getHeading() > 90.0f) &&
						(euler.getHeading() <= 100.00f)) {

						//Sound.getInstance().beep();
						log.info("REACHED");
					}
				}
			}

		});

		Button.waitForAnyPress();
		log.debug("{}", Battery.getInstance().getVoltage());
		bno055.close();
		log.info("Closing connection with Arduino");

	}
}