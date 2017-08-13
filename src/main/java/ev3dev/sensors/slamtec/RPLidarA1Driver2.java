package ev3dev.sensors.slamtec;

import ev3dev.sensors.slamtec.model.Scan;
import ev3dev.sensors.slamtec.model.ScanDistance;
import ev3dev.sensors.slamtec.service.RpLidarHighLevelDriver;
import ev3dev.sensors.slamtec.service.RpLidarScan;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j class RPLidarA1Driver2 implements RPLidarProvider {

    private boolean initSuccess = false;
    private AtomicBoolean closingStatus;

    private RpLidarHighLevelDriver driver = null;
    private final String USBPort;

    private int counter = 0;
    private boolean flag = false;
    private List<ScanDistance> distancesTemp = Collections.synchronizedList(new ArrayList<>());
    private Scan scan;

    private final List<RPLidarProviderListener> listenerList = Collections.synchronizedList(new ArrayList());

    public RPLidarA1Driver2(final String USBPort) {
        this.USBPort = USBPort;
        this.closingStatus = new AtomicBoolean(false);
        driver = new RpLidarHighLevelDriver();
        if(log.isInfoEnabled()){
            log.info("Starting a RPLidar instance");
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
            this.setPortProperty();

            initSuccess = driver.initialize(this.USBPort, 100);
            //Let's just pretend it worked...
            initSuccess = true;
        //TODO Improve this Exception handling
        } catch (Exception e) {
            throw new RPLidarA1ServiceException(e);
        }
    }

    private void setPortProperty(){
        final String ports = System.getProperty("gnu.io.rxtx.SerialPorts");
        final String newPorts = ((ports == null) ? "" : (ports + ":")) + this.USBPort;
        System.setProperty("gnu.io.rxtx.SerialPorts", newPorts);
    }

    @Override
    public Scan scan() throws RPLidarA1ServiceException {

        //int scanCount = 0;
        if(initSuccess) {
            RpLidarScan scan = new RpLidarScan();
            if (!driver.blockCollectScan(scan, 0)) { //10000
                log.debug("Scan wasn't ready yet");
            } else {

                synchronized (this){
                    //final List<ScanDistance> distances = new ArrayList<>();

                    for (int x : scan.used.data) {
                        distancesTemp.add(new ScanDistance(
                                x/64f,
                                scan.distance[x],
                                scan.quality[x],
                                false));
                    }

                    for (RPLidarProviderListener listener : listenerList) {
                        listener.scanFinished(new Scan(distancesTemp));
                    }
                }

            }
        }else {
            //Lidar not initialized
        }

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
        driver.stop();
        initSuccess = false;
    }

    @Override
    public void addListener(RPLidarProviderListener listener) {
        listenerList.add(listener);
    }

    @Override
    public void removeListener(RPLidarProviderListener listener) {
        listenerList.remove(listener);
    }

}
