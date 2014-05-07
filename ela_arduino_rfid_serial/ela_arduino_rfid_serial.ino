/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

#include <SPI.h>
#include <RFID.h>

/*
 * Status commands returned by service
 */
#define COMMAND_OK 0
#define COMMAND_WARN 1
#define COMMAND_FAIL 2

char* ENTER_COMMAND = "ENTER";
char* LEAVE_COMMAND = "LEAVE";

/*
 * buffer for serial transmission
 */
#define SERIAL_BUFFER_LEN 20
char serialBuffer[SERIAL_BUFFER_LEN];

/* 
 * RFID reader
 */
#define SS_PIN 6
#define RST_PIN 7
 
RFID rfid(SS_PIN, RST_PIN); 

/* 
 * status LEDs
 */
int redLedPin = 8;
int yellowLedPin = 9;
int greenLedPin = 10;

/* 
 * card number used for enter command
 */
const unsigned char enterCardNum[] = {52,82,110,26,18};

/*
 * Setup routine to configure:
 * - serial transmission
 * - SPI interface
 * - RFID controller
 * - pins for LEDs
 */
void setup()
{
  Serial.begin(9600);    
  SPI.begin(); 
  rfid.init();
  pinMode(redLedPin, OUTPUT);
  pinMode(yellowLedPin, OUTPUT);
  pinMode(greenLedPin, OUTPUT);
  digitalWrite(redLedPin, HIGH);
  digitalWrite(greenLedPin, LOW);
  digitalWrite(yellowLedPin, LOW);
}

/*
 * reading data into buffer from serial
 */
int readData(char* buff)
{
  int numChar = 0;
  int index=0;
  if (Serial.available() > 0) {
    delay(100);
    numChar = Serial.available();
    while (numChar--) {
      if (index < SERIAL_BUFFER_LEN) {
        buff[index++] = Serial.read();
      }
    }
  }
  return index;
}

/*
 * parsing status command
 */
int parseStatusCommand(char* buffer)
{
  char* token = strtok(buffer, ":");
  if (token == NULL) {
    return COMMAND_FAIL;
  }
  if (strcmp(token, "STATUS") != 0) {
    return COMMAND_FAIL;
  }  
  token = strtok(NULL, ":");
  if (token == NULL) {
    return COMMAND_FAIL;
  }
  return atoi(token);
}

/*
 * reading status from serial
 * E.g.: <STATUS:0>
 */
int readStatus() 
{
  bool statusRead = false;
  bool commandStarted = false;
  char tmpSerialBuffer[SERIAL_BUFFER_LEN];
  int index = 0;
  while (!statusRead) {
    int dataCount = readData(tmpSerialBuffer);
    for (int i=0;i<dataCount;i++) {
      if (!commandStarted) {
        if (tmpSerialBuffer[i] == '<') {
          commandStarted = true;
        }
      } else {
        if (tmpSerialBuffer[i] == '>') {
          commandStarted = false;
          statusRead = true;
          break;
        } else {
          if (index < SERIAL_BUFFER_LEN) {
            serialBuffer[index++] = tmpSerialBuffer[i];
          }
        }
      }
    }
  }  
  return parseStatusCommand(serialBuffer);  
}

/*
 * sending command with card number over serial
 * E.g.: <LEAVE:34526E1A12>
 */
int sendCommand(const char* command, const unsigned char* serNum)
{
  Serial.print("<");
  Serial.print(command);
  Serial.print(":");
  Serial.print(serNum[0],HEX);
  Serial.print(serNum[1],HEX);
  Serial.print(serNum[2],HEX);
  Serial.print(serNum[3],HEX);
  Serial.print(serNum[4],HEX);
  Serial.print(">");
  
  return readStatus();
}

/*
 * green LED short flash on success
 */
void showStatusOK()
{
  digitalWrite(greenLedPin, HIGH);
  delay(500);
  digitalWrite(greenLedPin, LOW);
}

/*
 * yellow LED short flash on warning
 */
void showStatusWARN()
{
  digitalWrite(yellowLedPin, HIGH);
  delay(500);
  digitalWrite(yellowLedPin, LOW);
}

/*
 * red LED blinking on fail
 */
void showStatusFAIL()
{
  for (int i=0;i<5;i++) {
    digitalWrite(redLedPin, LOW);
    delay(100);
    digitalWrite(redLedPin, HIGH);
    delay(100);
  }
}

/*
 * decoding command execution status
 */
void handleCommandStatus(int commandStatus) 
{
  if (commandStatus == COMMAND_OK) {
    showStatusOK();
  } else if (commandStatus == COMMAND_WARN) {
    showStatusWARN();
  } else {
    showStatusFAIL();
  }
}

/*
 * Main loop:
 * - reading RFID card
 * - sending enter/leave commands over serial
 * - decoding response and showing the status on leds
 */
void loop()
{
  int commandStatus = COMMAND_OK;
  if (rfid.isCard() && rfid.readCardSerial()) {
    if (rfid.serNum[0] == enterCardNum[0] &&
        rfid.serNum[1] == enterCardNum[1] &&
        rfid.serNum[2] == enterCardNum[2] && 
        rfid.serNum[3] == enterCardNum[3] &&
        rfid.serNum[4] == enterCardNum[4]) {
      commandStatus = sendCommand(ENTER_COMMAND, enterCardNum);
      handleCommandStatus(commandStatus);
    } else {
      commandStatus = sendCommand(LEAVE_COMMAND, enterCardNum);    
      handleCommandStatus(commandStatus);
    }
  }
  rfid.halt();
}

