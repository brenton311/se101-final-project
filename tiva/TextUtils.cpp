#include "TextUtils.h"
#include "TivaUtils.h"

/**
 * @brief Parse a Comment from a JSON object
 * @param commentJSON The JSON object representing the comment
 * 
 * @return The comment parsed from the JSON
 */
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


/**
 * @brief Write a string to the OLED display without splitting words
 * @param text The string to be displayed
 * 
 */
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


/**
 * @brief Calculates the number of OLED display vertical lines needed to 
        display a given string.
 * @param text The string to compute how many OLED lines it needs
 * 
 * @return The number of vertical lines needed
 */
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
