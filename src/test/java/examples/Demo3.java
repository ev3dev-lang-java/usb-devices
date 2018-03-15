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
