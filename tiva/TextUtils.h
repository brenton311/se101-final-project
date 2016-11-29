#ifndef TEXT_UTILS_H
#define TEXT_UTILS_H

// JSON library used to make communication between the phone 
// and Tiva easier
#include <aJSON.h>

// Universal way to store comment (message) data
struct Comment
{
    String messageID;
    String message;
    String author;
    bool iLiked;
    bool iBookmarked;
};

void writeTextWithoutSplittingWords(String text);
Comment processJSON(aJsonObject* commentJSON);
int numberOfLEDlineRequired(String text);

#endif // TEXT_UTILS_H