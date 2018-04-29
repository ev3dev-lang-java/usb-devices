package slamtec;

import ev3dev.sensors.slamtec.RPLidarA1;
import ev3dev.sensors.slamtec.RPLidarA1Factory;
import ev3dev.sensors.slamtec.RPLidarProviderListener;
import ev3dev.sensors.slamtec.model.Scan;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

public @Slf4j class RPLidarA1FakeEventsTests implements RPLidarProviderListener {

    @BeforeClass
	public static void runOnceBeforeClass() {
		System.setProperty(RPLidarA1Factory.RPLIDARA1_ENV_KEY, "true");
	}

	@Test
	public void lidarEventTest() throws Exception {

		final String USBPort = "ttyUSB0";
		final RPLidarA1 lidar = new RPLidarA1(USBPort);
        lidar.addListener(this);
		lidar.init();
        Thread.sleep(2000);
        lidar.close();
        log.info("End");
        System.exit(0);
	}

    @Test
    public void lidarEventTest2() throws Exception {

        final String USBPort = "ttyUSB0";
        final RPLidarA1 lidar = new RPLidarA1(USBPort);
        lidar.addListener(new RPLidarProviderListener() {

            @Override
            public void scanFinished(Scan scan) {
                log.info("Measures: {}", scan.getDistances().size());
                scan.getDistances()
                        .stream()
                        .filter((measure) -> measure.getQuality() > 10)
                        .filter((measure) -> (measure.getAngle() >= 345 || measure.getAngle() <= 15))
                        .filter((measure) -> measure.getDistance() <= 50)
                        .forEach(System.out::println);
            }
        });
        lidar.init();
        Thread.sleep(2000);
        lidar.close();
        log.info("End");
        System.exit(0);
    }



    @Override
    public void scanFinished(final Scan scan) {
        final long counter = scan.getDistances()
            .stream()
            .count();
		log.info("Measures: {}", counter);
    }
}