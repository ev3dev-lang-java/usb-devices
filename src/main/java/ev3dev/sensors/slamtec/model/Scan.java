package ev3dev.sensors.slamtec.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Scan is the object returned by any RPLidar provider.
 * Internally, the class store information about the distances.
 *
 */
public class Scan {

    @Getter
    private final List<ScanDistance> distances;

    /**
     * Constructor. Scan object set in the constructor the distances.
     * @param distances
     */
    public Scan(final List<ScanDistance> distances) {
        this.distances = Collections.synchronizedList(new ArrayList<>());
        this.distances.addAll(distances);
    }

}
