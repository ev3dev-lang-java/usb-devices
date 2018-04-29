package examples;

import ev3dev.sensors.slamtec.RPLidarA1;
import ev3dev.sensors.slamtec.model.Scan;
import lombok.extern.slf4j.Slf4j;

public @Slf4j class Demo {

    public static void main(String[] args) throws Exception {

        log.info("Testing RPLidar on a EV3 Brick with Java");
        final String USBPort = "/dev/ttyUSB0";
        final RPLidarA1 lidar = new RPLidarA1(USBPort);
        lidar.init();

        for(int x = 0; x <= 5; x++){

            final Scan scan = lidar.scan();
            log.info("Iteration: {}, Measures: {}", x, scan.getDistances().size());
            scan.getDistances()
                .stream()
                .filter((measure) -> measure.getQuality() > 10)
                .filter((measure) -> (measure.getAngle() >= 345 || measure.getAngle() <= 15))
                .filter((measure) -> measure.getDistance() <= 50)
                .forEach(System.out::println);
        }

        lidar.close();
        log.info("End demo");
        System.exit(0);
    }
}
