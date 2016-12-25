/*
    Heizung
    
    Copyright (C) 2016 Mandl

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


#include <Bounce.h>
#include <avr/wdt.h>

// Pin Definitions
const int BETRIEBS_STUNDEN = 11;
const int BRENNER_STOERUNG = 9;
const int BLINK_LICHT = 12;
const int HEIZUNG_AN = 8;

int Betriebsstunden = 1;
int BrennerStoerung = 1; 
int Spannung = 0; // Akku Spannung
int PT1000_1 = 0; // Temperatur
int reset = 1;

String inputString = "";         // a string to hold incoming data
boolean stringComplete = false;  // whether the string is complete

Bounce BrennerStoerungbouncer = Bounce( BRENNER_STOERUNG,1200 ); 
Bounce Betriebsstundenbouncer = Bounce( BETRIEBS_STUNDEN,800); 

// the setup routine runs once when you press reset:
void setup() {    

  // initialize the digital pin as an output.
  pinMode(HEIZUNG_AN, OUTPUT);    
  pinMode(BLINK_LICHT, OUTPUT);  

  pinMode(BETRIEBS_STUNDEN, INPUT_PULLUP);  // Betriebsstunden
  pinMode(BRENNER_STOERUNG, INPUT_PULLUP);  // Brenner stoerung

  Serial1.begin(9600);          //  setup serial
  inputString.reserve(200);
  wdt_enable(WDTO_8S);
  //Serial1.println("ready");  

}

// the loop routine runs over and over again forever:
void loop() {

  BrennerStoerungbouncer.update();
  Betriebsstundenbouncer.update();
  
 
  
  BrennerStoerung = BrennerStoerungbouncer.read();
  if(BrennerStoerung == 1)
  {
    // Blinklicht aus
    digitalWrite(BLINK_LICHT, LOW);  
  }
  else
  {
    digitalWrite(BLINK_LICHT, HIGH);  
  } 

  // print the string when a newline arrives:
  if (stringComplete) {

    //Serial1.println(inputString); // echo string

    if(inputString == "an")
    {
      digitalWrite(HEIZUNG_AN, HIGH);  
      Serial1.println("an_ok");  
      
    }
    else if(inputString == "aus")
    {
      digitalWrite(HEIZUNG_AN, LOW);  
      Serial1.println("aus_ok"); 
       
    }  
    else if(inputString=="status")
    {
      // werte lesen
      Spannung = analogRead(0);
      BrennerStoerung = BrennerStoerungbouncer.read();
      Betriebsstunden = Betriebsstundenbouncer.read();
      PT1000_1 = analogRead(1);

      Serial1.print("status,"); 
      Serial1.print(reset);  
      Serial1.print(",");  
      Serial1.print(!BrennerStoerung);  
      Serial1.print(",");  
      Serial1.print(!Betriebsstunden);  
      Serial1.print(",");  
      Serial1.print(Spannung); 
      Serial1.print(",");  
      Serial1.println(PT1000_1);
      
      reset = 0;
    }  

    else
    {
      Serial1.println("error");
      
    }  
    // clear the string:
    inputString = "";
    stringComplete = false;
  } 

  while (Serial1.available()) {
    // get the new byte:
    char inChar = (char)Serial1.read(); 
    // add it to the inputString:

    //Serial1.println(inChar,HEX);
    // if the incoming character is a newline, set a flag
    // so the main loop can do something about it:
    if (inChar == 0x0A) {
      stringComplete = true;
      break;
    } 
    if(inChar != 0x0D)
    {
     inputString += inChar;
    }
    
   
  }

  wdt_reset();

}




