package ev3dev.sensors.gps;

import ev3dev.sensors.SerialSensor;
import ev3dev.sensors.SerialServiceException;
import ev3dev.sensors.arduino.bn055.BNO055Listener;
import ev3dev.sensors.arduino.bn055.model.*;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;


public @Slf4j class GenericGPS implements SerialSensor, SerialPortEventListener {

	private BufferedReader input;
	private OutputStream output;
	private static final int TIME_OUT = 2000;
	private static final int DATA_RATE = 115200;

	SerialPort serialPort;

	private final String USBPort;

	public GenericGPS(final String USBPort){
		this.USBPort = USBPort;
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
				final String inputLine=input.readLine();

				System.out.println(inputLine);

			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
			}
		}
	}

}