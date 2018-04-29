package ev3dev.sensors.slamtec;

import ev3dev.sensors.slamtec.model.Scan;
import ev3dev.sensors.slamtec.model.ScanDistance;
import ev3dev.sensors.slamtec.service.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j class RPLidarA1Driver implements RPLidarProvider, RpLidarListener {

    private AtomicBoolean closingStatus;

    private RpLidarLowLevelDriver driver;
    private final String USBPort;

    private int counter = 0;
    private boolean flag = false;
    private List<ScanDistance> distancesTemp = Collections.synchronizedList(new ArrayList<>());
    private Scan scan;

    private final List<RPLidarProviderListener> listenerList = Collections.synchronizedList(new ArrayList());

    public RPLidarA1Driver(final String USBPort) {
        this.USBPort = USBPort;
        this.closingStatus = new AtomicBoolean(false);
        if(log.isInfoEnabled()){
            log.info("Starting a RPLidarA1 instance");
        }
    }

    @Override
    public void init() throws RPLidarA1ServiceException {

        if(log.isInfoEnabled()){
            log.info("Connecting with: {}", this.USBPort);
        }
        File f = new File(this.USBPort);
        if(!f.exists() || f.isDirectory()) {
            log.error("This device is not valid: {}", this.USBPort);
            throw new RPLidarA1ServiceException("This device is not valid: " + this.USBPort);
        }

        try {
            driver = new RpLidarLowLevelDriver(this.USBPort, this);
        //TODO Improve this Exception handling
        } catch (Exception e) {
            throw new RPLidarA1ServiceException(e);
        }
        closingStatus = new AtomicBoolean(false);
        driver.setVerbose(false);
        driver.sendReset();

        //for v2 only - I guess this command is ignored by v1
        driver.sendStartMotor(660);

        driver.pause(200);
    }

    @Override
    public Scan scan() throws RPLidarA1ServiceException {

        driver.sendScan(300);
        driver.pause(700);

        final List<ScanDistance> distances = new ArrayList<>();
        synchronized(distancesTemp){
            distances.addAll(distancesTemp);
            distancesTemp.clear();
        }
        distances.sort(Comparator.comparing(ScanDistance::getAngle));
        return new Scan(Collections.unmodifiableList(distances));
    }

    @Override
    public void close() throws RPLidarA1ServiceException {
        closingStatus = new AtomicBoolean(true);
        driver.sendStopMotor();
        driver.shutdown();
        driver.pause(100);
    }

    @Override
    public void addListener(RPLidarProviderListener listener) {
        listenerList.add(listener);
    }

    @Override
    public void removeListener(RPLidarProviderListener listener) {
        listenerList.remove(listener);
    }

//    @Override
//    public void handleMeasurement(RpLidarMeasurement measurement) {
//
//        if(!this.closingStatus) {
//
//            //TODO This conversion should be incorporated in RpLidarLowLevelDriver
//            int angle = new Double(measurement.angle / 64.0).intValue();
//            double distance = (measurement.distance / 4.0) / 10.0;
//
//            if(!this.containAngle(distancesTemp, angle)){
//                distancesTemp.add(new ScanDistance(angle, distance, measurement.quality, measurement.start));
//            }
//        }
//    }

    @Override
    public void handleMeasurement(final RpLidarMeasurement measurement) {

        if(!closingStatus.get()){

            if(flag){
                if(measurement.start){
                    log.trace("{}", counter);
                    synchronized (distancesTemp) {
                        final List<ScanDistance> distances = new ArrayList<>();
                        distances.addAll(distancesTemp);
                        distancesTemp.clear();
                        scan = new Scan(distances);

                        for (RPLidarProviderListener listener : listenerList) {
                            listener.scanFinished(new Scan(distances));
                        }

                    }

                    counter= 0;
                    flag=false;
                }
            }

            if (measurement.start) {
                flag = true;
            }

            if(flag){
                counter++;
                int angle = new Float(measurement.angle / 64.0f).intValue();
                float distance = (measurement.distance / 4.0f) / 10.0f;
                distancesTemp.add(new ScanDistance(angle, distance, measurement.quality, measurement.start));
            }
        }

    }

    public boolean containAngle(final List<ScanDistance> list, final int angle){
        return list.stream().filter(o -> o.getAngle() == angle).findFirst().isPresent();
    }

    //Not used at the moment

    @Override
    public void handleDeviceHealth(RpLidarHeath health) {

    }

    @Override
    public void handleDeviceInfo(RpLidarDeviceInfo info) {

    }
}
