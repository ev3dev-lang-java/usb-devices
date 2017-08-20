package ev3dev.sensors.microphone;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Microphone {

    private final ScheduledExecutorService executor;
    private ScheduledFuture scheduledFuture;
    private AtomicFloat value;
    private AtomicBoolean closingFlag;

    private static final float SAMPLE_RATE = 44100f; //44.1kHz
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;

    public Microphone(){
        executor = Executors.newScheduledThreadPool(1);
        value = new AtomicFloat(0);
        closingFlag = new AtomicBoolean(false);
    }

    public void init() {

        final AudioFormat fmt = new AudioFormat( SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);

        final int bufferByteSize = 2048;
        final TargetDataLine line;

        try {
            line = AudioSystem.getTargetDataLine(fmt);
            line.open(fmt, bufferByteSize);
        } catch (LineUnavailableException e) {
            System.err.println(e);
            return;
        }

        byte[] buf = new byte[bufferByteSize];
        float[] samples = new float[bufferByteSize / 2];

        //TODO Java could evolve the method with a version with a Callable
        scheduledFuture = executor.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {

                line.start();
                for (int b; (b = line.read(buf, 0, buf.length)) > -1; ) {

                    if(closingFlag.get()){
                        break;
                    }

                    // convert bytes to samples here
                    for (int i = 0, s = 0; i < b; ) {
                        int sample = 0;

                        sample |= buf[i++] & 0xFF; // (reverse these two lines
                        sample |= buf[i++] << 8;   //  if the format is big endian)

                        // normalize to range of +/-1.0f
                        samples[s++] = sample / 32768f;
                    }

                    float rms = 0f;
                    float peak = 0f;
                    for (float sample : samples) {

                        float abs = Math.abs(sample);
                        if (abs > peak) {
                            peak = abs;
                        }

                        rms += sample * sample;
                    }

                    rms = (float) Math.sqrt(rms / samples.length);

                    System.out.println(rms);
                    value.set(rms);

                }

                line.stop();
            }

        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    public float readValue(){
        return value.get();
    }

    public void close(){
        System.out.println("Closing sensor");
        executor.shutdownNow();
        closingFlag.set(true);
    }

    public static void main(String[] args) throws Exception {

        Microphone microphone = new Microphone();
        microphone.init();

        for(int x= 0; x < 10; x++){
            System.out.println("Sensor value" + microphone.readValue());
            Thread.sleep(300);
        }

        microphone.close();
        System.out.println("End example");
    }

}
