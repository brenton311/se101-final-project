// char val; // variable to receive data from the serial port
#define LED GREEN_LED
// #include <
#include <OrbitOledGrph.h>
#include <OrbitBoosterPackDefs.h>
#include <OrbitOled.h>
#include <FillPat.h>
#include <LaunchPad.h>
#include <OrbitOledChar.h>
#include <delay.h>

extern int    dxcoOledFontCur;
extern int    dycoOledFontCur;

// Found through experimentation
const int maxCharsX = 17;
const int maxCharsY = 5;

// Scale pot position between 0-100
int getPotPosition(int pin)
{
    int value = analogRead(pin);
    return map(value, 0, 4096, 0, 100);
}

void setup() 
{
    pinMode(LED, OUTPUT);  // pin 48 (on-board LED) as OUTPUT
    Serial1.begin(9600); 
    Serial.begin(19200);       // start serial communication at 9600bps
    OrbitOledInit();

    Serial.println(dxcoOledFontCur);
    Serial.println(dycoOledFontCur);
}


String msg;
int oldPotPosition = 0;
int msgIndex = 0;

const int numMsgs = 10;
String msgs[numMsgs] = {"Hello World!!", "PRONTO", "Brenton S.", "Test", "1", "2Hello World!!", "2PRONTO", "2Brenton S.", "2Test", "21"};

int y = 0;

void loop() 
{
    if(Serial1.available())
    {
         msg = Serial1.readString();
         Serial.println(msg);
    
         OrbitOledClear();
         OrbitOledSetCursor(0, 0);
         OrbitOledPutString((char*) msg.c_str());
    }

    // Get the desired cursor y location
    int newPotPosition = getPotPosition(A0);
    if(newPotPosition != oldPotPosition)
    {
        // The min pos is all messages just off the screen (top)
        // The max pos is all message just off the screen (bottom)
        y = map(newPotPosition, 0, 100, -numMsgs - 1, maxCharsY);
        Serial.println(y);

        oldPotPosition = newPotPosition;

        // Display all the messages to the screen in the correct order
        // Only displayed when pot position changes to prevent screen flicker
        OrbitOledClear();
        for(int i = 0; i < numMsgs; i++)
        {
            int yPos = y + i;

            // Don't draw messages if they are not on the screen
            if(yPos < 0 || yPos > maxCharsY - 2)
                continue;

            OrbitOledSetCursor(0, yPos);
            OrbitOledPutString((char*) msgs[i].c_str());
        }
    }

    delay(100);
}
