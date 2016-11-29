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

/**
 * @brief Parse a Comment from a JSON object
 * @param commentJSON The JSON object representing the comment
 * 
 * @return The comment parsed from the JSON
 */
Comment processJSON(aJsonObject* commentJSON);

/**
 * @brief Write a string to the OLED display without splitting words
 * @param text The string to be displayed
 * 
 */
void writeTextWithoutSplittingWords(String text);

/**
 * @brief Calculates the number of OLED display vertical lines needed to 
        display a given string.
 * @param text The string to compute how many OLED lines it needs
 * 
 * @return The number of vertical lines needed
 */
int numberOfLEDlineRequired(String text);

#endif // TEXT_UTILS_H