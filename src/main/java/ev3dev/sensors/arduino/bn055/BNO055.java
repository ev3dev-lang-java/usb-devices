package ev3dev.sensors.arduino.bn055;

import ev3dev.sensors.arduino.bn055.model.*;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;


public @Slf4j class BNO055 implements SerialSensor, SerialPortEventListener {

	private BufferedReader input;
	private OutputStream output;
	private static final int TIME_OUT = 2000;
	private static final int DATA_RATE = 115200;

	SerialPort serialPort;

	private final List<BNO055Listener> listenerList = Collections.synchronizedList(new ArrayList());

	private final String USBPort;

	public BNO055(final String USBPort){
		this.USBPort = USBPort;
	}

	private BNO055Response response;

	private void setPortProperty(){
		final String ports = System.getProperty("gnu.io.rxtx.SerialPorts");
		final String newPorts = ((ports == null) ? "" : (ports + ":")) + this.USBPort;
		System.setProperty("gnu.io.rxtx.SerialPorts", newPorts);
	}

	@Override
	public void init() throws BNO055ServiceException {

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
			throw new BNO055ServiceException("Could not find port.");
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
			throw new BNO055ServiceException(e);
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
				final String inputLine=input.readLine();
				final String[] sensorResponseParts = inputLine.split(",");

				if(sensorResponseParts.length > 0){

					Quaternion quaternion = new Quaternion(
							Float.parseFloat(sensorResponseParts[0]),
							Float.parseFloat(sensorResponseParts[1]),
							Float.parseFloat(sensorResponseParts[2]),
							Float.parseFloat(sensorResponseParts[3])
					);

					Euler euler = new Euler(
							Float.parseFloat(sensorResponseParts[4]),
							Float.parseFloat(sensorResponseParts[5]),
							Float.parseFloat(sensorResponseParts[6])
					);

					Acceleration acceleration = new Acceleration(
							Float.parseFloat(sensorResponseParts[7]),
							Float.parseFloat(sensorResponseParts[8]),
							Float.parseFloat(sensorResponseParts[9])
					);

					Magnetometer magnetometer = new Magnetometer(
							Float.parseFloat(sensorResponseParts[10]),
							Float.parseFloat(sensorResponseParts[11]),
							Float.parseFloat(sensorResponseParts[12])
					);

					Gyroscope gyroscope = new Gyroscope(
							Float.parseFloat(sensorResponseParts[13]),
							Float.parseFloat(sensorResponseParts[14]),
							Float.parseFloat(sensorResponseParts[15])
					);

					synchronized (this) {
						final BNO055Response bno055Response = new BNO055Response(
								quaternion,
								euler,
								acceleration,
								magnetometer,
								gyroscope
						);

						response = bno055Response;

						//Events
						for (BNO055Listener listener : listenerList) {
							listener.dataReceived(bno055Response);
						}
					}

				}

			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
			}
		}
	}

	public synchronized BNO055Response getResponse(){
		return response;
	}

	@Override
	public void addListener(BNO055Listener listener) {
		listenerList.add(listener);
	}

	@Override
	public void removeListener(BNO055Listener listener) {
		listenerList.remove(listener);
	}

}