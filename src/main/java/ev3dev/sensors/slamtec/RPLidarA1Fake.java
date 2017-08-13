package ev3dev.sensors.slamtec;

import ev3dev.sensors.slamtec.model.Scan;
import ev3dev.sensors.slamtec.model.ScanDistance;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j class RPLidarA1Fake implements RPLidarProvider {

    public RPLidarA1Fake(final String USBPort){
        readThread = new ReadSerialThread();
        log.trace("Starting a Fake RPLidarA1 Object");
    }

    private ReadSerialThread readThread;

    @Override
    public void init() throws RPLidarA1ServiceException {
        new Thread(readThread).start();
    }

    @Override
    public Scan scan() throws RPLidarA1ServiceException {
        final List<ScanDistance> distances = Collections.synchronizedList(new ArrayList<>());
        for(int angle = 0; angle < 360; angle++){
            distances.add(new ScanDistance(angle, new Float(Math.random() * 4000 + 1), 1, false));
        }
        return new Scan(distances);
    }

    @Override
    public void close() throws RPLidarA1ServiceException {
        readThread.requestStop();
    }

    public class ReadSerialThread implements Runnable {

        final private AtomicBoolean run;
        final private AtomicInteger counter;

        private final List<RPLidarProviderListener> listenerList = Collections.synchronizedList(new ArrayList());

        ReadSerialThread(){
            run = new AtomicBoolean(true);
            counter = new AtomicInteger(0);
        }

        public void addListener(final RPLidarProviderListener listener) {
            synchronized (listenerList){
                listenerList.add(listener);
            }
        }

        public void removeListener(final RPLidarProviderListener listener){
            synchronized (listenerList){
                listenerList.remove(listener);
            }
        }

        public void requestStop() {
            run.getAndSet(true);
        }

        @Override
        public void run() {

            while(run.get()){
                counter.incrementAndGet();
                if(counter.get() > 50){

                    synchronized (listenerList) {
                        for (RPLidarProviderListener listener : listenerList) {
                            try {
                                listener.scanFinished(scan());
                            } catch (RPLidarA1ServiceException e) {
                                log.error(e.getLocalizedMessage());
                                e.printStackTrace();
                            }
                        }
                    }

                    counter.getAndSet(0);
                    log.info("Detected start flag");
                }

                try { TimeUnit.MILLISECONDS.sleep(5); } catch (InterruptedException e) {}
            }

        }
    }

    @Override
    public void addListener(RPLidarProviderListener listener) {
        readThread.addListener(listener);
    }

    @Override
    public void removeListener(RPLidarProviderListener listener) {
        readThread.removeListener(listener);
    }
}
