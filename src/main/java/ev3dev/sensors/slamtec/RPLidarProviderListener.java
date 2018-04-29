package ev3dev.sensors.slamtec;

import ev3dev.sensors.slamtec.model.Scan;

/**
 * This interface model the event when a Scan finished
 */
public interface RPLidarProviderListener {

    void scanFinished(Scan scan);

}

