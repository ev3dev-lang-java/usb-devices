import ev3dev.sensors.microphone.Microphone;

/**
 * Created by jabrena on 20/8/17.
 */
public class MicrophoneExample {

    public static void main(String[] args) throws Exception {

        Microphone microphone = new Microphone();
        microphone.init();

        for(int x= 0; x < 10; x++){
            System.out.println("Sensor value: " + microphone.readValue());
            Thread.sleep(300);
        }

        System.out.println("End example");
    }

}
