/*macro definitions of PIR motion sensor pin and LED pin*/
#define PIR_MOTION_SENSOR 2//Use pin 2 to receive the signal from the module
//#define LED 4//the Grove - LED is connected to D4 of Arduino


void setup()
{
    pinMode(PIR_MOTION_SENSOR, INPUT);
    Serial.begin(115200);
}

void loop()
{
    if(isPeopleDetected())//if it detects the moving people?
        Serial.println("Detected");
    else
        Serial.println("Hello world");
}


/***************************************************************/
/*Function: Detect whether anyone moves in it's detecting range*/
/*Return:-boolean, true is someone detected.*/
boolean isPeopleDetected()
{
    int sensorValue = digitalRead(PIR_MOTION_SENSOR);
    if(sensorValue == HIGH)//if the sensor value is HIGH?
    {
        return true;//yes,return true
    }
    else
    {
        return false;//no,return false
    }
}
