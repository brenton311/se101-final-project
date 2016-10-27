#include <OrbitOledGrph.h>
#include <OrbitBoosterPackDefs.h>
#include <OrbitOled.h>
#include <FillPat.h>
#include <LaunchPad.h>
#include <OrbitOledChar.h>
#include <delay.h>

/*
  Blink
  The basic Energia example.
  Turns on an LED on for one second, then off for one second, repeatedly.
  Change the LED define to blink other LEDs.
  
  Hardware Required:
  * LaunchPad with an LED
  
  This example code is in the public domain.
*/

// most launchpads have a red LED
#define LED RED_LED

//see pins_energia.h for more LED definitions
// #define LED GREEN_LED
  
// the setup routine runs once when you press reset:
void setup() 
{                
//  void  OrbitOledInit();
//void  OrbitOledClear();
//void  OrbitOledClearBuffer();
//void  OrbitOledUpdate();
    OrbitOledInit();
}

// the loop routine runs over and over again forever:
void loop() 
{
    char line[6] = {0};
    OrbitOledSetCursor(0, 0);
    OrbitOledPutString(" Pronto v0.0.1!");

    OrbitOledSetCursor(0, 2);
    OrbitOledLineTo(127, 16);

    OrbitOledSetCursor(0, 4);
    OrbitOledPutString("A SE'XXI Project'");
}
