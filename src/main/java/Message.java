/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
        
/**
 *Represent a single QuickChat message, along with the validated rules,
 * hash generation, send/store/disregard logic,
 * search, delete, and report on message.
 * 
 * @author Karabo Mkhize
 */
public class Message {
    
    //Messages shown to the user
    public static final String RECIPIENT_SUCCESS = "Cell phone number successfully captured.";
    public static final String RECIPIENT_ERROR = "Cell phone number is incorrectly formatted or does not "
       + "contain an international code. Please correct the number and try again.";
    public static final String LENGTH_SUCCESS = "Message ready to send.";
    
//Flags that record what happened to each message
private static final String SENT = "Sent";
private static final String STORED = "Stored";
private static final String DISREGARDED = "Disregarded";

//Parallel arrays: the same index in each of these lists belongs to the
//same message, so the Message ID and Message Hash arrays can be used to
//find the matching recipient, sender, message, and flag
private static final List<String> messageIDs = new ArrayList<>();
private static final List<String> messageHashes = new ArrayList<>();
private static final List<String> messageSenders = new ArrayList<>();
private static final List<String> messageRecipients = new ArrayList<>();
private static final List<String> messageTexts = new ArrayList<>();
private static final List<String> messageFlags = new ArrayList<>();


//One array per flag, each holding the message text. They start empty and
//are populated while the program runs (nothing is hard-coded)
private static final List<String> sentMessages = new ArrayList<>();
private static final List<String> disregardedMessages = new ArrayList<>();
private static final List<String> storedMessages = new ArrayList<>();

private static int totalMessagesSent = 0;
private static int messageCounter = 0; //assigns each message its sequence number
private static String storageFile = "messages.json";
private static String currentSender = "Unknown";

private final String messageID;
private final int messageNumber;
private final String recipient;
private final String messageText;
private final String messageHash;
private final String sender;
private String status; // "Sent", "Stored", or "Disregarded"


//Normal use: the message ID is generated automatically
public Message(String recipient, String messageText) {
this.messageID = generateMessageID();
this.messageNumber = messageCounter++;
this.recipient = recipient;
this.messageText = messageText;
this.sender = currentSender;
this.messageHash = createMessageHash();
}


//Test use: lets a unit test supply a fixed message ID, so the hash
//can be checked deterministically instead of against a random value
public Message(String messageID, String recipient, String messageText) {
this.messageID = messageID;
this.messageNumber = messageCounter++;
this.recipient = recipient;
this.messageText = messageText;
this.sender = currentSender;
this.messageHash = createMessageHash();
}


private String generateMessageID() {
  Random random = new Random();
  StringBuilder id = new StringBuilder();
  for (int i = 0; i < 10; i++) {
   id.append(random.nextInt(10));
}
return id.toString();
}


//Sets who the messages are being sent from (the logged-in user)
public static void setSender(String senderName) {
currentSender = senderName;
}


//Clears every array and counter, and points the JSON storage at a
//separate test file - used only between unit tests so each test starts
//clean and never touches the real messages.json
public static void resetForTesting() {
  messageCounter = 0;
  totalMessagesSent = 0;
  currentSender = "Unknown";
  messageIDs.clear();
  messageHashes.clear();
  messageSenders.clear();
  messageRecipients.clear();
  messageTexts.clear();
  messageFlags.clear();
  sentMessages.clear();
  disregardedMessages.clear();
  storedMessages.clear();
  
  storageFile = "messages-test.json";
  File file = new File(storageFile);
  if (file.exists()) {
      file.delete();
}
}



// ------------------------------------------------------------------
// Validation
// ------------------------------------------------------------------


//A valid recipient starts with the +27 country code, followed by digits
//only, and the number after the code is no more than ten characters long
public static boolean recipientIsValid(String recipient) {
return recipient != null && recipient.matches("\\+27\\d{1,10}");
}

public static boolean lengthIsValid(String text) {
  return text != null && text.length() <= 250;
}

public static String checkLength(String text) {
if (lengthIsValid(text)) {
return LENGTH_SUCCESS;
}
int excess = (text == null ? 0 : text.length()) - 250;
return "Message exceeds 250 characters by " + excess + "; please reduce the size.";
}


//Ensures that the message ID is not more than ten characters
public boolean checkMessageID() {
   return messageID != null && messageID.length() <= 10;
}

//Returns a status line confirming the generated message ID
public String generateMessageIDStatus() {
return "Message ID generated: " + messageID;
}


//Ensures the recipient cell number is valid and starts with a code
public String checkRecipientCell() {
if (recipientIsValid(recipient)) {
return RECIPIENT_SUCCESS;
}
return RECIPIENT_ERROR;
}

//Ensures the message is no longer than 250 characters
public String checkMessageLength() {
return checkLength(messageText);
}


//Builds the Message Hash: the first two digits of the message ID, the
//message's sequence number, and the first and last words of the
//message, all in capitals - e.g. 00:0:HITHANKS
public final String createMessageHash() {
String firstTwo = messageID.substring(0, 2);
String[] words = messageText.trim().split("\\s+");
String firstWord = words[0].replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
String lastWord = words[words.length - 1].replaceAll("[^a-zA-Z0-9]","").toUpperCase();
return firstTwo + ":" + messageNumber + ":" + firstWord + lastWord;
}


// ------------------------------------------------------------------
// Send, disregard, or store - this is where the arrays are populated
// ------------------------------------------------------------------
//Lets the user choose whether to send, store, or disregard the message.
//choice: 1 = send, 2 = disregard, 3 = store
public String sentMessage(int choice) {
switch (choice) {
case 1:
status = SENT;
totalMessagesSent++;
addEntry(messageID, messageHash, sender, recipient, messageText,SENT);
return "Message successfully sent.";

case 2:
status = DISREGARDED;
addEntry(messageID, messageHash, sender, recipient, messageText,DISREGARDED);
return "Press 0 to delete the message.";

case 3:
status = STORED;
storeMessage();
addEntry(messageID, messageHash, sender, recipient, messageText,STORED);
return "Message successfully stored.";
default:
return "Invalid option selected.";
}
}


//Adds one message to the parallel arrays and to the array for its flag
private static void addEntry(String id, String hash, String sender,
    String recipient, String text, String flag) {
 messageIDs.add(id);
 messageHashes.add(hash);
 messageSenders.add(sender);
 messageRecipients.add(recipient);
 messageTexts.add(text);
 messageFlags.add(flag);
 
if (flag.equals(SENT)) {
  sentMessages.add(text);
} else if (flag.equals(STORED)) {
  storedMessages.add(text);
} else {
  disregardedMessages.add(text);
}
}


//Removes the message at this index from every parallel array and from
//the array for its flag
private static void removeEntry(int index) {
String flag = messageFlags.get(index);


//Work out the position of this message inside its own flag array
int flagIndex = 0;
for (int i = 0; i < index; i++) {
if (messageFlags.get(i).equals(flag)) {
flagIndex++;
}
}

if (flag.equals(SENT)) {
sentMessages.remove(flagIndex);
} else if (flag.equals(STORED)) {
storedMessages.remove(flagIndex);
} else {
disregardedMessages.remove(flagIndex);
}

  messageIDs.remove(index);
  messageHashes.remove(index);
  messageSenders.remove(index);
  messageRecipients.remove(index);
  messageTexts.remove(index);
  messageFlags.remove(index);
}


// ------------------------------------------------------------------
// JSON storage: write a message, read them all back, rewrite the file
// ------------------------------------------------------------------

//Stores this message's details as a JSON object, appended as one line
//to the storage file. A small hand-written JSON writer is used so the
//project does not need an external JSON library.
public void storeMessage() {
String json = toJson(messageID, messageHash, sender, recipient,messageText, STORED);

try (FileWriter writer = new FileWriter(storageFile, true)) {
 writer.write(json);
 writer.write(System.lineSeparator());
} catch (IOException e) {
   System.out.println("Could not store message: " + e.getMessage());
}
}


//Reads the JSON file back into the arrays, so stored messages from
//earlier runs are available too. Stored messages already in the arrays
//are cleared first so nothing is duplicated.
//Reference: add the source you used for reading a file line by line here
//(e.g. the Oracle Java API documentation for BufferedReader).
public static void loadStoredMessages() {
for (int i = messageFlags.size() - 1; i >= 0; i--) {
if (messageFlags.get(i).equals(STORED)) {
removeEntry(i);
}
}
File file = new File(storageFile);
if (!file.exists()) {
return;
}


try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
String line;
while ((line = reader.readLine()) != null) {
if (line.trim().isEmpty()) {
continue;
}

addEntry(readJsonValue(line, "messageID"),
readJsonValue(line, "messageHash"),
readJsonValue(line, "sender"),
readJsonValue(line, "recipient"),
readJsonValue(line, "message"),
STORED);
}
} catch (IOException e) {
System.out.println("Could not read stored messages: " + e.getMessage());
}
}


//Rewrites the whole JSON file from the stored messages still in the arrays
private static void rewriteStoredFile() {
try (FileWriter writer = new FileWriter(storageFile, false)) {
for (int i = 0; i < messageFlags.size(); i++) {
if (messageFlags.get(i).equals(STORED)) {
writer.write(toJson(messageIDs.get(i), messageHashes.get(i),
messageSenders.get(i), messageRecipients.get(i),
messageTexts.get(i), STORED));
writer.write(System.lineSeparator());
}
}
} catch (IOException e) {
    System.out.println("Could not update stored messages: " + e.getMessage());
}
}

private static String toJson(String id, String hash, String sender,
String recipient, String text, String status) {
return "{"
+ "\"messageID\":\"" + escapeJson(id) + "\","
+ "\"messageHash\":\"" + escapeJson(hash) + "\","
+ "\"sender\":\"" + escapeJson(sender) + "\","
+ "\"recipient\":\"" + escapeJson(recipient) + "\","
+ "\"message\":\"" + escapeJson(text) + "\","
+ "\"status\":\"" + escapeJson(status) + "\""
+ "}";
}

private static String escapeJson(String value) {
if (value == null) {
return "";
}
return value.replace("\\", "\\\\").replace("\"", "\\\"");
}


//Pulls the value for one key out of a single line of JSON
private static String readJsonValue(String json, String key) {
String marker = "\"" + key + "\":\"";
int start = json.indexOf(marker);
if (start == -1) {
return "";
}
start += marker.length();

StringBuilder value = new StringBuilder();
for (int i = start; i < json.length(); i++) {
char c = json.charAt(i);
if (c == '\\' && i + 1 < json.length()) {
value.append(json.charAt(i + 1)); //an escaped character
i++;
} else if (c == '"') {
break; //end of the value
} else {
value.append(c);
}
}
return value.toString();
}


// ------------------------------------------------------------------
// Reading the arrays (used by the unit tests)
// ------------------------------------------------------------------
public static List<String> getSentMessages() {
return new ArrayList<>(sentMessages);
}
public static List<String> getDisregardedMessages() {
return new ArrayList<>(disregardedMessages);
}
public static List<String> getStoredMessages() {
return new ArrayList<>(storedMessages);
}
public static List<String> getMessageHashes() {
return new ArrayList<>(messageHashes);
}
public static List<String> getMessageIDs() {
return new ArrayList<>(messageIDs);
}


// ------------------------------------------------------------------
// Stored Messages menu features
// ------------------------------------------------------------------

//a. The sender and recipient of every stored message
public static String displaySenderAndRecipient() {
StringBuilder result = new StringBuilder();
for (int i = 0; i < messageFlags.size(); i++) {
if (messageFlags.get(i).equals(STORED)) {
result.append("Sender: ").append(messageSenders.get(i))
.append(" | Recipient:").append(messageRecipients.get(i))
.append("\n");
}
}
if (result.length() == 0) {
return "No stored messages.";
}
return result.toString();
}


//b. The longest stored message
public static String longestStoredMessage() {
if (storedMessages.isEmpty()) {
return "No stored messages.";
}
String longest = storedMessages.get(0);
for (String text : storedMessages) {
if (text.length() > longest.length()) {
longest = text;
}
}
return longest;
}


//c. Search for a message ID - returns the message text
public static String searchByMessageID(String id) {
int index = messageIDs.indexOf(id);
if (index == -1) {
return "Message ID not found.";
}
return messageTexts.get(index);
}


//c. Search for a message ID - returns the recipient and the message
public static String displayMessageByID(String id) {
int index = messageIDs.indexOf(id);
if (index == -1) {
return "Message ID not found.";
}
return "Recipient: " + messageRecipients.get(index)
+ "\nMessage: " + messageTexts.get(index);
}


//d. All the messages sent or stored for one recipient
public static List<String> searchByRecipient(String recipient) {
List<String> results = new ArrayList<>();
for (int i = 0; i < messageRecipients.size(); i++) {
boolean sentOrStored = messageFlags.get(i).equals(SENT)
|| messageFlags.get(i).equals(STORED);
if (sentOrStored && messageRecipients.get(i).equals(recipient)) {
results.add(messageTexts.get(i));
}
}
return results;
}


//e. Delete a stored message using its message hash
public static String deleteByMessageHash(String hash) {
for (int i = 0; i < messageHashes.size(); i++) {
if (messageHashes.get(i).equals(hash) &&
messageFlags.get(i).equals(STORED)) {
String text = messageTexts.get(i);
removeEntry(i);
rewriteStoredFile();
return "Message: \"" + text + "\" successfully deleted.";
}
}
return "Message hash not found.";
}

//f. A report with the full details of every stored message
public static String displayStoredReport() {
StringBuilder report = new StringBuilder("===== Stored Messages Report =====\n");
boolean found = false;
for (int i = 0; i < messageFlags.size(); i++) {
if (messageFlags.get(i).equals(STORED)) {
found = true;
report.append("Message ID:").append(messageIDs.get(i)).append("\n")
.append("Message Hash:").append(messageHashes.get(i)).append("\n")
.append("Sender:").append(messageSenders.get(i)).append("\n")
.append("Recipient:").append(messageRecipients.get(i)).append("\n")
.append("Message:").append(messageTexts.get(i)).append("\n\n");
}
}
if (!found) {
return "No stored messages.";
}
return report.toString();
}

//A report of all the sent messages: Message Hash, Recipient, Message
public static String displayReport() {
StringBuilder report = new StringBuilder("===== Sent Messages Report =====\n");
for (int i = 0; i < messageFlags.size(); i++) {
if (messageFlags.get(i).equals(SENT)) {
report.append("Message Hash:").append(messageHashes.get(i)).append("\n")
.append("Recipient:").append(messageRecipients.get(i)).append("\n")
.append("Message:").append(messageTexts.get(i)).append("\n\n");
}
}
return report.toString();
}

//Returns all the messages sent while the program is running, in the
//order required: Message ID, Message Hash, Recipient, Message
public static String printMessages() {
StringBuilder result = new StringBuilder();
for (int i = 0; i < messageFlags.size(); i++) {
if (messageFlags.get(i).equals(SENT)) {
result.append("Message ID:").append(messageIDs.get(i)).append("\n")
.append("Message Hash:").append(messageHashes.get(i)).append("\n")
.append("Recipient:").append(messageRecipients.get(i)).append("\n")
.append("Message:").append(messageTexts.get(i)).append("\n\n");
}
}
return result.toString();
}

//Returns the total number of messages sent so far
public static int returnTotalMessagess() {
return totalMessagesSent;
}

//Getters for a single message
public String getMessageID() { return messageID; }
public String getMessageHash() { return messageHash; }
public String getRecipient() { return recipient; }
public String getMessageText() { return messageText; }
public String getSender() { return sender; }
public int getMessageNumber() { return messageNumber; }
public String getStatus() { return status; }

    static class getSentMessages {

        public getSentMessages() {
        }
    }
    
}
