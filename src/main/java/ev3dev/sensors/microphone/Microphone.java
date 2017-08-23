package ev3dev.sensors.microphone;

public class Microphone {

    private SoundProcessor soundProcessor;

    public void init() {
        soundProcessor = new SoundProcessor();
        soundProcessor.init();
        soundProcessor.setDaemon(true);
        soundProcessor.start();
    }

    public float readValue(){
        return soundProcessor.getValue();
    }
}
