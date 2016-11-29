#include "TivaUtils.h"
#include <aJSON.h>

/**
 * @brief Read a pot's position between 0 and 100
 * @param pin The pin the potentiometer is attached to
 * 
 * @return The pot's position mapped as an integer from 0 to 100. 
            Where 0 is fully left, and 100 is fully right.
 */
int getPotPosition(int pin)
{
    int value = analogRead(pin);
    return map(value, 0, 4096, 0, 100);
}

/**
 * @brief Update the bookmark/like status LEDs and the text on the OLED display
 * 
 * @return No return
 */
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