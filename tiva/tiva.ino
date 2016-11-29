#include <aJSON.h>

#include <OrbitOledGrph.h>
#include <OrbitBoosterPackDefs.h>
#include <OrbitOled.h>
#include <FillPat.h>
#include <LaunchPad.h>
#include <OrbitOledChar.h>
#include <delay.h>

// Comment out to disable debug messages on 
// the serial monitor
#define DEBUG

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

// Universal way to store comment (message) data
struct Comment
{
    String messageID;
    String message;
    String author;
    boolean iLiked;
    boolean iBookmarked;
};

// The maximum number of constants that can fix on the
// X and Y axes. Found through experimentation
const int maxCharsX = 16;
const int maxCharsY = 4;

int oldPotPosition = 0;

int numMsgs = 0;
int msgReceiveIndex = 0;
int msgReadIndex = 0;
Comment* comments = new Comment[30];
int numLinesRequiredForCurrentMsg = 0;

int y = 0;


void writeTextWithoutSplittingWords(String text);

/*Storing the original string and the JSON object is a bit too
much for your Arduino - it will most likely use up all the memory.
Therefore it is better to parse streams instead of strings*/
aJsonStream json_stream(&Serial1);

// Scale pot position between 0-100
int getPotPosition(int pin)
{
    int value = analogRead(pin);
    return map(value, 0, 4096, 0, 100);
}


template<typename T>
void printDebugMsg(T str, bool noNewline=false)
{
#ifdef DEBUG
    if(noNewline)
        Serial.print(str);
    else
        Serial.println(str);
#endif
}

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

Comment processJSON(aJsonObject* commentJSON)
{
    aJsonObject* authorObject = aJson.getObjectItem(commentJSON, "author_id");
    aJsonObject* msgIdObject = aJson.getObjectItem(commentJSON, "msg_id");
    aJsonObject* textObject = aJson.getObjectItem(commentJSON, "text");
    aJsonObject* likedObject = aJson.getObjectItem(commentJSON, "i_liked");
    aJsonObject* bookmarkedObject = aJson.getObjectItem(commentJSON, "i_bookmarked");

    String author = "";
    if (!authorObject)
    {
        printDebugMsg("Missing data in JSON");
    }
    else if (authorObject->type != aJson_String)
    {
        printDebugMsg("Invalid data type in JSON");
    }
    else
    {
        author = authorObject->valuestring;
    }

    String id = "";
    if (!msgIdObject)
    {
        printDebugMsg("Missing data in JSON");
    }
    else if (msgIdObject->type != aJson_String)
    {
        printDebugMsg("Invalid data type in JSON");
    }
    else
    {
        id = msgIdObject->valuestring;
    }

    String text = "";
    if (!textObject)
    {
        printDebugMsg("Missing data in JSON");
    }
    else if (textObject->type != aJson_String)
    {
        printDebugMsg("Invalid data type in JSON");
    }
    else
    {
        text = textObject->valuestring;
    }

    bool liked = false;
    if (!likedObject)
    {
        printDebugMsg("Missing data in JSON");
    }
    else if (likedObject->type != aJson_True && likedObject->type != aJson_False)
    {
        printDebugMsg("Invalid data type in JSON");
    }
    else
    {
        // liked = ((likedObject->valuestring).equals("true"))?true:false;
        liked = likedObject->valuebool;
    }

    bool bookmarked = false;
    if (!bookmarkedObject)
    {
        printDebugMsg("Missing data in JSON");
    }
    else if (bookmarkedObject->type != aJson_True && bookmarkedObject->type != aJson_False)
    {
        printDebugMsg("Invalid data type in JSON");
    }
    else
    {
        // bookmarked = ((bookmarkedObject->valuestring).equals("true"))?true:false;
        bookmarked = bookmarkedObject->valuestring;
    }

    struct Comment cmt = {.messageID = id,
        .message = text,
        .author = author,
        .iLiked = liked,
        .iBookmarked = bookmarked };

    return cmt;
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

void writeTextWithoutSplittingWords(String text)
{
    text.trim();
    int charIndex = 0;
    int lineNumber = 0;
    String formattedOutput = "";
    String line = "";
    int spacesLeftOnLine = maxCharsX;

    printDebugMsg(charIndex);
    while (charIndex < text.length())
    {
        printDebugMsg(charIndex);
        int newCharIndex = text.indexOf(" ", charIndex);
        if (newCharIndex == -1)
        {
            newCharIndex = text.length();
        }

        String word = text.substring(charIndex, newCharIndex);
        printDebugMsg(word);

        int wordLength = newCharIndex - charIndex;
        if (spacesLeftOnLine == maxCharsX)
        {
            printDebugMsg("1");
            // if first word on line; just need to for word
            if (spacesLeftOnLine >= wordLength)
            {
                printDebugMsg("11");
                line = word;
                charIndex = newCharIndex + 1;
                spacesLeftOnLine -= wordLength;
            }
            else
            {
                printDebugMsg("12");
                line = word.substring(0, spacesLeftOnLine);
                printDebugMsg(line);
                if (y + lineNumber >= 0)
                {
                    if (y + lineNumber < maxCharsY)
                    {
                        // OrbitOledSetCursor(0, y + lineNumber);
                        // OrbitOledPutString((char*) line.c_str());
                        while (spacesLeftOnLine > 0)
                        {
                            line += " ";
                            spacesLeftOnLine--;
                        }
                        formattedOutput += line;
                    }
                    else
                    {
                        break;
                    }
                }
                lineNumber++;
                line = "";
                spacesLeftOnLine = maxCharsX;
                charIndex = charIndex + spacesLeftOnLine;
            }
        }
        else
        {
            printDebugMsg("2");

            // need to fit space + word
            if (spacesLeftOnLine >= 1 + wordLength)
            {
                printDebugMsg("21");

                line += " " + word;
                charIndex = newCharIndex + 1;
                spacesLeftOnLine -= 1 + wordLength;
            }
            else
            {
                printDebugMsg("22");
                printDebugMsg(line);

                if (y + lineNumber >= 0)
                {
                    if (y + lineNumber < maxCharsY)
                    {
                        // OrbitOledSetCursor(0, y + lineNumber);
                        // OrbitOledPutString((char*) line.c_str());
                        while (spacesLeftOnLine > 0)
                        {
                            line += " ";
                            spacesLeftOnLine--;
                        }
                        formattedOutput += line;
                    }
                    else
                    {
                        break;
                    }
                }
                lineNumber++;
                line = "";
                spacesLeftOnLine = maxCharsX;
            }
        }
    }
    if (y + lineNumber >= 0 && y + lineNumber < maxCharsY)
    {
        // OrbitOledSetCursor(0, y + lineNumber);
        // OrbitOledPutString((char*) line.c_str());
        while (spacesLeftOnLine > 0)
        {
            line += " ";
            spacesLeftOnLine--;
        }
        formattedOutput += line;
    }

    OrbitOledClear();
    OrbitOledSetCursor(0, 0);
    OrbitOledPutString((char*)formattedOutput.c_str());
}

int numberOfLEDlineRequired(String text)
{
    text.trim();
    int charIndex = 0;
    int lineNumber = 0;
    int spacesLeftOnLine = maxCharsX;

    while (charIndex < text.length())
    {
        int newCharIndex = text.indexOf(" ", charIndex);
        if (newCharIndex == -1)
        {
            newCharIndex = text.length();
        }
        int wordLength = newCharIndex - charIndex;
        if (spacesLeftOnLine == maxCharsX)
        {
            if (spacesLeftOnLine >= wordLength)
            {
                charIndex = newCharIndex + 1;
                spacesLeftOnLine -= wordLength;
            }
            else
            {
                lineNumber++;
                spacesLeftOnLine = maxCharsX;
                charIndex = charIndex + spacesLeftOnLine;
            }
        }
        else
        {
            if (spacesLeftOnLine >= 1 + wordLength)
            {
                charIndex = newCharIndex + 1;
                spacesLeftOnLine -= 1 + wordLength;
            }
            else
            {
                lineNumber++;
                spacesLeftOnLine = maxCharsX;
            }
        }
    }

    if (spacesLeftOnLine != maxCharsX)
    {
        lineNumber++;
    }
    
    return lineNumber;
}