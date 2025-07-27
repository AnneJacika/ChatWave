
package hellofx;
       
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;

import hellofx.Main.GridPaneChild;
//import hellofx.Main.GridPaneChildData;
import hellofx.Main.GridPaneDetails;
import hellofx.Main.MainData;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class Server {
    private static Server instance;
    private ServerSocket serverSocket;
    private volatile boolean isRunning;
    public static Map<String, ClientHandler> clientHandlers = Collections.synchronizedMap(new HashMap<>());
    private static final String LOCK_FILE_PATH = "server.lock"; // Path for the lock file
    private static final ConcurrentHashMap<String, Main> userMainMap = new ConcurrentHashMap<>();
    // Assuming a map on the server to associate userId with their Socket
    private static final ConcurrentHashMap<Socket, String> userIdToSocket = new ConcurrentHashMap<>();

    public void handleNewClientConnection(Socket clientSocket) {
        try {
            // Create input stream to read from client
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Read the userId as the first message
            String userId = in.readLine();
            if (userId != null) {
                userIdToSocket.put(clientSocket,userId);
                System.out.println("User ID " + userId + " connected with socket: " + clientSocket);
            } else {
                System.out.println("Failed to receive user ID from client.");
            }

            // Continue handling other messages from the client in a separate thread if needed
            // ...
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Server() {
        isRunning = false;
    }

    public static synchronized Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }


    public synchronized void startServer() {
        System.out.println("Inside startServer method");

        if (isRunning) {
            System.out.println("Server is already running.");
            return;
        }

        if (isLockFileExists()) {
            System.out.println("Another instance of the server is already running (lock file found).");
            return;
        }

        // Create the lock file
        createLockFile();
        Runtime.getRuntime().addShutdownHook(new Thread(this::deleteLockFile));

        Thread serverThread = new Thread(() -> {
            try {
                System.out.println("Starting server...");
                setRunning(true);
                mainServerLoop();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Delete the lock file when the server stops
                deleteLockFile();
                setRunning(false);
            }
        });

        serverThread.setDaemon(true); // Allow the server thread to close when the application closes
        serverThread.start();
    }

    private void mainServerLoop() throws IOException {
        System.out.println("Before try: Server started...");
        try {
            serverSocket = new ServerSocket(12345);
            System.out.println("Server started, listening on port 12345.");

            while (isRunning) {
                
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connection attempt from: " + clientSocket.getInetAddress().getHostAddress());
                handleNewClientConnection(clientSocket);
                String clientId=userIdToSocket.get(clientSocket);
                System.out.println("Retrieved client Id is ?:"+clientId);
                if(clientId!=null){
                    MainRecreator recreator = new MainRecreator();
                    Main recreatedMain = recreator.recreateMainFromJson(clientId + ".json");

                    if (recreatedMain != null) {
                            System.out.println("Main instance recreated successfully for userId: " + clientId);
                            registerUserMain(clientId, recreatedMain);
            
                } else {
                            System.out.println("Failed to recreate Main instance for userId: " + clientId);
                        }

                    // Handle the client connection in a separate ClientHandler
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this,clientId);
                    new Thread(clientHandler).start();}
                System.out.println("Current clients in Server: " + clientHandlers.keySet());
                System.out.println("Current mains:"+userMainMap);
            }
        } catch (IOException e) {
            System.out.println("Server encountered an issue.");
            e.printStackTrace();
            deleteLockFile();
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                deleteLockFile();
                System.out.println("Server socket closed.");
            }
        }
    }


    public synchronized void registerUserMain(String userId, Main mainInstance) {
        userMainMap.put(userId, mainInstance);
        System.out.println("Main stored for "+userId);
    }

    public static synchronized Main getMainInstance(String userId) {
        return userMainMap.get(userId);
    }

    private synchronized void setRunning(boolean running) {
        isRunning = running;
        System.out.println("inside set");
    }

    public static void broadcastToContacts(String message, ClientHandler client) {
        Set<String> contacts = getContacts(client.getClientId());
        String senderId = client.getClientId();

        System.out.println("Contacts for client " + senderId + ": " + contacts);

        for (String recipientId : contacts) {
            // Check if the recipient is currently connected by using the map
            ClientHandler recipientHandler = clientHandlers.get(recipientId);
            if (senderId.equals(recipientId)) {
                System.out.println("Sender and recipient cannot be the same. Skipping...");
                continue; // Skip to the next recipient
            }

            if (recipientHandler != null) {
                recipientHandler.sendMessage(senderId+": " + recipientId + ": " + message);

            } else {
                // If recipient is offline, save the message in the database for later delivery
                String insertQuery = "INSERT INTO contacts(sender_id, receiver_id, message, status) VALUES (?, ?, ?, 'pending')";
                try (Connection connection = connect();
                     PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                    statement.setString(1, senderId);  // sender ID
                    statement.setString(2, recipientId);  // recipient ID
                    statement.setString(3, message);  // message content
                    int rowsAffected = statement.executeUpdate();
                    System.out.println("Inserted message for offline recipient " + recipientId + ": " + rowsAffected + " rows affected.");
                    boolean notificationSent=ManageUser.notifyPendingMessages();
                    //System.out.println("Notification: "+notificationSent);


                    if (notificationSent) {
                        String updateQuery = "UPDATE contacts SET status = 'sent' WHERE receiver_id = ?";
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                            updateStmt.setString(1, recipientId);
                            updateStmt.executeUpdate();
                            System.out.println("update");
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    public static Set<String> getContacts(String userId) {
        System.out.println("Retrieving contacts");
        Set<String> contacts = new HashSet<>();
        String query = "SELECT id FROM contactlist WHERE user_id = ?";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                contacts.add(resultSet.getString("id"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return contacts;
    }


    public static Connection connect() {
        String url = "jdbc:mysql://localhost:3306/mydemo"; // Replace with your DB URL
        String user = "root"; // Replace with your DB username
        String password = "abcdefg7"; // Replace with your DB password

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");  // Ensure the driver is loaded
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.out.println("Connection to the database failed.");
            e.printStackTrace();
            return null;
        }

    }


    // Method to insert a new contact if valid
    public static boolean addContactIfValid(String userId, String receiverId, String message) {

        if (message == null || message.trim().isEmpty()) {
            System.out.println("Message cannot be null or empty.");
            return false; // Return false or handle as necessary
        }

        String query = "SELECT * FROM users WHERE id = ?";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // User exists, add to the contacts table
                String insertQuery = "INSERT INTO contacts (sender_id, receiver_id, message, status) VALUES (?, ?, ?, 'sent')";
                try (Connection dbconnection = connect(); PreparedStatement insertStatement = dbconnection.prepareStatement(insertQuery)) {
                    insertStatement.setString(1, userId); // Add as sender_id
                    insertStatement.setString(2, receiverId); // Assuming this is the receiver ID
                    insertStatement.setString(3, message); // Insert the message
                    insertStatement.executeUpdate();
                    return true; // Contact added successfully
                }
            } else {
                return false; // User not found
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    // Send a private message from one client to another
    public static synchronized void privateMessage(String message, String recipientId, ClientHandler senderClient) {

        System.out.println("in server before private cond after insert");

        String senderId=senderClient.clientId;
        System.out.println("in else servers");
        senderClient.sendMessage("User with ID " + recipientId + " not found.");

        String insertQuery = "INSERT INTO contacts (sender_id, receiver_id, message, status) VALUES (?, ?, ?, 'pending')";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.setString(1, senderId);  // sender ID
            statement.setString(2, recipientId);  // recipient ID
            statement.setString(3, message);  // message content
            int rowsAffected = statement.executeUpdate();
            System.out.println("Inserted message for offline recipient " + recipientId + ": " + rowsAffected + " rows affected.");
            boolean notificationSent=ManageUser.notifyPendingMessages(recipientId);
            System.out.println("Notification: "+notificationSent);


            if (notificationSent) {
                String updateQuery = "UPDATE contacts SET status = 'sent' WHERE receiver_id = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                    updateStmt.setString(1, recipientId);
                    updateStmt.executeUpdate();
                    System.out.println("update");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public static boolean isValidClientId(String clientId) {
        String query = "SELECT * FROM contacts WHERE sender_id = ? ";
        try (Connection connection = connect();PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, clientId);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next(); // If there's at least one record, the ID is valid
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static synchronized void addClient(ClientHandler clientHandler) {
        String clientId = clientHandler.getClientId();
        if (clientHandlers.containsKey(clientId)) {
            System.out.println("Client already connected: " + clientId);
        } else {
            System.out.println("Adding client: " + clientId);
            clientHandlers.put(clientId, clientHandler);
        }
    }

    public static synchronized void removeClient(ClientHandler clientHandler) {
        String clientId = clientHandler.getClientId();
        if (clientHandlers.remove(clientId) != null) {
            System.out.println("Removed client: " + clientId);
        }
    }


    public static void main(String[] args) {
        Server server = Server.getInstance();
        server.startServer();
        System.out.println("Launching Server...");
    }


    // Lock file management
    private boolean isLockFileExists() {
        File lockFile = new File(LOCK_FILE_PATH);
        return lockFile.exists();
    }

    private void createLockFile() {
        try {
            new File(LOCK_FILE_PATH).createNewFile();
            System.out.println("Lock file created.");
        } catch (IOException e) {
            System.out.println("Failed to create lock file.");
            e.printStackTrace();
        }
    }

    private void deleteLockFile() {
        File lockFile = new File(LOCK_FILE_PATH);
        if (lockFile.delete()) {
            System.out.println("Lock file deleted.");
        } else {
            System.out.println("Failed to delete lock file.");
        }
    }
    public class MainRecreator {
        public Main recreateMainFromJson(String fileName) {
            System.out.println("recreating main");
            try (FileReader reader = new FileReader(fileName)) {
                Gson gson = new Gson();
                MainData mainData = gson.fromJson(reader, MainData.class);
    
                Main recreatedMain = new Main();
    
                // Set messageLabel text
                recreatedMain.messageLabel.setText(mainData.getMessageLabelText());
    
                // Recreate GridPane
                GridPane gridPane = new GridPane();
                GridPaneDetails gridData = mainData.getGridPaneDetails();
                for (GridPaneChild childData : gridData.getChildren()) {
                    Node node = null;
                    switch (childData.getType()) {
                        case "Label":
                            node = new Label(childData.getText());
                            break;
                        case "Button":
                            node = new Button(childData.getText());
                            break;
                        case "TextField":
                            node = new TextField(childData.getText());
                            break;
                    }
                    if (node != null) {
                        gridPane.add(node, childData.getCol(), childData.getRow());
                    }
                }
                recreatedMain.gridPane = gridPane;
    
                // Set other fields from mainData
                //recreatedMain.manageUser = mainData.getManageUser();
                recreatedMain.msg = mainData.getMsg();
                recreatedMain.receiverId = mainData.getReceiverId();
                recreatedMain.broadcast = mainData.isBroadcast();
                recreatedMain.flag = mainData.isFlag();
                recreatedMain.server = new Server();
                recreatedMain.gridPaneDetails = mainData.getGridPaneDetails();
                
                return recreatedMain;
    
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
    public static String fetchGroupName(String groupId){
        String query = "SELECT group_name FROM users WHERE group_id = ?";
            try (Connection connection = Server.connect();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, groupId);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getString("group_name"); // Assuming 'name' is the column for the client's name
                } else {
                    return null; // If no user found
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return null; // Return null if an error occurs
            }
        
    }
    
}


















       





       

















       






       

















       



