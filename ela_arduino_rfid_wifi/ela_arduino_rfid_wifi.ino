/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */
 
#include <Adafruit_CC3000.h>
#include <ccspi.h>
#include <SPI.h>
#include <string.h>
#include <aJSON.h>

/*
 * WiFi modconfiguration
 */
#define ADAFRUIT_CC3000_IRQ   3
#define ADAFRUIT_CC3000_VBAT  5
#define ADAFRUIT_CC3000_CS    10
#define IDLE_TIMEOUT_MS  10000
Adafruit_CC3000 cc3000 = Adafruit_CC3000(ADAFRUIT_CC3000_CS, 
                                        ADAFRUIT_CC3000_IRQ, 
                                        ADAFRUIT_CC3000_VBAT,
                                        SPI_CLOCK_DIVIDER);
#define WLAN_SSID       "Your WiFi SSID"
#define WLAN_PASS       "Your WiFi password"
#define WLAN_SECURITY   WLAN_SEC_WPA2

/*
 * ELA service commands
 */
char* ENTER_COMMAND = "enter";
char* LEAVE_COMMAND = "leave";
char* ELA_SERVICE_HOST = "ela-service-webapp.appspot.com";

/*
 * Status commands returned by service
 */
#define COMMAND_OK 0
#define COMMAND_WARN 1
#define COMMAND_FAIL 2

uint32_t ip = 0;

const unsigned char cardNum[5] = {52,82,110,26,18}; 

#define DEBUG 1
#ifdef DEBUG
#define DEBUG_PRINT(msg) Serial.print(msg)
#define DEBUG_PRINTLN(msg) Serial.println(msg)
#else
#define DEBUG_PRINT(msg)
#define DEBUG_PRINTLN(msg)
#endif

#define DATA_BUFFER_LEN 200
char data_buffer[DATA_BUFFER_LEN];

uint16_t checkFirmwareVersion()
{
  uint8_t major, minor;
  uint16_t version;
  
  if(!cc3000.getFirmwareVersion(&major, &minor)) {
    DEBUG_PRINTLN(F("Unable to retrieve the firmware version!"));
    version = 0;
  } else {
    DEBUG_PRINT(F("Firmware Version:")); DEBUG_PRINT(major); DEBUG_PRINT(F(".")); DEBUG_PRINTLN(minor);
    version = major; 
    version <<= 8; 
    version |= minor;
  }
  return version;
}

void hang(const __FlashStringHelper* msg)
{
  DEBUG_PRINTLN(msg);
  while(1) ;  
}

/*
 * setup routine to configure:
 * - WIFI connection
 * - RFID controller
 * - pins for LEDs
 */
void setup()
{
  if (DEBUG) {
    Serial.begin(115200);
  }

  DEBUG_PRINTLN(F("Initialising CC3000 ...")); 
  
  if (!cc3000.begin()) {
    hang(F("Unable to initialise the CC3000!"));
  }

  uint16_t firmware = checkFirmwareVersion();
  if ((firmware != 0x113) && (firmware != 0x118)) {
    hang(F("Wrong firmware version!"));
  }
  
  DEBUG_PRINTLN(F("Deleting old connection profiles"));
  if (!cc3000.deleteProfiles()) {
    hang(F("Failed to delete old connection profiles!"));
  }

  char* ssid = WLAN_SSID;
  DEBUG_PRINT(F("Attempting to connect to:")); DEBUG_PRINTLN(ssid);
  
  if (!cc3000.connectToAP(WLAN_SSID, WLAN_PASS, WLAN_SECURITY)) {
    hang(F("Failed to connect!"));
  }

  DEBUG_PRINTLN(F("Connected!"));
  
  DEBUG_PRINTLN(F("Requesting DHCP"));
  while (!cc3000.checkDHCP()) {
    delay(100);
  }  

  DEBUG_PRINT(F("Resolving:")); DEBUG_PRINTLN(ELA_SERVICE_HOST);
  while (ip == 0) {
    if (!cc3000.getHostByName(ELA_SERVICE_HOST, &ip)) {
      DEBUG_PRINTLN(F("Couldn't resolve!"));
    }
    delay(500);
  }

  DEBUG_PRINT(F("Resolved to:"));
  if (DEBUG) {
    cc3000.printIPdotsRev(ip);
    DEBUG_PRINTLN("");
  }
  DEBUG_PRINTLN(F("Finished initialization of CC3000")); 
}

/*
 * parsing status command
 */
int parseStatusCommand(char* statusCmd)
{
  DEBUG_PRINT(F("Parsing status code:")); DEBUG_PRINTLN(statusCmd);
  
  aJsonObject* jsonObject = aJson.parse(statusCmd);
  aJsonObject* jsonStatusCode = aJson.getObjectItem(jsonObject, "status_code");
  if (jsonStatusCode == NULL) {
    return COMMAND_FAIL;
  }
  int statusCode = atoi(jsonStatusCode->valuestring);
  aJson.deleteItem(jsonObject);
  return statusCode;
}

/*
 * reading response data
 */
int readStatus(Adafruit_CC3000_Client& elaService) 
{
  DEBUG_PRINT(F("Reading response from:")); DEBUG_PRINTLN(ELA_SERVICE_HOST);

  enum ParsingPhase {
    PP_HEADER_STATUS = 0,
    PP_HEADER,
    PP_BODY_CHUNK_LEN,
    PP_BODY_CHUNK_DATA,
    PP_BODY_END
  };
  ParsingPhase parsingPhase = PP_HEADER_STATUS;
  int writeIdx = 0;
  unsigned long lastRead = millis();
  int statusCode = COMMAND_FAIL;
  boolean cr = false;
  unsigned long dataLen = 0;
  
  while (elaService.connected() && (millis() - lastRead < IDLE_TIMEOUT_MS)) {
    while (elaService.available()) {
      char c = elaService.read();
      if (parsingPhase != PP_BODY_END) {
        if (c == '\r') {
          cr = true;
        } else if (c == '\n' && cr) {
          cr = false;
          data_buffer[writeIdx] = '\0';
          DEBUG_PRINT(F("DATA:")); DEBUG_PRINTLN(data_buffer);
          writeIdx = 0;
          switch (parsingPhase) {
            case PP_HEADER_STATUS: {
              if (strstr(data_buffer, "200 OK") == NULL) {
                parsingPhase = PP_BODY_END;
              } else {
                parsingPhase = PP_HEADER;
              }
              break;
            }
            case PP_HEADER: {
              if (strlen(data_buffer) == 0) {
                parsingPhase = PP_BODY_CHUNK_LEN;
              }
              break;
            }
            case PP_BODY_CHUNK_LEN: {
              dataLen = strtol(data_buffer, 0, 16);
              DEBUG_PRINT(F("CHUNK LEN:")); DEBUG_PRINTLN(dataLen);
              if (dataLen > 0) {
                parsingPhase = PP_BODY_CHUNK_DATA;
              } else {
                parsingPhase = PP_BODY_END;
              }
              break;
            }
            case PP_BODY_CHUNK_DATA: {
              DEBUG_PRINTLN(F("CHUNK DATA"));
              statusCode = parseStatusCommand(data_buffer);
              parsingPhase = PP_BODY_CHUNK_LEN;
              break;
            }            
            default: {
              hang(F("Invalid state!"));
            }
          }
        } else {
          DEBUG_PRINT(c);
          data_buffer[writeIdx++] = c;
        }
        lastRead = millis();
      }
    }
  }
  return statusCode;
}

/*
 * sending command with card number
 */
int sendCommand(const char* command, const unsigned char cardNum[5])
{
  #define CMD_BUF_LEN 30
  char cmdBuff[CMD_BUF_LEN];

  DEBUG_PRINT(F("Sending command:")); DEBUG_PRINTLN(command);
  snprintf(cmdBuff, CMD_BUF_LEN, "/ela/%s/%02X%02X%02X%02X%02X", command, cardNum[0], cardNum[1], cardNum[2], cardNum[3], cardNum[4]);

  DEBUG_PRINT(F("Requesting URL:")); DEBUG_PRINTLN(cmdBuff);

  Adafruit_CC3000_Client elaService = cc3000.connectTCP(ip, 80);
  if (elaService.connected()) {
    elaService.fastrprint(F("PUT "));
    elaService.fastrprint(cmdBuff);
    elaService.fastrprint(F(" HTTP/1.1\r\n"));
    elaService.fastrprint(F("Host: ")); 
    elaService.fastrprint(ELA_SERVICE_HOST); 
    elaService.fastrprint(F("\r\n"));
    elaService.fastrprint(F("Content-Type: application/json; charset=UTF-8")); 
    elaService.fastrprint(F("\r\n"));
    elaService.fastrprint(F("Content-Length: "));   
    if (strcmp(command, ENTER_COMMAND) == 0) {
      elaService.print(23);
      elaService.print(F("\r\n\r\n"));
      elaService.print(F("{\"entered_area\":\"true\"}"));
    } else {
      elaService.print(24);
      elaService.print(F("\r\n\r\n"));
      elaService.print(F("{\"entered_area\":\"false\"}"));
    }
  } else {
    DEBUG_PRINTLN(F("Connection failed"));    
    return COMMAND_FAIL;
  }
  int elaStatus = readStatus(elaService);
  elaService.close();  
  return elaStatus;
}

/*
 * green LED short flash on success
 */
void showStatusOK()
{
  DEBUG_PRINTLN(F("OK"));
}

/*
 * yellow LED short flash on warning
 */
void showStatusWARN()
{
  DEBUG_PRINTLN(F("WARN"));
}

/*
 * red LED blinking on fail
 */
void showStatusFAIL()
{
  DEBUG_PRINTLN(F("FAIL"));
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
 * - sending enter/leave commands over WiFi
 * - decoding response and showing the status on leds
 */
void loop()
{  
  int commandStatus = sendCommand(ENTER_COMMAND, cardNum);
  handleCommandStatus(commandStatus);
  delay(5000);
  commandStatus = sendCommand(LEAVE_COMMAND, cardNum);    
  handleCommandStatus(commandStatus);
  delay(5000);
}
