package ev3dev.sensors.slamtec.model;

import lombok.Value;

/**
 * Scan distances store information about:
 *
 * - angle measured
 * - distance from the object
 * - quality of the measure
 * - start flag
 *
 */
@Value
public class ScanDistance {

    private final float angle;
    private final float distance;
    private final int quality;
    private final boolean start;
}
