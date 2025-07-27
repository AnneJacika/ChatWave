# ChatWave

ChatWave is a real-time chatting application built using Java, JavaFX, and HTML/CSS for the UI. 
It allows users to communicate seamlessly by sending text messages and images, either in one-to-one chats or through group conversations. The application is backed by a MySQL database for secure and efficient storage of user data, messages, and group information.

# Features
User Authentication – Secure login with credentials

Private and Group Chat – Send text and images to individuals or groups

Broadcast Messaging – Send messages to all contacts at once

Chat History – View and load previous conversations

Contact Management – Add, delete, or block contacts

Image Sharing – Send and delete images in chats

Group Management – Create groups and chat with multiple users

Real-Time Communication – Instant messaging via TCP sockets

Robust Server Handling – Multi-client server with session and message management

Error Handling – Graceful feedback on connection, message, and authentication failures

Responsive UI – Clean, styled JavaFX interface using HTML/CSS

MySQL Integration – Stores messages, user data, and group metadata

# Project Scope
1.  Functional Scope
   
- Client-Side Features
  - User Authentication: Secure login system using username and password, with appropriate error handling.
  - Private Chat: One-to-one messaging with contacts.
  - Group Chat: Create and participate in group conversations with multiple users.
  - Broadcast Messages: Send a message to all contacts at once.
  - Add Contacts: Add other users to your contact list (only if they exist in the system).
  - Contact List Management: View, update, and manage contact lists in real time.
  - Chat History: Retrieve and display past conversations on selecting a contact.
  - Delete Messages: Support for deleting messages for self or all participants.
  - Block Users: Ability to block contacts to avoid communication.
  - Delete Contacts: Remove users from the contact list.
  - Send Images: Share images in both private and group chats, with delete functionality.
  - Responsive UI: Built using JavaFX for a clean and dynamic user experience.
  - Error Handling: Informative messages for login failures, message delivery issues, or connection problems.

- Server-Side Features
  - Multi-Client Handling: Handles multiple users concurrently using multithreading.
  - Message Routing: Routes messages to intended recipients based on socket connections.
  - User Management: Maintains connected users, active sessions, and authentication handling.
  - Message Persistence: Stores messages in a database for chat history access.
  - Connection Management: Manages online/offline status updates and disconnections.
  - Error Handling: Detects and handles issues like message delivery failures or broken connections.

- Database Features
  - User Info Storage: MySQL tables to store username, password, and email.
  - Chat History Storage: Messages saved to the database for future reference.
  - Contact List Management: Store and retrieve user contact lists.
  - Group Data Management: Save group metadata like name, members, and message logs.

2. Technical Scope
  - Programming Language: Java

  - User Interface: JavaFX for desktop-based responsive UI

  - Networking: TCP/IP sockets used for client-server communication
  
  - Database: MySQL used for persistent storage of users, contacts, messages, and groups

  - Multithreading: Server uses Java multithreading to manage simultaneous client connections

  - UI Styling: JavaFX with integrated HTML and CSS for a polished frontend appearance


# Tech Stack

  - Programming Language	:  Java (JDK 8 or above)

  - User Interface  :	JavaFX + HTML/CSS

  - Networking  :	Java Socket Programming (TCP/IP)

  - Database  :	MySQL

  - Multithreading  :	Java Thread

    
# Installation

## Prerequisites
   
  - Java Development Kit (JDK) – Version 8 or above

  - MySQL Server – Running locally or on your server

  - JavaFX SDK – Add to your classpath (if not using JDK with JavaFX bundled)

  - An IDE like IntelliJ IDEA / Eclipse / VSCode (recommended)

## Setup
To run this project locally on your machine, follow these steps:

 **Download the Project Folder**: Obtain the project folder from the project source.
 
 **Open with any IDE**: Launch IDE, then open the downloaded project.
 
 **Set Up the MySQL Database**:
   - Ensure you have MySQL installed.
   - Create a database named `mydemo` and set up the necessary tables using the commands below:

``` sql
     -- Create the database
     CREATE DATABASE mydemo;
     USE mydemo;

     -- Create the Users table
     CREATE TABLE Users (
         id INT PRIMARY KEY AUTO_INCREMENT,
         username VARCHAR(255) NOT NULL,
         password VARCHAR(255) NOT NULL,
         email VARCHAR(255) NOT NULL
     );

     -- Create the User_Groups table
     CREATE TABLE User_Groups (
         group_id INT PRIMARY KEY AUTO_INCREMENT,
         group_name VARCHAR(255) NOT NULL,
         group_members TEXT NOT NULL
     );

     -- Create the Contacts table
     CREATE TABLE Contacts (
         id INT PRIMARY KEY AUTO_INCREMENT,
         sender_id INT,
         receiver_id INT,
         message VARCHAR(255) NOT NULL,
         timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
         status VARCHAR(20) DEFAULT 'sent',
         deleted_by_sender TINYINT(1) DEFAULT 0,
         deleted_by_receiver TINYINT(1) DEFAULT 0,
         image_path VARCHAR(255),
         file_path VARCHAR(255),
         FOREIGN KEY (sender_id) REFERENCES Users(id),
         FOREIGN KEY (receiver_id) REFERENCES Users(id)
     );

     -- Create the ContactList table
     CREATE TABLE ContactList (
         id INT,
         name VARCHAR(100),
         user_id INT,
         FOREIGN KEY (user_id) REFERENCES Users(id)
     );

     -- Create the Group_Messages table
     CREATE TABLE Group_Messages (
         message_id INT PRIMARY KEY AUTO_INCREMENT,
         group_id INT,
         user_id INT,
         message TEXT NOT NULL,
         timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
         FOREIGN KEY (group_id) REFERENCES User_Groups(group_id),
         FOREIGN KEY (user_id) REFERENCES Users(id)
     );

     -- Create the Blocked_Users table
     CREATE TABLE Blocked_Users (
         blocker_id INT NOT NULL,
         blocked_id INT NOT NULL,
         PRIMARY KEY (blocker_id, blocked_id),
         FOREIGN KEY (blocker_id) REFERENCES Users(id),
         FOREIGN KEY (blocked_id) REFERENCES Users(id)
     );
 ```


- In the code, set your MySQL password in the database connection configuration.Update it as follows:

     ```java
     String url = "jdbc:mysql://localhost:3306/mydemo";
     String username = "your_mysql_username";
     String password = "your_mysql_password"; // Replace with your MySQL password
     ```


Compile and Run the Project
   
  Start the Server

        javac Server.java
        java Server
   
Make sure the server is running before starting clients.

  Start the Client
  
        javac Main.java
        java Main

# How to Use the Application

1. **Log In or Sign In**: Enter a username and password to log in. New users can sign in by clicking on the "Signin" button.
2. **Start a Chat**:
   - After logging in, select an user from your contacts to start a chat.
   - You can add contacts from the database using "Add contact" button.
   - You can create groups with your contacts and chat in groups.
   - Type your message in the chat input box and press "Send" to message the selected user.
   - Send images by clicking "Upload Image" button.
   - You can block and unblock users,delete messages for everyone and yourself.
3. **Multiple Sessions**: You can open multiple instances of the application to simulate different users.
4. **End the Chat**: Close the chat window or log out to end your session.

# Demo
Here is a quick look into some of ChatWave's prominent features!

![Demo](chatwaveDemo.gif)
# Authors
K Anishka , *SSN College of Engineering , Tamil nadu , India*

Anne Jacika J , *SSN College of Engineering , Tamil nadu , India*

Anushya V , *SSN College of Engineering , Tamil nadu , India*

# License

© 2025 K Anishka, Anne Jacika J, Anushya V. All rights reserved.

This project is proprietary and may not be copied, modified, or distributed without permission.

