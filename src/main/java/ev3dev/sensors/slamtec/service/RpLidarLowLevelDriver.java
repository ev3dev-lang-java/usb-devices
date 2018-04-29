package ev3dev.sensors.slamtec.service;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Low level service for RPLidar.
 * Just sends and receives packets.
 * Doesn't attempt to filter bad data or care about timeouts.
 *
 * @author Peter Abeles
 */
public @Slf4j class RpLidarLowLevelDriver {

	// out going packet types
	public static final byte SYNC_BYTE0 = (byte) 0xA5;
	public static final byte SYNC_BYTE1 = (byte) 0x5A;
	public static final byte STOP = (byte) 0x25;
	public static final byte RESET = (byte) 0x40;
	public static final byte SCAN = (byte) 0x20;
	public static final byte FORCE_SCAN = (byte) 0x21;
	public static final byte GET_INFO = (byte) 0x50;
	public static final byte GET_HEALTH = (byte) 0x52;

	// in coming packet types
	public static final byte RCV_INFO = (byte) 0x04;
	public static final byte RCV_HEALTH = (byte) 0x06;
	public static final byte RCV_SCAN = (byte) 0x81;
	private static final byte START_MOTOR = (byte) 0xF0;

	SerialPort serialPort;
	InputStream in;
	OutputStream out;

	// buffer for out going data
	byte[] dataOut = new byte[1024];

	// flag to turn on and off verbose debugging output
	boolean verbose = false;

	// thread for reading serial data
	private ReadSerialThread readThread;

	// Storage for incoming packets
	RpLidarHeath health = new RpLidarHeath();
	RpLidarDeviceInfo deviceInfo = new RpLidarDeviceInfo();
	RpLidarMeasurement measurement = new RpLidarMeasurement();
	RpLidarListener listener;

	// if it is in scanning mode.  When in scanning mode it just parses measurement packets
	boolean scanning = false;

	// Type of packet last received
	//int lastReceived = 0;
	volatile int lastReceived = 0;


	/**
	 * Initializes serial connection
	 *
	 * @param portName Path to serial port
	 * @param listener Listener for in comming packets
	 * @throws Exception
	 */
	public RpLidarLowLevelDriver(final String portName, final RpLidarListener listener) throws Exception {

		log.info("Opening port " + portName);

		this.listener = listener;

		//Configuration for Serial port operations
		final CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		final CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);
		serialPort = (SerialPort) commPort;
		serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
		serialPort.setDTR(false); // lovely undocumented feature where if true the motor stops spinning

		in = serialPort.getInputStream();
		out = serialPort.getOutputStream();

		readThread = new ReadSerialThread();
		new Thread(readThread).start();
	}

	/**
	 * Pauses for the specified number of milliseconds
	 */
	//TODO Refactor using Delay
	public void pause(long milli) {
		synchronized (this) {
			try {
				wait(milli);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * Shuts down the serial connection and threads
	 */
	public void shutdown() {

		serialPort.close();

		//TODO Check by the status of the Thread
		if (readThread != null) {
			readThread.requestStop();
			readThread = null;
		}
	}

	/**
	 * Request that it enter scan mode
	 *
	 * @param timeout Blocking time.  Resends packet periodically.  <= 0 means no blocking.
	 * @return true if successful
	 */
	public boolean sendScan(final long timeout) {
		return sendBlocking(SCAN, RCV_SCAN, timeout);
	}

	/**
	 * Sends a STOP packet
	 */
	public void sendStop() {
		scanning = false;
		sendNoPayLoad(STOP);
	}

	/**
	 * Sends a reset packet which will put it into its initial state
	 */
	public void sendReset() {
		scanning = false;
		sendNoPayLoad(RESET);
	}

	/**
	 * Requests that a sensor info packet be sent
	 *
	 * @param timeout Blocking time.  Resends packet periodically.  <= 0 means no blocking.
	 * @return true if successful
	 */
	public boolean sendGetInfo(final long timeout) {
		return sendBlocking(GET_INFO, RCV_INFO, timeout);
	}

	/**
	 * Requests that a sensor health packet be sent
	 *
	 * @param timeout Blocking time.  Resends packet periodically.  <= 0 means no blocking.
	 * @return true if successful
	 */
	public boolean sendGetHealth(final long timeout) {
		return sendBlocking(GET_HEALTH, RCV_HEALTH, timeout);
	}

	/**
	 * Low level blocking packet send routine
	 */
	protected boolean sendBlocking(byte command, byte expected, long timeout) {
		if (timeout <= 0) {
			sendNoPayLoad(command);
			return true;
		} else {
			lastReceived = 0;
			long endTime = System.currentTimeMillis() + timeout;
			do {
				sendNoPayLoad(command);
				pause(20);
			} while (endTime >= System.currentTimeMillis() && lastReceived != expected);

			//System.out.printf("Last received command %02x \n", lastReceived & 0xFF);
			//System.out.printf("Expected command %02x \n", expected & 0xFF);

			return lastReceived == expected;
		}
	}

	/**
	 * Sends a command with no data payload
	 */
	protected void sendNoPayLoad(byte command) {
		if (verbose) {
			log.debug("Sending command 0x%02x\n", command & 0xFF);
		}

		dataOut[0] = SYNC_BYTE0;
		dataOut[1] = command;

		try {
			out.write(dataOut, 0, 2);
			out.flush();
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
			//TODO Think in a Low Level exception
			e.printStackTrace();
		}
	}

	/**
	 * Sends a command with data payload
	 */
	protected void sendPayLoad(byte command, byte[] payLoad) {
		if (verbose) {
			System.out.printf("Sending command 0x%02x\n", command & 0xFF);
		}

		dataOut[0] = SYNC_BYTE0;
		dataOut[1] = command;

		//add payLoad and calculate checksum
		dataOut[2] = (byte) payLoad.length;
		int checksum = 0 ^ dataOut[0] ^ dataOut[1] ^ (dataOut[2] & 0xFF);

		for(int i=0; i<payLoad.length; i++){
			dataOut[3 + i] = payLoad[i];
			checksum ^= dataOut[3 + i];
		}

		//add checksum - now total length is 3 + payLoad.length + 1
		dataOut[3 + payLoad.length] = (byte) checksum;

		try {
			out.write(dataOut, 0, 3 + payLoad.length + 1);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a command with data payload - int
	 */
	protected void sendPayLoad(byte command, int payLoadInt) {
		byte[] payLoad = new byte[2];

		//load payload little Endian
		payLoad[0] = (byte) payLoadInt;
		payLoad[1] = (byte) (payLoadInt >> 8);

		sendPayLoad(command, payLoad);
	}

	/**
	 * Sends a start motor command
	 */
	public void sendStartMotor(int speed) {
		sendPayLoad(START_MOTOR, speed);
	}

	/**
	 * Sends a stop motor command
	 */
	public void sendStopMotor() {
		sendPayLoad(START_MOTOR, 0);
	}

	/**
	 * Searches for and parses all complete packets inside data
	 */
	protected int parseData(byte[] data, int length) {

		int offset = 0;

		if (verbose) {
			StringBuilder sb = new StringBuilder("parseData length = ").append(length);
			for (int i = 0; i < length; i++) {
				sb.append("%02x ").append(data[i] & 0xFF);
			}
			log.debug(sb.toString());
		}

		// search for the first good packet it can find
		while (true) {
			if (scanning) {
				if (offset + 5 > length) {
					return length;
				}

				if (parseScan(data, offset, 5)) {
					offset += 5;
				} else {
					if( verbose )
						log.debug("--- Bad Packet ---");
					offset += 1;
				}
			} else {
				// see if it has consumed all the data
				if (offset + 1 + 4 + 1 > length) {
					return length;
				}

				byte start0 = data[offset];
				byte start1 = data[offset + 1];

				if (start0 == SYNC_BYTE0 && start1 == SYNC_BYTE1) {
					int info = ((data[offset + 2] & 0xFF)) | ((data[offset + 3] & 0xFF) << 8) | ((data[offset + 4] & 0xFF) << 16) | ((data[offset + 5] & 0xFF) << 24);

					int packetLength = info & 0x3FFFFFFF;
					int sendMode = (info >> 30) & 0xFF;
					byte dataType = data[offset + 6];

					if (verbose) {
						log.debug("packet 0x%02x length = %d\n", dataType, packetLength);
					}
					// see if it has received the entire packet
					if (offset + 2 + 5 + packetLength > length) {
						if (verbose) {
							log.debug("  waiting for rest of the packet");
						}
						return offset;
					}

					if (parsePacket(data, offset + 2 + 4 + 1, packetLength, dataType)) {
						lastReceived = dataType & 0xFF;
						offset += 2 + 5 + packetLength;
					} else {
						offset += 2;
					}
				} else {
					offset++;
				}
			}
		}
	}

	protected boolean parsePacket(byte[] data, int offset, int length, byte type) {
		switch (type) {
			case (byte) RCV_INFO: // INFO
				return parseDeviceInfo(data, offset, length);

			case (byte) RCV_HEALTH: // health
				return parseHealth(data, offset, length);

			case (byte) RCV_SCAN: // scan and force-scan
				//if (parseScan(data, offset, length)) {
					scanning = true;
					return true;
				//}
				//break;
			default:
				log.debug("Unknown packet type = 0x%02x\n", type);
		}
		return false;
	}

	protected boolean parseHealth(byte[] data, int offset, int length) {
		if (length != 3) {
			log.debug("  bad health packet");
			return false;
		}

		health.status = data[offset] & 0xFF;
		health.error_code = (data[offset + 1] & 0xFF) | ((data[offset + 2] & 0xFF) << 8);

		listener.handleDeviceHealth(health);
		return true;
	}

	protected boolean parseDeviceInfo(byte[] data, int offset, int length) {
		if (length != 20) {
			log.debug("  bad device info");
			return false;
		}

		deviceInfo.model = data[offset] & 0xFF;
		deviceInfo.firmware_minor = data[offset + 1] & 0xFF;
		deviceInfo.firmware_major = data[offset + 2] & 0xFF;
		deviceInfo.hardware = data[offset + 3] & 0xFF;

		for (int i = 0; i < 16; i++) {
			deviceInfo.serialNumber[i] = data[offset + 4 + i];
		}

		listener.handleDeviceInfo(deviceInfo);
		return true;
	}

	protected boolean parseScan(byte[] data, int offset, int length) {

		if (length != 5) {
			log.debug("length != 5");
			return false;
		}

		byte b0 = data[offset];
		byte b1 = data[offset + 1];

		boolean start0 = (b0 & 0x01) != 0;
		boolean start1 = (b0 & 0x02) != 0;

		if (start0 == start1) {
			return false;
		}

		if ((b1 & 0x01) != 1) {
			return false;
		}

		measurement.timeMilli = System.currentTimeMillis();
		measurement.start = start0;
		measurement.quality = (b0 & 0xFF) >> 2;
		measurement.angle = ((b1 & 0xFF)  | ((data[offset + 2] & 0xFF) << 8)) >> 1;
		measurement.distance = ((data[offset + 3] & 0xFF) | ((data[offset + 4] & 0xFF) << 8));

		listener.handleMeasurement(measurement);
		return true;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Thread which reads in coming serial data from the LIDAR
	 */
	public class ReadSerialThread implements Runnable {

		byte data[] = new byte[1024 * 2];
		int size = 0;

		private AtomicBoolean run;

		public ReadSerialThread(){
			run = new AtomicBoolean(true);
		}

		public synchronized void requestStop() {
			run = new AtomicBoolean(false);
		}

		@Override
		public void run() {

			while (run.get()) {

				try {

					if (in.available() > 0) {
						int totalRead = in.read(data, size, data.length - size);

						size += totalRead;

						int used = parseData(data, size);

						// shift the buffer over by the amount read
						for (int i = 0; i < size - used; i++) {
							data[i] = data[i + used];
						}
						size -= used;
					}

					Thread.sleep(5);
				} catch (IOException e) {
					log.error(e.getLocalizedMessage());
					e.printStackTrace();
				} catch (InterruptedException e) {
					log.error(e.getLocalizedMessage());
					e.printStackTrace();
				//TODO Improve this Exception scenario
				} catch (Exception e){
					log.error(e.getLocalizedMessage());
					e.printStackTrace();
				}
			}
			log.info("RPLidar thread is terminating...");
		}
	}
}
