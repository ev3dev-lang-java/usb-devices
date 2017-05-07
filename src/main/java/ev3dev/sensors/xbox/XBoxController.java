package ev3dev.sensors.xbox;

import net.java.games.input.*;

public class XBoxController {

    public static void main(String[] args) {
        Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();

        for(int i =0;i<ca.length;i++) {

            /* Get the name of the controller */
            System.out.println(ca[i].getName());
        }
    }

    /*
    public static void main(String[] args) throws ReflectiveOperationException {

        List<Controller> gamepads = Arrays.stream(ControllerEnvironment.getDefaultEnvironment().getControllers()).filter(controller ->
                controller.getType().equals(Controller.Type.GAMEPAD)).collect(Collectors.toList());
        Controller gamepad = gamepads.get(0); // only working with one gamepad

        Event event;
        Component component;
        float value;
        String tempPosition = "";

        while (true) {
            gamepad.poll();

            EventQueue eq = gamepad.getEventQueue();
            event = new Event();

            while (eq.getNextEvent(event)) {
                component = event.getComponent();
                value = event.getValue();

                // clear temporarily stored position if analog stick is in neutral position
                if ((value < 0.3) && (value > -0.3) && (tempPosition.equals(component.getIdentifier().getName()))) {
                    tempPosition = "";
                }

                if (component.isAnalog()) {
                    // input from analog-sticks and back triggers
                    if ((value > 0.8) && !(tempPosition.equals(component.getIdentifier().getName()))) {
                        // positive direction
                        switch (component.getIdentifier().getName()) {
                            case "x":
                                // left stick - RIGHT
                                tempPosition = "x";

                                break;
                            case "y":
                                // left stick - DOWN
                                tempPosition = "y";

                                break;
                            case "rx":
                                // right stick - RIGHT
                                tempPosition = "rx";

                                break;
                            case "ry":
                                // right stick - DOWN
                                tempPosition = "ry";

                                break;
                            case "z":
                                // left trigger (z-axis)
                                tempPosition = "z";

                                break;
                        }
                    }

                    if (value < -0.8 && !(tempPosition.equals(component.getIdentifier().getName()))) {
                        // negative direction
                        switch (component.getIdentifier().getName()) {
                            case "x":
                                // left stick - LEFT
                                tempPosition = "x";

                                break;
                            case "y":
                                // left stick - UP
                                tempPosition = "y";

                                break;
                            case "rx":
                                // right stick - LEFT
                                tempPosition = "rx";

                                break;
                            case "ry":
                                // right stick - UP
                                tempPosition = "ry";

                                break;
                            case "z":
                                // right trigger (z-axis)
                                tempPosition = "z";

                                break;
                        }
                    }
                } else {
                    // input from buttons, dpad analog-stick-pushes
                    if (value == 1.0) {
                        switch (component.getIdentifier().getName()) {
                            case "0":
                                // A-Button

                                break;
                            case "1":
                                // B-Button

                                break;
                            case "2":
                                // X-Button

                                break;
                            case "3":
                                // Y-Button

                                break;
                            case "4":
                                // LB-Button

                                break;
                            case "5":
                                // RB-Button

                                break;
                            case "6":
                                // Back-Button

                                break;
                            case "7":
                                // Start-Button

                                break;
                            case "8":
                                // Left Stick Push

                                break;
                            case "9":
                                // Right Stick Push

                                break;
                            case "pov":
                                // DPad Left

                                break;
                            default:
                                break;
                        }
                    } else if (value == 0.25) {
                        // DPad Up

                    } else if (value == 0.5) {
                        // DPad Right

                    } else if (value == 0.75) {
                        // DPad Down

                    }
                    // if you want to use UpLeft, UpRight, DownLeft and DownRight on DPad, you have to ad cases for value = 0.125, 0.375, 0.625 and 0.875
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    */

}
