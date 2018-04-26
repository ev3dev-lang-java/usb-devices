# usb-devices

A Java library to manage USB devices like LIDARs, Arduino boards, IMUs, GPS, etc...

## Devices supported

## 1. 2D LIDAR

The library supports SLAMTEC A1 & A2 models

![](https://raw.githubusercontent.com/ev3dev-lang-java/usb-devices/develop/docs/images/rplidar_a2.png)

Example using a RPLIDAR A2

``` java
package examples;

import ev3dev.sensors.slamtec.RPLidarA1;
import ev3dev.sensors.slamtec.RPLidarProviderListener;
import ev3dev.sensors.slamtec.model.Scan;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

public @Slf4j class Demo3 {

    private static AtomicInteger counter;

    public static void main(String[] args) throws Exception {

        log.info("Testing RPLidar on a EV3Dev with Java");
        final String USBPort = "/dev/ttyUSB0";
        final RPLidarA1 lidar = new RPLidarA1(USBPort);
        lidar.init();
        lidar.addListener(new RPLidarProviderListener() {
            @Override
            public void scanFinished(Scan scan) {
                //log.info("Iteration: {}, Measures: {}", counter.incrementAndGet(), scan.getDistances().size());
                log.info("Measures: {}", scan.getDistances().size());
                scan.getDistances()
                        .stream()
                        .filter((measure) -> measure.getQuality() > 10)
                        .filter((measure) -> (measure.getAngle() >= 345 || measure.getAngle() <= 15))
                        .filter((measure) -> measure.getDistance() <= 50)
                        .forEach(System.out::println);
            }
        });
        for(int x = 0; x <= 10; x++) {
            lidar.scan();
        }
        lidar.close();
        log.info("End");
        System.exit(0);
    }

}

```

## 2. Arduino

### Arduino 9 axes motion shield (BNO055)

http://www.arduino.org/products/shields/arduino-9-axes-motion-shield

![](https://raw.githubusercontent.com/ev3dev-lang-java/usb-devices/develop/docs/images/Arduino-9-Axes-Motion-Shield.jpg)

Example using the Arduino + Shield:

``` java

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

```

**Video:**

https://www.youtube.com/watch?v=OY2B7B0Qi2Y

## 3. GPS

Example connecting with a USB GPS

![](https://raw.githubusercontent.com/ev3dev-lang-java/usb-devices/develop/docs/images/usb-gps.jpg)

``` java
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
		for(int x = 0; x <= seconds; x++){
			log.info("Iteration: {} \n\n", x);
			Thread.sleep(1000);
		}
	}
}
```