package slamtec;

import ev3dev.sensors.slamtec.RPLidarA1;
import ev3dev.sensors.slamtec.RPLidarA1Factory;
import ev3dev.sensors.slamtec.RPLidarProvider;
import ev3dev.sensors.slamtec.model.Scan;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

public @Slf4j class RPLidarA1FakeTests {

	@BeforeClass
	public static void runOnceBeforeClass() {
		System.setProperty(RPLidarA1Factory.RPLIDARA1_ENV_KEY, "true");
	}

	@Test
	public void getDistanceStreamTest() throws Exception {

		final String USBPort = "ttyUSB0";
		final RPLidarA1 lidar = new RPLidarA1(USBPort);
		lidar.init();
		lidar.scan().getDistances()
			.stream()
			.forEach(System.out::println);
		lidar.close();
	}

	@Test
	public void return360DistanceTest() throws Exception {

		final String USBPort = "ttyUSB0";
		final RPLidarA1 lidar = new RPLidarA1(USBPort);
		lidar.init();
		final Scan scan = lidar.scan();
		lidar.close();

		assertThat(scan, is(notNullValue()));
		assertThat(scan.getDistances().size(), is(lessThanOrEqualTo(RPLidarProvider.SCAN_DEGREES)));
	}

}