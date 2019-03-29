#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include<EEPROM.h>
#include <ESP8266WebServer.h>
#include "FirebaseArduino.h"
#define FIREBASE_HOST "project-85a2a.firebaseio.com"
#define FIREBASE_AUTH "ZCo27yFFWGtJoRVa67pdX6nM8LXUGMC6l0Xk519S"
const char* ssid = "NETGEAR82";
const char* password = "waterypond850";
int address = 0;
int pin = 12;//D6
int addr = 10;
String mailId;
int exists;
ESP8266WebServer server(80);

//no need authentification
void offLED(){
  String message = "OFF LED";
  server.send(200, "text/plain", message);
  Serial.println(server.args());
  digitalWrite(BUILTIN_LED, LOW);  // turn on LED with voltage HIGH
}
void clear()
{
  for(int i = 0; i < 512; i++)
  {
    EEPROM.write(i,0);
  }
  EEPROM.commit();
}
void writeAccount(){
  String mail = server.arg(0);
  for(int i = 0;i<mail.length();i++)
  {
    EEPROM.write(addr+i,mail[i]);
  }
  EEPROM.write(addr+mail.length(),'\0');
  if(mail.length()>0)
  {
    EEPROM.write(address,1);
    EEPROM.commit();
  }
  else
  {
    clear();
  }
}
void signOut()
{  
 
  mailId = readAccount();
  String path = "/USERS/"+mailId+"/key";
  String validKey = Firebase.getString(path);
  String key = server.arg(0);
    Serial.println(key);
    Serial.println(validKey);
  if(key == validKey)
  {
    clear();
    String message = "SUCCESS";
    Serial.println(message);
    server.send(200, "text/plain", message);
  }
  else
  {
    String message = {"FAILURE"};
    Serial.println(message);
    server.send(200, "text/plain", message);
  } 
}
String readAccount()
{
  char k;
  char tmp[100];
  int len = 0;
  k = EEPROM.read(addr);
  while(k!='\0')
  {
    k = EEPROM.read(addr+len);
    tmp[len] = k;
    len++; 
  }
  tmp[len] = '\0';
  return String(tmp);
}
void checkAccount(){
  int val;
  val = EEPROM.read(address);
    Serial.println(val);
  if(val == 0)
  {
    String message = {"YES"};
    server.send(200, "text/plain", message);
  }
  else
  {
    String message = {"NO"};
    server.send(200, "text/plain", message);
  }
}
void verifyUser()
{
  mailId = readAccount();
  String path = "/USERS/"+mailId+"/key";
  String validKey = Firebase.getString(path);
  String key = server.arg(0);
    Serial.println(key);
    Serial.println(validKey);
  if(key == validKey)
  {
    String message = {"VALID"};
    Serial.println(message);
    server.send(200, "text/plain", message);
  }
  else
  {
    String message = {"INVALID"};
    Serial.println(message);
    server.send(200, "text/plain", message);
  }
}
void onLED(){
  String message = {"ON LED"};
  server.send(200, "text/plain", message);
  Serial.println(server.args());
  digitalWrite(BUILTIN_LED, HIGH);  // turn on LED with voltage HIGH
}
void lock()
{
  String message = {"lock"};
  server.send(200, "text/plain", message);
  Serial.println(message);
  digitalWrite(BUILTIN_LED, LOW);
  digitalWrite(pin, LOW); 
}
void unlock()
{
    String message = {"unlock"};
  server.send(200, "text/plain", message);
  Serial.println(message);
  digitalWrite(BUILTIN_LED, HIGH); 
  digitalWrite(pin, HIGH);
}
void connection(){
  String message = {"Connected"};
  server.send(200, "text/plain", message);
  digitalWrite(BUILTIN_LED, HIGH);
}

void setup(void){  
  Serial.begin(115200);
  WiFi.begin(ssid, password);
  Serial.println("");
  EEPROM.begin(512);
  Firebase.begin(FIREBASE_HOST,FIREBASE_AUTH);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.print("Connected to ");
  Serial.println(ssid);
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());

  pinMode(pin,OUTPUT);
  pinMode(BUILTIN_LED, OUTPUT);// Onboard LED
  server.on("/off", offLED);
  server.on("/on", onLED);
  server.on("/checkAccount",checkAccount);
  server.on("/", connection);
  server.on("/writeAccount", writeAccount);
  server.on("/verifyUser", verifyUser);
  server.on("/lock",lock);
  server.on("/unlock",unlock);
  server.on("/signout",signOut);
  
  server.begin();
  Serial.println("HTTP server started");
  int val;
  val = EEPROM.read(address);
    Serial.println(val);
  if(val == 0)
  {
    String message = {"YES"};
    server.send(200, "text/plain", message);
  }
  else
  {
    String message = {"NO"};
    server.send(200, "text/plain", message);
  }
}

void loop(void){
  server.handleClient();
}
