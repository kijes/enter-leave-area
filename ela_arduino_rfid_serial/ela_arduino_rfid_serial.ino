/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

#include <SPI.h>
#include <RFID.h>

#define SS_PIN 6
#define RST_PIN 7

/*
 * Status commands returned by service
 */
#define COMMAND_OK 0
#define COMMAND_WARN 1
#define COMMAND_FAIL 2

/*
 * buffer for serial transmission
 */
#define SERIAL_BUFFER_LEN 20
char serialBuffer[SERIAL_BUFFER_LEN];

/* 
 * RFID controller
 */
RFID rfid(SS_PIN, RST_PIN); 

/* 
 * status LEDs
 */
int RED_PIN = 8;
int YELLOW_PIN = 9;
int GREEN_PIN = 10;

/*
 * setup routine to configure:
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
  pinMode(RED_PIN, OUTPUT);
  pinMode(YELLOW_PIN, OUTPUT);
  pinMode(GREEN_PIN, OUTPUT);
  digitalWrite(RED_PIN, HIGH);
  digitalWrite(GREEN_PIN, LOW);
  digitalWrite(YELLOW_PIN, LOW);
}

/*
 * reading data into buffer from serial (over BT)
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
 */
int sendCommand(char* command, unsigned char* serNum)
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
  digitalWrite(GREEN_PIN, HIGH);
  delay(500);
  digitalWrite(GREEN_PIN, LOW);
}

/*
 * yellow LED short flash on warning
 */
void showStatusWARN()
{
  digitalWrite(YELLOW_PIN, HIGH);
  delay(500);
  digitalWrite(YELLOW_PIN, LOW);
}

/*
 * red LED blinking on fail
 */
void showStatusFAIL()
{
  for (int i=0;i<5;i++) {
    digitalWrite(RED_PIN, LOW);
    delay(100);
    digitalWrite(RED_PIN, HIGH);
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
 * main loop:
 * - reading RFID card
 * - sending enter/leave commands over serial
 * - decoding response and showing the status on leds
 */
void loop()
{
  unsigned char cardNum[] = {52,82,110,26,18};
  int commandStatus = COMMAND_OK;
  if (rfid.isCard() && rfid.readCardSerial()) {
    if (rfid.serNum[0] == cardNum[0] &&
        rfid.serNum[1] == cardNum[1] &&
        rfid.serNum[2] == cardNum[2] && 
        rfid.serNum[3] == cardNum[3] &&
        rfid.serNum[4] == cardNum[4]) {
      commandStatus = sendCommand("ENTER", cardNum);
      handleCommandStatus(commandStatus);
    } else {
      commandStatus = sendCommand("LEAVE", cardNum);    
      handleCommandStatus(commandStatus);
    }
  }
  rfid.halt();
}

