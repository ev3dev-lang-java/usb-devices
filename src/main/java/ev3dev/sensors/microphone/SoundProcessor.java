package ev3dev.sensors.microphone;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class SoundProcessor extends Thread {

    private final float SAMPLE_RATE = 44100f; //44.1kHz
    private final int SAMPLE_SIZE_IN_BITS = 16;
    private final int CHANNELS = 1;
    private final boolean SIGNED = true;
    private final boolean BIG_ENDIAN = false;
    private final AudioFormat audioFormat;

    private TargetDataLine line;
    private final int bufferByteSize = 2048;
    private final byte[] buf = new byte[bufferByteSize];
    private final float[] samples = new float[bufferByteSize / 2];

    private AtomicFloat value;

    public SoundProcessor(){
        audioFormat = new AudioFormat( SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
        value = new AtomicFloat(0);
    }

    public void init(){

        try {
            line = AudioSystem.getTargetDataLine(audioFormat);
            line.open(audioFormat, bufferByteSize);

            //TODO Review concept about Service Exception
        } catch (LineUnavailableException e) {
            System.err.println(e);
            return;
        }
    }

    @Override
    public void run() {

        line.start();

        for (int b; (b = line.read(buf, 0, buf.length)) > -1; ) {

            // convert bytes to samples here
            for (int i = 0, s = 0; i < b; ) {
                int sample = 0;

                sample |= buf[i++] & 0xFF; // (reverse these two lines
                sample |= buf[i++] << 8;   //  if the format is big endian)

                // normalize to range of +/-1.0f
                samples[s++] = sample / 32768f;
            }

            float rms = 0f;
            for (float sample : samples) {
                rms += sample * sample;
            }

            rms = (float) Math.sqrt(rms / samples.length);

            //System.out.println(rms);
            value.set(rms);
        }

        line.stop();
    }

    public float getValue() {
        return value.get();
    }
}
