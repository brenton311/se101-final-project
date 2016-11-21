char val; // variable to receive data from the serial port
#define LED GREEN_LED
void setup() {
  pinMode(LED, OUTPUT);  // pin 48 (on-board LED) as OUTPUT
  Serial1.begin(9600); 
  Serial.begin(19200);       // start serial communication at 9600bps
}
void loop() {
  if( Serial1.available() )       // if data is available to read
  {
    val = Serial1.read();         // read it and store it in 'val'
    Serial.println(val);
  }
  if( val == 'H' )               // if 'H' was received
  {
    digitalWrite(LED, HIGH);  // turn ON the LED
  } else { 
    digitalWrite(LED, LOW);   // otherwise turn it OFF
  }
  delay(100);                    // wait 100ms for next reading
}
