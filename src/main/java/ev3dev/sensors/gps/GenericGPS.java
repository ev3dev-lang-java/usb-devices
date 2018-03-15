package ev3dev.sensors.gps;

import ev3dev.sensors.SerialSensor;
import ev3dev.sensors.SerialServiceException;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import lejos.hardware.gps.*;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.NoSuchElementException;


public @Slf4j class GenericGPS implements SerialSensor, SerialPortEventListener {

	private BufferedReader input;
	private OutputStream output;
	private static final int TIME_OUT = 2000;
	private static final int DATA_RATE = 115200;

	SerialPort serialPort;

	private final String USBPort;

	//Classes which manages GGA, VTG, GSA Sentences
	private GGASentence ggaSentence;
	private VTGSentence vtgSentence;
	private GSASentence gsaSentence;

	//Classes which manages GGA, RMC, VTG, GSV, GSA Sentences
	private RMCSentence rmcSentence;

	//TODO device sends a sequence of complementary gsv sentences
	// this class only remembers the last one
	private GSVSentence gsvSentence;

	//TODO: Replace by LocalDate
	//Date Object with use GGA & RMC Sentence
	private Date date;

	public GenericGPS(final String USBPort){
		this.USBPort = USBPort;

		ggaSentence = new GGASentence();
		vtgSentence = new VTGSentence();
		gsaSentence = new GSASentence();
		rmcSentence = new RMCSentence();
		gsvSentence = new GSVSentence();

		date = new Date();
	}

	private void setPortProperty(){
		final String ports = System.getProperty("gnu.io.rxtx.SerialPorts");
		final String newPorts = ((ports == null) ? "" : (ports + ":")) + this.USBPort;
		System.setProperty("gnu.io.rxtx.SerialPorts", newPorts);
	}

	@Override
	public void init() throws SerialServiceException {

		//System.setProperty("gnu.io.rxtx.SerialPorts", this.USBPort);
		this.setPortProperty();

		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		//First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			if (currPortId.getName().equals(this.USBPort)) {
				portId = currPortId;
				break;
			}
		}

		if (portId == null) {
			log.error("Could not find port.");
			throw new SerialServiceException("Could not find port.");
		}

		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			//TODO: Review??
			//serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			throw new SerialServiceException(e);
		}
	}

	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	@Override
	public synchronized void serialEvent(SerialPortEvent oEvent) {

		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {

			try {
				String s = input.readLine();

				log.trace("Sentence: {}", s);

				// Check if sentence is valid:
				if (!s.startsWith("$"))
					return;

				int p = s.lastIndexOf('*');
				if (p < 0)
					return;

				//XOR all characters between $ and *
				int checksum1 = 0;
				for (int i=1; i<p; i++)
					checksum1 ^= s.charAt(i);

				try{
					int checksum2 = Integer.parseInt(s.substring(p+1), 16);
					if (checksum1 != checksum2)
						return;

					s = s.substring(0, p);
					final int comma = s.indexOf(',');
					final String token = s.substring(0,comma);

					sentenceChooser(token, s);

				} catch(NoSuchElementException e) {
					log.error(e.getLocalizedMessage());
				} catch(StringIndexOutOfBoundsException e) {
					log.error(e.getLocalizedMessage());
				} catch(ArrayIndexOutOfBoundsException e) {
					log.error(e.getLocalizedMessage());
				}

			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
			}
		}
	}

	/**
	 * Internal helper method to aid in the subclass architecture. Overwritten by subclass.
	 * @param header
	 * @param s
	 */
	protected void sentenceChooser(String header, String s) {
		if (header.equals(GGASentence.HEADER)){
			this.ggaSentence.parse(s);
		}else if (header.equals(VTGSentence.HEADER)){
			this.vtgSentence.parse(s);
		}else if (header.equals(GSASentence.HEADER)){
			gsaSentence.parse(s);
		}
	}

	/**
	 * Get Latitude
	 *
	 * @return the latitude
	 */
	public double getLatitude() {
		return ggaSentence.getLatitude();
	}


	/**
	 * Get Latitude Direction
	 *
	 * @return the latitude direction
	 */
	public char getLatitudeDirection(){
		return ggaSentence.getLatitudeDirection();
	}


	/**
	 * Get Longitude
	 *
	 * @return the longitude
	 */
	public double getLongitude() {
		return ggaSentence.getLongitude();
	}

	/**
	 * Get Longitude Direction
	 *
	 * @return the longitude direction
	 */
	public char getLongitudeDirection(){
		return ggaSentence.getLongitudeDirection();
	}


	/**
	 * The altitude above mean sea level
	 *
	 * @return Meters above sea level e.g. 545.4
	 */
	public float getAltitude(){
		return ggaSentence.getAltitude();
	}

	/**
	 * Returns the number of satellites being tracked to
	 * determine the coordinates.
	 * @return Number of satellites e.g. 8
	 */
	public int getSatellitesTracked(){
		return ggaSentence.getSatellitesTracked();
	}

	/**
	 * Fix quality:
	 * <li>0 = invalid
	 * <li>1 = GPS fix (SPS)
	 * <li>2 = DGPS fix
	 * <li>3 = PPS fix
	 * <li>4 = Real Time Kinematic
	 * <li>5 = Float RTK
	 * <li>6 = estimated (dead reckoning) (2.3 feature)
	 * <li>7 = Manual input mode
	 * <li>8 = Simulation mode
	 *
	 * @return the fix quality
	 */
	public int getFixMode(){
		return ggaSentence.getFixQuality();
	}

	/**
	 * Get the last time stamp from the satellite for GGA sentence.
	 *
	 * @return Time as a UTC integer. 123459 = 12:34:59 UTC
	 */
	public int getTimeStamp() {
		return ggaSentence.getTime();
	}

	/**
	 * Get speed in kilometers per hour
	 *
	 * @return the speed in kilometers per hour
	 */
	public float getSpeed() {
		return vtgSentence.getSpeed();
	}

	/**
	 * Get the course heading of the GPS unit.
	 * @return course (0.0 to 360.0)
	 */
	public float getCourse() {
		return vtgSentence.getTrueCourse();
	}

	/**
	 * Selection type of 2D or 3D fix
	 * <li> 'M' = manual
	 * <li> 'A' = automatic
	 * @return selection type - either 'A' or 'M'
	 */
	public String getSelectionType(){
		return gsaSentence.getMode();
	}

	/**
	 *  3D fix - values include:
	 *  <li>1 = no fix
	 *  <li>2 = 2D fix
	 *  <li>3 = 3D fix
	 *
	 * @return fix type (1 to 3)
	 */
	public int getFixType(){
		return gsaSentence.getModeValue();
	}

	/**
	 * Get an Array of Pseudo-Random Noise codes (PRN). You can look up a list of GPS satellites by
	 * this number at: http://en.wikipedia.org/wiki/List_of_GPS_satellite_launches
	 * Note: This number might be similar or identical to SVN.
	 *
	 * @return array of PRNs
	 */
	public int[] getPRN(){
		return gsaSentence.getPRN();
	}

	/**
	 * Get the 3D Position Dilution of Precision (PDOP). When visible GPS satellites are close
	 * together in the sky, the geometry is said to be weak and the DOP value is high; when far
	 * apart, the geometry is strong and the DOP value is low. Thus a low DOP value represents
	 * a better GPS positional accuracy due to the wider angular separation between the
	 * satellites used to calculate a GPS unit's position. Other factors that can increase
	 * the effective DOP are obstructions such as nearby mountains or buildings.
	 *
	 * @return The PDOP (PDOP * 6 meters = the error to expect in meters) -1 means PDOP is unavailable from the GPS.
	 */
	public float getPDOP(){
		return gsaSentence.getPDOP();
	}

	/**
	 * Get the Horizontal Dilution of Precision (HDOP). When visible GPS satellites are close
	 * together in the sky, the geometry is said to be weak and the DOP value is high; when far
	 * apart, the geometry is strong and the DOP value is low. Thus a low DOP value represents
	 * a better GPS positional accuracy due to the wider angular separation between the
	 * satellites used to calculate a GPS unit's position. Other factors that can increase
	 * the effective DOP are obstructions such as nearby mountains or buildings.
	 *
	 * @return the HDOP (HDOP * 6 meters = the error to expect in meters) -1 means HDOP is unavailable from the GPS.
	 */
	public float getHDOP(){
		return gsaSentence.getHDOP();
	}

	/**
	 * Get the Vertical Dilution of Precision (VDOP). When visible GPS satellites are close
	 * together in the sky, the geometry is said to be weak and the DOP value is high; when far
	 * apart, the geometry is strong and the DOP value is low. Thus a low DOP value represents
	 * a better GPS positional accuracy due to the wider angular separation between the
	 * satellites used to calculate a GPS unit's position. Other factors that can increase
	 * the effective DOP are obstructions such as nearby mountains or buildings.
	 *
	 * @return the VDOP (VDOP * 6 meters = the error to expect in meters) -1 means VDOP is unavailable from the GPS.
	 */
	public float getVDOP(){
		return gsaSentence.getVDOP();
	}


	/**
	 * Return Compass Degrees
	 * in a range: 0.0-359.9
	 *
	 * @return the compass degrees
	 */
	public float getCompassDegrees(){
		return rmcSentence.getCompassDegrees();
	}

	/**
	 * Return a Date Object with data from GGA and RMC NMEA Sentence
	 *
	 * @return the date
	 */
	public Date getDate(){
		// TODO: Would be more proper to return a new Date object instead of recycled Date.
		updateDate();
		updateTime();
		return date;
	}

	/**
	 * Update Time values
	 */
	private void updateTime(){

		int timeStamp = ggaSentence.getTime();

		if(timeStamp >0) {
			int hh = timeStamp / 10000;
			int mm = (timeStamp / 100) % 100;
			int ss = timeStamp % 100;

			date.setHours(hh);
			date.setMinutes(mm);
			date.setSeconds(ss);
		}
	}

	/**
	 * Update Date values
	 */
	private void updateDate(){
		int dateStamp = rmcSentence.getDate();

		if(dateStamp > 0) {
			int dd = dateStamp / 10000;
			int mm = (dateStamp / 100) % 100;
			int yy = dateStamp % 100;

			date.setDate(dd);
			date.setMonth(mm);
			date.setYear(yy);
		}
	}

	/**
	 *
	 * Get NMEA Satellite. The satellite list is retrieved from the almanac data. Satellites are
	 * ordered by their elevation: highest elevation (index 0) -> lowest elevation.
	 *
	 * @param index the satellite index
	 * @return the NMEASaltellite object for the selected satellite
	 */
	public Satellite getSatellite(int index){
		Satellite s = gsvSentence.getSatellite(index);
		// Compare getPRN() with this satellite, fill in setTracked():
		// TODO: This fails because most satellites are set to 0 when this is called. Not synced yet.
		boolean tracked = false;
		int [] prns = getPRN();
		for(int i=0;i<prns.length;i++) {
			if(prns[i] == s.getPRN()) {
				tracked=true;
				break;
			}
		}
		s.setTracked(tracked);
		return s;
	}

	/**
	 * The satellites in view is a list of satellites the GPS could theoretically connect to (i.e. satellites that
	 * are not over the earth's horizon). The getSatellitesInView() method will always return an equal or greater
	 * number than getSatellitesTracked().
	 *
	 * @return Number of satellites e.g. 8
	 */
	public int getSatellitesInView(){
		return gsvSentence.getSatellitesInView();
	}
}