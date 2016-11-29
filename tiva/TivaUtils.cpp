#include "TivaUtils.h"
#include <aJSON.h>

// extern int y;

// extern int numMsgs = 0;
// extern int msgReceiveIndex = 0;
// extern int msgReadIndex = 0;
// extern Comment* comments = new Comment[30];
// extern int numLinesRequiredForCurrentMsg = 0;

// Scale pot position between 0-100
int getPotPosition(int pin)
{
    int value = analogRead(pin);
    return map(value, 0, 4096, 0, 100);
}

void updateDisplay()
{
    // map(value, fromLow, fromHigh, toLow, toHigh)
    // The min pos is all messages just off the screen (top)
    // The max pos is all message just off the screen (bottom)
    printDebugMsg(y, true);
    printDebugMsg(" ", true);
    printDebugMsg(msgReadIndex, true);
    printDebugMsg(" ", true);
    printDebugMsg(comments[msgReadIndex].message);

    if (comments[msgReadIndex].iLiked)
    {
        digitalWrite(likeLED, HIGH);
    }
    else
    {
        digitalWrite(likeLED, LOW);
    }

    if (comments[msgReadIndex].iBookmarked)
    {
        digitalWrite(bookmarkLED, HIGH);
    }
    else
    {
        digitalWrite(bookmarkLED, LOW);
    }

    writeTextWithoutSplittingWords(
        comments[msgReadIndex].author + ": " + comments[msgReadIndex].message);
}