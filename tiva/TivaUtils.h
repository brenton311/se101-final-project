#ifndef UTILS_H
#define UTILS_H

// All required to use the LaunchPad and OLED
#include <OrbitOledGrph.h>
#include <OrbitBoosterPackDefs.h>
#include <OrbitOled.h>
#include <FillPat.h>
#include <LaunchPad.h>
#include <OrbitOledChar.h>

#include "TextUtils.h"

// When defined printDebugMsg will output given debug messages
// to the serial monitor. If not defined, it will simply return.
#define DEBUG

// The maximum number of constants that can fix on the
// X and Y axes of the OLED. Found through experimentation.
const int maxCharsX = 16;
const int maxCharsY = 4;
extern int y;

// Defined in tiva.ino. Complier can' find values When
// vars are initialized in this file
extern const uint32_t likeLED;
extern const uint32_t bookmarkLED;
extern const uint32_t Potentiometer;

extern int numMsgs;
extern int msgReceiveIndex;
extern int msgReadIndex;
extern Comment* comments;
extern int numLinesRequiredForCurrentMsg;

// Tiva util functions
template<typename T>
void printDebugMsg(T str, bool noNewline = false)
{
#ifdef DEBUG
    if(noNewline)
        Serial.print(str);
    else
        Serial.println(str);
#endif
}

int getPotPosition(int pin);

void updateDisplay();


#endif