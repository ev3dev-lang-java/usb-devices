#include "NAxisMotion.h"        
#include <Wire.h>

NAxisMotion mySensor;          
unsigned long lastStreamTime = 0;     
const int streamPeriod = 20;          //To stream at 50Hz without using additional timers (time period(ms) =1000/frequency(Hz))
bool updateSensorData = true;         //Flag to update the sensor data. Default is true to perform the first read before the first stream

void setup() //This code is executed once
{    
  //Peripheral Initialization
  Serial.begin(115200);           //Initialize the Serial Port to view information on the Serial Monitor
  I2C.begin();                    //Initialize I2C communication to the let the library communicate with the sensor.

  //Sensor Initialization
  mySensor.initSensor();          //The I2C Address can be changed here inside this function in the library
  mySensor.setOperationMode(OPERATION_MODE_NDOF);   //Can be configured to other operation modes as desired
  mySensor.setUpdateMode(MANUAL);  //The default is AUTO. Changing to MANUAL requires calling the relevant update functions prior to calling the read functions
  //Setting to MANUAL requires fewer reads to the sensor 
}

void loop()
{
  if (updateSensorData)  //Keep the updating of data as a separate task
  {
    mySensor.updateEuler();
    mySensor.updateLinearAccel();
    mySensor.updateMag();
    mySensor.updateGyro();
    mySensor.updateCalibStatus();  //Update the Calibration Status
    updateSensorData = false;
  }
  
  if ((millis() - lastStreamTime) >= streamPeriod)
  {
    lastStreamTime = millis();    

    //Euler
    
    //Serial.print(" H: ");
    Serial.print(mySensor.readEulerHeading());
    Serial.print(", ");

    //Serial.print(" R: ");
    Serial.print(mySensor.readEulerRoll()); 
    Serial.print(",");

    //Serial.print(" P: ");
    Serial.print(mySensor.readEulerPitch());
    Serial.print(", ");

    //Acceleration
    
    //Serial.print(" AccelX: ");
    Serial.print(mySensor.readLinearAccelX());
    Serial.print(", ");
    
    //Serial.print(" AccelY: ");
    Serial.print(mySensor.readLinearAccelY());
    Serial.print(", ");
    
    //Serial.print(" AccelZ: ");
    Serial.print(mySensor.readLinearAccelZ());
    Serial.print(", ");

    //Magnetometer
    
    //Serial.print(" MagX: ");
    Serial.print(mySensor.readMagX());
    Serial.print(", ");
    
    //Serial.print(" MagY: ");
    Serial.print(mySensor.readMagY());
    Serial.print(", ");
    
    //Serial.print(" MagZ: ");
    Serial.print(mySensor.readMagZ());
    Serial.print(", ");

    //Gyroscope
    
    //Serial.print(" GyroX: ");
    Serial.print(mySensor.readGyroX());
    Serial.print(", ");

    //Serial.print(" GyroY: ");
    Serial.print(mySensor.readGyroY());
    Serial.print(", ");
    
    //Serial.print(" GyroZ: ");
    Serial.print(mySensor.readGyroZ());
    Serial.print(", ");
    
    Serial.println();

    updateSensorData = true;
  }
}
