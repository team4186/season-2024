#include <Arduino.h>
#include <Wire.h>        // Instantiate the Wire library
#include <TFLI2C.h>      // TFLuna-I2C Library v.0.1.1
 
TFLI2C tflI2C;
 
int16_t  tfDist;    // distance in centimeters
int16_t  tfAddr = TFL_DEF_ADR;  // Use this default I2C address
int notBroken = 28;
 
void setup(){
    Serial.begin(115200);  // Initalize serial port
    Wire.begin();           // Initalize Wire library
    pinMode(LED_BUILTIN, OUTPUT); //initialize the LED to report beam status1q
    
    pinMode(13, OUTPUT);  //set out the output pin for DIO
}
 
void loop(){
  
    if(tflI2C.getData(tfDist, tfAddr)){
        Serial.println(String(tfDist)+" cm / " + String(tfDist/2.54)+" inches");
        digitalWrite(LED_BUILTIN, LOW);
        if(tfDist < notBroken){
          Serial.println("Beam is broken");
          digitalWrite(13, HIGH); // sets the digital pin 13 high
          //do we need to initialize the pin value?
          digitalWrite(LED_BUILTIN, HIGH);
          Serial.println("Beam is broken");
        }
    }
    delay(50);
}