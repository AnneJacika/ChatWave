
package hellofx;

import java.io.*;
import java.net.*;
import java.sql.*;
import javafx.application.Platform;
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    public String clientId; // Using client ID for the database
    private String clientName;
    private String message;
    private Server server;
    Main main;

    public ClientHandler(Socket clientSocket, Server server,String clientId) throws IOException {
        this.clientSocket = clientSocket;
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.clientId = clientId;
        System.out.println("Trying to create client handler: "+clientId);
        this.server=server;
        this.main=Server.getMainInstance(clientId);

        Server.addClient(this); // Ensure this happens when the client connects


    }

    public static String fetchClientName(String clientId) {
        String query = "SELECT username FROM users WHERE id = ?";
        try (Connection connection = Server.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, clientId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("username"); // Assuming 'name' is the column for the client's name
            } else {
                return null; // If no user found
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null; // Return null if an error occurs
        }
    }


    public static String fetchClientId(String clientName){
        String insertQuery = "SELECT id FROM users where username= ? ";
        try (Connection connection = Server.connect();PreparedStatement stmnt = connection.prepareStatement(insertQuery)) {
            stmnt.setString(1,clientName);
            ResultSet rSet = stmnt.executeQuery();
            if (rSet.next()) {
                return rSet.getString("id");
            } else {
                System.out.println("User not found for username: " +clientName);
                return null;
            }
        }catch (SQLException e) {
            e.printStackTrace();
            return null; // Return null if an error occurs
        }

    }

    @Override


    public synchronized void run() {
        try {
            System.out.println("ClientHandler starting...");
            clientName = fetchClientName(clientId);
            System.out.println(clientId+"  "+clientName);
            if (clientName == null) {
                out.println("Invalid client ID. Closing connection.");
                clientSocket.close();
                return;
            }
            Main main=Server.getMainInstance(clientId);
            System.out.println("Main for id"+clientId+main);
            // Setup for chatting
            System.out.println("Waiting to chat in cH");
            String recipientId = main.receiverId;
            String recipientName = fetchClientName(recipientId);
            if (!main.broadcast && recipientName == null) {
                out.println("Invalid client ID. Closing connection.");
                clientSocket.close();
                return;
            }

            System.out.println("Connected to client: " + clientSocket.getInetAddress());
            while ((message = in.readLine()) != null) {
                System.out.println("Message received: " + message);
                if (main.broadcast) {
                    System.out.println("Broadcasting message...");
                    String[] parts = message.split(": ", 2);
                    if (parts.length >= 2) {
                        String messageContent = parts[1];
                        Server.broadcastToContacts(messageContent, this);
                    }
                } else {
                    String[] parts = message.split(": ", 3);
                    System.out.println("Before private message");
                    if (parts.length >= 3) {
                        String receiverId = parts[1];
                        String messageContent = parts[2];
                        System.out.println("Before private message");
                        Server.privateMessage(messageContent, receiverId, this);
                        Main mainInstance=Server.getMainInstance(clientId);
                        Platform.runLater(() -> mainInstance.showMyChats(mainInstance.gridPane, clientId, receiverId, true));
                    } else {
                        System.out.println("Invalid message format received.");
                    }
                }
            }
            
        } catch (IOException e) {
            System.out.println("Sending message");
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Sending message");
            }
            //Server.removeClient(this);
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }
}























