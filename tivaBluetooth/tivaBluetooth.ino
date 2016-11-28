
#include <aJSON.h>

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

const uint32_t SwitchCount = 2;
const uint32_t ButtonCount = 2;
const uint32_t Switches[SwitchCount] = {PA_6, PA_7};
int SwitchStates[2] = {0 , 0};
const uint32_t Buttons[ButtonCount] = { PE_0, PD_2 };
bool ButtonStates[2] = {false, false};
const uint32_t likeLED = PC_7;
const uint32_t bookmarkLED = PC_6;
const uint32_t Potentiometer = PE_3;

extern int    dxcoOledFontCur;
extern int    dycoOledFontCur;


extern int    dxcoOledFontCur;
extern int    dycoOledFontCur;

struct comment {
  String messageID;
  String message;
  String author;
  boolean iLiked;
  boolean iBookmarked;
};

// Found through experimentation
const int maxCharsX = 17;
const int maxCharsY = 5;

String msg;
int oldPotPosition = 0;

int numMsgs = 0;
int msgReceiveIndex = 0;
int msgReadIndex = 0;
comment *comments = new comment[30];

int y = 0;



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

void setup()
{
  pinMode(LED, OUTPUT);  // pin 48 (on-board LED) as OUTPUT
  Serial1.begin(9600);
  Serial.begin(19200);       // start serial communication at 9600bps
  OrbitOledInit();

  Serial.println(dxcoOledFontCur);
  Serial.println(dycoOledFontCur);

  pinMode(likeLED, OUTPUT);
  pinMode(bookmarkLED, OUTPUT);

  for (int i = 0; i < SwitchCount; ++i ) {
    pinMode(Switches[i], INPUT);
  }

  for (int i = 0; i < ButtonCount; ++i ) {
    pinMode(Buttons[i], INPUT);
  }

  for (int i = 0; i < SwitchCount; ++i ) {
    SwitchStates[i] = digitalRead(Switches[i]);
  }

  OrbitOledClear();
  OrbitOledSetCursor(0, 0);
  OrbitOledPutString((char*)"Welcome to Pronto! Set the app to \"Tiva Mode\"");
}

comment processJSON(aJsonObject *commentJSON)
{
  aJsonObject *authorObject = aJson.getObjectItem(commentJSON, "author_id");
  aJsonObject *msgIdObject = aJson.getObjectItem(commentJSON, "msg_id");
  aJsonObject *textObject = aJson.getObjectItem(commentJSON, "text");
  aJsonObject *likedObject = aJson.getObjectItem(commentJSON, "i_liked");
  aJsonObject *bookmarkedObject = aJson.getObjectItem(commentJSON, "i_bookmarked");

  String author = "";
  if (!authorObject) {
    Serial.println("Missing data in JSON");
  } else if (authorObject->type != aJson_String) {
    Serial.println("Invalid data type in JSON");
  } else {
    author = authorObject->valuestring;
  }

  String id = "";
  if (!msgIdObject) {
    Serial.println("Missing data in JSON");
  } else if (msgIdObject->type != aJson_String) {
    Serial.println("Invalid data type in JSON");
  } else {
    id = msgIdObject->valuestring;
  }

  String text = "";
  if (!textObject) {
    Serial.println("Missing data in JSON");
  } else if (textObject->type != aJson_String) {
    Serial.println("Invalid data type in JSON");
  } else {
    text = textObject->valuestring;
  }

  bool liked = false;
  if (!likedObject) {
    Serial.println("Missing data in JSON");
  } else if (likedObject->type != aJson_True && likedObject->type != aJson_False) {
    Serial.println("Invalid data type in JSON");
  } else {
    //liked = ((likedObject->valuestring).equals("true"))?true:false;
    liked = likedObject->valuebool;
  }

  bool bookmarked = false;
  if (!bookmarkedObject) {
    Serial.println("Missing data in JSON");
  } else if (bookmarkedObject->type != aJson_True && bookmarkedObject->type != aJson_False) {
    Serial.println("Invalid data type in JSON");
  } else {
   //bookmarked = ((bookmarkedObject->valuestring).equals("true"))?true:false;
   bookmarked = bookmarkedObject->valuestring;
  }
  
  struct comment  cmt =  {
    .messageID =id,
    .message = text,
    .author = author,
    .iLiked = liked,
    .iBookmarked = bookmarked
  };
  return cmt;
}

void loop()
{

  if (Serial1.available())
  {
    //msg = Serial1.readStringUntil('\n');
    //Serial.println(msg);

    if (msg.substring(0, 4).equals("CMD:"))
    {
      if (msg.substring(4).equals("Finished"))
      {
        numMsgs = 0;
        msgReceiveIndex = 0;
        msgReadIndex = 0;
        OrbitOledClear();
        OrbitOledSetCursor(0, 0);
        OrbitOledPutString((char*)"Welcome to Pronto! Set the app to \"Tiva Mode\"");
      }
    }
    else
    {
      aJsonObject *commentJSONArray = aJson.parse(&json_stream);
      if (!commentJSONArray) {
        Serial.println("Missing data in JSON");
        return;
      }
      aJsonObject *commentJSON = commentJSONArray -> child;
      while (commentJSON) {
        comments[msgReceiveIndex++] = processJSON(commentJSON);
        numMsgs++;
        commentJSON = commentJSON -> next;
      }
      //Deleting the root takes care of everything else (deletes the objects and all values referenced by it)
      aJson.deleteItem(commentJSONArray);
      if (numMsgs > 0) {
          updateDisplay();
      }
    }
  }

  if (numMsgs > 0) {
    int state =  digitalRead(Switches[0]);
    if (state != SwitchStates[0]) {
      SwitchStates[0] = state;
      msgReadIndex--;
      if (msgReadIndex < 0) {
        msgReadIndex = 0;
      }
      updateDisplay();
    }

    state =  digitalRead(Switches[1]);
    //Serial.print("vals ");
    //Serial.println(state);
    //Serial.println(SwitchStates[1]);
    if (state != SwitchStates[1]) {
      SwitchStates[1] = state;
      msgReadIndex++;
      if (msgReadIndex >= numMsgs) {
        msgReadIndex = numMsgs - 1;
      }
      updateDisplay();
    }

    if (digitalRead(Buttons[0]) == HIGH && !ButtonStates[0]) {
      ButtonStates[0] = true;
      Serial.println("pressed");
      digitalWrite(likeLED, HIGH);
    }
    if (digitalRead(Buttons[0]) == LOW && ButtonStates[0]) {
      ButtonStates[0] = false;
    }

    if (digitalRead(Buttons[1]) == HIGH && !ButtonStates[1]) {
      ButtonStates[1] = true;
      Serial.println("pressed");
      digitalWrite(bookmarkLED, HIGH);
    }
    if (digitalRead(Buttons[1]) == LOW && ButtonStates[1]) {
      ButtonStates[1] = false;
    }

    // Get the desired cursor y location
    int newPotPosition = getPotPosition(Potentiometer);
    if (newPotPosition != oldPotPosition)
    {
      y = map(newPotPosition, 0, 100, -numMsgs - 1, maxCharsY);
      oldPotPosition = newPotPosition;
      updateDisplay();
    }
  }

  delay(100);
}

void updateDisplay() {
  //map(value, fromLow, fromHigh, toLow, toHigh)
  // The min pos is all messages just off the screen (top)
  // The max pos is all message just off the screen (bottom)

  Serial.print(y);
  Serial.print(" ");
  Serial.print(msgReadIndex);
  Serial.print(" ");
  Serial.println(comments[msgReadIndex].message);


  // Display all the messages to the screen in the correct order
  // Only displayed when pot position changes to prevent screen flicker
  OrbitOledClear();
  /* for(int i = 0; i < numMsgs; i++)
    {
       int yPos = y + i;

       // Don't draw messages if they are not on the screen
       if(yPos < 0 || yPos > maxCharsY - 2)
           continue;

       OrbitOledSetCursor(0, yPos);
       OrbitOledPutString((char*) msgs[i].c_str());
    }*/
  OrbitOledSetCursor(0, y);
  OrbitOledPutString((char*) (comments[msgReadIndex].author + ": " + comments[msgReadIndex].message).c_str());

}


