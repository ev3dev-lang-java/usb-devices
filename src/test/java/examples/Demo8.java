package examples;

import ev3dev.sensors.slamtec.RPLidarA1;
import ev3dev.sensors.slamtec.RPLidarProviderListener;
import ev3dev.sensors.slamtec.model.Scan;
import lombok.extern.slf4j.Slf4j;

public @Slf4j class Demo8 {

    private static volatile int samplesPerSecond;

    public static void main(String[] args) throws Exception {

        log.info("Testing RPLidar on a EV3Dev with Java");

        final String USBPort = "/dev/ttyUSB0";
        final RPLidarA1 lidar = new RPLidarA1(USBPort);
        lidar.init();

        lidar.addListener(new RPLidarProviderListener() {

            @Override
            public void scanFinished(final Scan scan) {
                final int counter = scan.getDistances().size();

                log.info("{}",counter);

                /*
                for (ScanDistance scanDistance: scan.getDistances()) {
                    log.info("Angle: {}, Distance: {}, Quality: {}", scanDistance.getAngle(), scanDistance.getDistance(), scanDistance.getQuality());
                }
                */

            }
        });

        int counter = 0;

        boolean flag = true;
        while(flag){

            lidar.scan();

            counter++;
            log.info("Counter: {}", counter);

            if(counter > 10){
                break;
            }
        }

        lidar.close();
        log.info("End demo");
        System.exit(0);
    }
}
