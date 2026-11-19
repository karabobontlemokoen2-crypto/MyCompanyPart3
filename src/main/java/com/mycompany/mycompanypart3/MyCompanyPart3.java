/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.mycompanypart3;


import java.util.List;
import java.util.Scanner;

/** 
 * Console-driven QuickChat application. A user must register and log in
 * before the messaging menu appears.
 *
 * @author Karabo Mkhize
 */
public class MyCompanyPart3 {
    
    //Scanner is declared here so it can be reused by every method  
    static Scanner input = new Scanner(System.in);
    
    public static void main (String[] args) {
        
     //----- Registration ----  
    System.out.println("===== Registration ====="); 
    
    System.out.println("Enter your first name: ");
    String firstName = input.nextLine();  
    
    System.out.println("Enter your last name: "); 
    String lastName = input.nextLine();  
    
    Login login = new Login();
    
     String username, password, cellPhoneNumber, registrationResult;
     do {     
         System.out.print("Enter a username: ");
         username = input.nextLine();    
     
     System.out.print("Enter a password: ");   
     password = input.nextLine();
     
     System.out.print("Enter your South African cell phone number (e.g. +27838968976): "); 
     cellPhoneNumber = input.nextLine();          
     
     registrationResult = login.registerUser(username, password, cellPhoneNumber); 
     System.out.println(registrationResult);
     
     } while (!registrationResult.startsWith("Username successfully captured")); 
     
//----- Login ----  
 System.out.println("\n===== Login =====");    
 System.out.println("Enter your username: ");     
 String LoginUsername = input.nextLine();  
 System.out.println("Enter your password: ");    
 String LoginPassword = input.nextLine();
 

 boolean LoginSuccessful = login.loginUser(LoginUsername,LoginPassword);
 System.out.println(login.returnLoginStatus(LoginSuccessful));
if (!LoginSuccessful) {
    System.out.println("Login failed - existing QuickChat.");
    input.close();
    return;
}

//The logged-in user is the sender of every message, and any messages
//stored on an earlier run are read back from the JSON file
Message.setSender(firstName + " " + lastName);
Message.loadStoredMessages();

System.out.println("\nWelcome to QuickChat.");

//Users define how many messages they wish to enter when the
//application starts
    int maxMessages = readNumber("How many messages would you like to enter?");

int messagesEntered = 0;
int choice = 0;

//The application runs until the user chooses to quit
while (choice != 3) {
System.out.println("\n1) Send Messages");
System.out.println("2) Show recently sent messages");
System.out.println("3) Quit");
System.out.println("4) Stored Messages");
choice = readNumber("Enter your choice: ");

switch (choice) {
case 1:
    messagesEntered = sendMessages(maxMessages, messagesEntered);
    break;

case 2:
    System.out.println("Coming Soon.");
    break;

case 3:
   System.out.println("\nThank you for using QuickChat.");
   System.out.println("Total messages sent: " +
Message.returnTotalMessagess());
break;

case 4:
   storedMessagesMenu();
   break;

default:
   System.out.println("Invalid option, please try again.");
}
}

input.close();

    } // close the while loop 
    
//Lets the user enter the messages they still have left. A for loop makes
//sure only the set number of messages can be entered. Returns how many
//messages have been entered so far.
static int sendMessages(int maxMessages, int messagesEntered) {
  if (messagesEntered >= maxMessages) {
  System.out.println("You have already entered your set number of messages.");
return messagesEntered;
}
for (int i = messagesEntered; i < maxMessages; i++) {
System.out.println("\n--- Message " + (i + 1) + " of " + maxMessages + "--- ");

//Keep asking until the recipient is correctly formatted
String recipient;
do {
  System.out.print("Enter recipient cell number: ");
  recipient = input.nextLine();
  if (!Message.recipientIsValid(recipient)) {
System.out.println(Message.RECIPIENT_ERROR);
}
  
} while (!Message.recipientIsValid(recipient));
System.out.println(Message.RECIPIENT_SUCCESS);

//Keep asking until the message is no longer than 250 characters
String messageText;
do {
  System.out.print("Enter your message: ");
  messageText = input.nextLine();
  System.out.println(Message.checkLength(messageText));
} while (!Message.lengthIsValid(messageText));

Message message = new Message(recipient, messageText);

//Send, disregard, or store the message
int sendChoice;
do {
  System.out.println("1) Send Message");
  System.out.println("2) Disregard Message");
  System.out.println("3) Store Message to send later");
  sendChoice = readNumber("Enter your choice: ");
  System.out.println(message.sentMessage(sendChoice));
} while (sendChoice < 1 || sendChoice > 3);

//The full details are shown once a message has been sent
if ("Sent".equals(message.getStatus())) {
System.out.println("\nMessage ID: " + message.getMessageID());
System.out.println("Message Hash: " + message.getMessageHash());
System.out.println("Recipient: " + message.getRecipient());
System.out.println("Message: " + message.getMessageText());
}

messagesEntered++;

}

   System.out.println("\nTotal messages sent so far: " +
Message.returnTotalMessagess());
  return messagesEntered;
}

//The Stored Messages menu - every option reads from the arrays
static void storedMessagesMenu() {
    
//Read the JSON file into the arrays first
Message.loadStoredMessages();

int option = 0;
while (option != 7) {
System.out.println("\n===== Stored Messages =====");
System.out.println("1) Display the sender and recipient of all stored messages");

System.out.println("2) Display the longest stored message");
System.out.println("3) Search for a message ID");
System.out.println("4) Search for all messages for a recipient");
System.out.println("5) Delete a message using the message hash");
System.out.println("6) Display a report of all stored messages");
System.out.println("7) Back to the main menu");
option = readNumber("Enter your choice: ");

switch (option) {
case 1:
  System.out.println(Message.displaySenderAndRecipient());
   break;

case 2:
  System.out.println("Longest stored message: " + Message.longestStoredMessage());
break;

case 3:
  System.out.print("Enter the message ID: ");
  String id = input.nextLine();
  System.out.println(Message.displayMessageByID(id));
  break;
  
case 4:
System.out.println("Enter the recipient cell number: ");
String recipient = input.nextLine();
List<String> results = Message.searchByRecipient(recipient);
if (results.isEmpty()) {
  System.out.println("No messages found for " + recipient);
} else {
   for (String text : results) {
     System.out.println("\"" + text + "\"");
}
}
break;

case 5:
   System.out.print("Enter the message hash: ");
   String hash = input.nextLine();
   System.out.println(Message.deleteByMessageHash(hash));
   break;
   
case 6:
  System.out.println(Message.displayStoredReport());
break;

case 7:
  break;

default:
  System.out.println("Invalid option, please try again.");
} 
}
}

//Keeps asking until the user types a whole number, so typing letters
//does not crash the application
static int readNumber(String prompt) {
  while (true) {
    System.out.print(prompt);
    try {
          return Integer.parseInt(input.nextLine().trim());
        } catch (NumberFormatException e) {
     System.out.println("Please enter a valid number.");
}
}
}


}
    

