#include <aJSON.h>

#include <delay.h>

#include "TivaUtils.h"
#include "TextUtils.h"

const uint32_t SwitchCount = 2;
const uint32_t ButtonCount = 2;
const uint32_t Switches[SwitchCount] = { PA_6, PA_7 };
int SwitchStates[2] = { 0, 0 };
const uint32_t Buttons[ButtonCount] = { PE_0, PD_2 };
bool ButtonStates[2] = { false, false };

const uint32_t likeLED = PC_7;
const uint32_t bookmarkLED = PC_6;
const uint32_t Potentiometer = PE_3;

extern int dxcoOledFontCur;
extern int dycoOledFontCur;

int y = 0;

int oldPotPosition = 0;

int numMsgs = 0;
int msgReceiveIndex = 0;
int msgReadIndex = 0;
Comment* comments = new Comment[30];
int numLinesRequiredForCurrentMsg = 0;
/*Storing the original string and the JSON object is a bit too
much for your Arduino - it will most likely use up all the memory.
Therefore it is better to parse streams instead of strings*/
aJsonStream json_stream(&Serial1);


void setup()
{
    // Serial1 = Bluetooth module
    // Serial = USB port on Tiva
    Serial1.begin(9600);
    Serial.begin(19200);

    OrbitOledInit();

    pinMode(likeLED, OUTPUT);
    pinMode(bookmarkLED, OUTPUT);

    for (int i = 0; i < SwitchCount; ++i)
    {
        pinMode(Switches[i], INPUT);
    }

    for (int i = 0; i < ButtonCount; ++i)
    {
        pinMode(Buttons[i], INPUT);
    }

    for (int i = 0; i < SwitchCount; ++i)
    {
        SwitchStates[i] = digitalRead(Switches[i]);
    }

    delay(1000);
    writeTextWithoutSplittingWords("Welcome to Pronto! Set the app to \"Tiva Mode\"");
}

void loop()
{
    if (Serial1.available())
    {
        if (Serial1.peek() == 'C')
        {
            String msg = Serial1.readStringUntil('\n');
            if (msg.substring(4).equals("Finished"))
            {
                digitalWrite(bookmarkLED, LOW);
                digitalWrite(likeLED, LOW);
                numMsgs = 0;
                msgReceiveIndex = 0;
                msgReadIndex = 0;
                writeTextWithoutSplittingWords("Welcome to Pronto! Set the app to \"Tiva Mode\"");
            }
        }
        else
        {
            aJsonObject* commentJSONArray = aJson.parse(&json_stream);
            if (!commentJSONArray)
            {
                printDebugMsg("Missing data in JSON");
                return;
            }
            aJsonObject* commentJSON = commentJSONArray->child;
            while (commentJSON)
            {
                comments[msgReceiveIndex++] = processJSON(commentJSON);
                numMsgs++;
                commentJSON = commentJSON->next;
            }

            // Deleting the root takes care of everything else (deletes the objects
            // and all values referenced by it)
            aJson.deleteItem(commentJSONArray);

            printDebugMsg("msgs ", true);
            printDebugMsg(numMsgs);

            if (numMsgs > 0)
            {
                numLinesRequiredForCurrentMsg = numberOfLEDlineRequired(
                    comments[msgReadIndex].author + ": " + comments[msgReadIndex].message);
                updateDisplay();
            }
        }
    }

    if (numMsgs > 0)
    {
        int state = digitalRead(Switches[0]);
        if (state != SwitchStates[0])
        {
            SwitchStates[0] = state;
            msgReadIndex--;
            if (msgReadIndex < 0)
            {
                msgReadIndex = 0;
            }

            numLinesRequiredForCurrentMsg = numberOfLEDlineRequired(
                comments[msgReadIndex].author + ": " + comments[msgReadIndex].message);
            y = 0;

            // print message from start initially,
            // regradless of pot position
            updateDisplay();
        }

        state = digitalRead(Switches[1]);
    
        printDebugMsg("vals ", true);
        printDebugMsg(state);
        printDebugMsg(SwitchStates[1]);

        if (state != SwitchStates[1])
        {
            SwitchStates[1] = state;
            msgReadIndex++;
            if (msgReadIndex >= numMsgs)
            {
                msgReadIndex = numMsgs - 1;
            }
            numLinesRequiredForCurrentMsg = numberOfLEDlineRequired(
                comments[msgReadIndex].author + ": " + comments[msgReadIndex].message);
            y = 0;
            // print message from start initially,
            // regradless of pot position
            updateDisplay();
        }

        if (digitalRead(Buttons[0]) == HIGH && !ButtonStates[0])
        {
            ButtonStates[0] = true;
            comments[msgReadIndex].iLiked = !comments[msgReadIndex].iLiked;
            updateDisplay();

            printDebugMsg("LIKE:", true);
            printDebugMsg(comments[msgReadIndex].messageID);
        }
        if (digitalRead(Buttons[0]) == LOW && ButtonStates[0])
        {
            ButtonStates[0] = false;
        }

        if (digitalRead(Buttons[1]) == HIGH && !ButtonStates[1])
        {
            ButtonStates[1] = true;
            comments[msgReadIndex].iBookmarked = !comments[msgReadIndex].iBookmarked;

            printDebugMsg("BKMK:", true);
            printDebugMsg(comments[msgReadIndex].messageID);
            updateDisplay();
        }
        if (digitalRead(Buttons[1]) == LOW && ButtonStates[1])
        {
            ButtonStates[1] = false;
        }

        // Display all the messages to the screen in the correct order
        // Only displayed when pot position changes to prevent screen flicker
        // Get the desired cursor y location
        int newPotPosition = getPotPosition(Potentiometer);
        if (newPotPosition != oldPotPosition)
        {
            if (numLinesRequiredForCurrentMsg <= maxCharsY)
            {
                y = 0;
            }
            else
            {
                y = map(
                    newPotPosition, 0, 100, 0, -(1 + numLinesRequiredForCurrentMsg - maxCharsY));
            }

            printDebugMsg(y);
            oldPotPosition = newPotPosition;
            updateDisplay();
        }
    }

    delay(100);
}