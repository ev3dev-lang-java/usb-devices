# usb-devices
A java libraries to manage USB devices like Controllers, Arduinos, IMUs, GPS, etc...

## Devices supported

### 1. Arduino

#### Arduino 9 axes motion shield (BNO055)

http://www.arduino.org/products/shields/arduino-9-axes-motion-shield

![](https://raw.githubusercontent.com/ev3dev-lang-java/usb-devices/develop/docs/images/Arduino-9-Axes-Motion-Shield.jpg)

Example using the Arduino + Shield:

``` java

package examples;

import ev3dev.actuators.Sound;
import ev3dev.arduino.sensors.bn055.BNO055;
import ev3dev.arduino.sensors.bn055.BNO055Listener;
import ev3dev.arduino.sensors.bn055.model.BNO055Response;
import ev3dev.arduino.sensors.bn055.model.Quaternion;
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

				if(response.getQuaternion() != null){

					final Quaternion quaternion = response.getQuaternion();

					log.debug("Heading: {}", quaternion.getHeading());

					if( (quaternion.getHeading() > 90.0f) &&
						(quaternion.getHeading() <= 100.00f)) {

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

```

**Video:**

https://www.youtube.com/watch?v=OY2B7B0Qi2Y