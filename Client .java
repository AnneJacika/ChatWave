package hellofx;

import java.io.*;
import java.net.*;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.sql.*;

public class Client {
    private static final ConcurrentHashMap<Socket, String> clientSockets = new ConcurrentHashMap<>();
    public ManageUser manageUser;
    public Main main;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    public String userId; // Unique identifier for the client
    private Connection dbConnection;

    public Client(String serverAddress, int port, String userId) throws IOException {
        this.userId = userId;
        initializeSocket(serverAddress, port);
        initializeDatabaseConnection();

        // Start listening for incoming messages
        new Thread(new IncomingMessageHandler()).start();
    }

    private void initializeSocket(String serverAddress, int port) throws IOException {
        try {
            socket = new Socket(serverAddress, port);
            System.out.println("Connected to the server. Socket ID: " + socket + ", User ID: " + userId);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Send the userId to the server as the first message
            out.println(userId);

            // Store the socket in the clientSockets map if needed (for client-side use)
            clientSockets.put(socket, userId);
        } catch (ConnectException e) {
            System.out.println("Error: Unable to connect to server. Please check server status.");
            e.printStackTrace();
        }
    }

    private void initializeDatabaseConnection() {
        try {
            String url = "jdbc:mysql://localhost:3306/mydemo";
            String username = "root";
            String password = "abcdefg7";
            dbConnection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String retrieveUserIdFromClient(Socket socket) {
        System.out.println("Retrieve Socket :"+socket);
        String uniqueId = clientSockets.get(socket);
        return (uniqueId != null) ? clientSockets.get(socket) : null;
    }

    public void sendMessage(String receiverId, String message) {
        if (out != null) {
            out.println(userId + ": " + receiverId + ": " + message);
            System.out.println("Message sent from client: " + message);
        }
    }

    void displayImage(String senderId, byte[] imageBytes, VBox chatBox) {

        Image image = new Image(new ByteArrayInputStream(imageBytes));

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);

        // Create an HBox to hold the ImageView
        HBox imageBox = new HBox();
        imageBox.setSpacing(10); // Spacing between messages
        imageBox.getChildren().add(imageView);
        if (senderId.equals(userId)) {
            imageBox.setAlignment(Pos.CENTER_RIGHT);
        } else {
            imageBox.setAlignment(Pos.CENTER_LEFT);
        }
        Platform.runLater(() -> {
            // Add the image box to the chatBox (VBox)
            chatBox.getChildren().add(imageBox);

            // Force layout refresh
            chatBox.layout();

        });
    }
    
    public void sendImage(File imageFile, int receiverUserId, VBox chatBox) {
        System.out.println(manageUser);
        byte[] imageBytes = null;
        try (FileInputStream fis = new FileInputStream(imageFile)) {
            imageBytes = new byte[(int) imageFile.length()];
            fis.read(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String imagePath = uploadImageToDb(imageBytes);

        String query = "INSERT INTO contacts (sender_id, receiver_id, message, image_path) VALUES (?, ?, ?, ?)";
        try (Connection connection = Server.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, Integer.parseInt(userId));
            statement.setInt(2, receiverUserId);
            statement.setString(3, "");
            statement.setString(4, imagePath);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        displayImage(userId, imageBytes, chatBox);
    }

    

    public String uploadImageToDb(byte[] imageBytes) {
        String directoryPath = "uploaded_images";
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdir();
        }

        String uniqueFileName = "img_" + System.currentTimeMillis() + ".png";
        String filePath = directoryPath + File.separator + uniqueFileName;

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(imageBytes);
            fos.flush();
            System.out.println("Image saved at: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return filePath;
    }
    public void sendBroadcastMessage(String message) {
        if (out != null) {
            out.println(userId + ": " + message);
        }
    }

    public void sendMessageToGroup(String groupId, String message, String senderId, List<String> groupMembers) {
        if (out != null) {
            out.println("GROUP:" + groupId + ":" + senderId + ":" + message);
            storeMessageInDatabase(groupId, message, senderId);
        }
    }

    private void storeMessageInDatabase(String groupId, String message, String senderId) {
        String sql = "INSERT INTO group_messages (group_id, message, user_id) VALUES (?, ?, ?)";
        try (PreparedStatement statement = dbConnection.prepareStatement(sql)) {
            statement.setString(1, groupId);
            statement.setString(2, message);
            statement.setString(3, senderId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private class IncomingMessageHandler implements Runnable {
        @Override
        public synchronized void run() {
            String message;
            try {

                while ((message = in.readLine()) != null) {
                    String[] parts = message.split(":", 4);
                    if (parts.length == 4 && parts[0].equals("GROUP")) {
                        String groupId = parts[1];
                        String senderId = parts[2];
                        String groupName=Server.fetchGroupName(groupId);
                        Main mainInstance=Server.getMainInstance(senderId);
                        Platform.runLater(() -> mainInstance.showGroupChat(mainInstance.gridPane, senderId,groupName, groupId, true));
                    } else if (parts.length >= 3) {
                        String senderId = parts[0];
                        String receiverId = parts[1];
                        Main mainInstance=Server.getMainInstance(senderId);
                        Platform.runLater(() -> mainInstance.showMyChats(mainInstance.gridPane, senderId, receiverId, true));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeConnection() throws IOException {
        if (socket != null) {
            socket.close();
        }
    }

    public String getClientId() {
        return userId;
    }
}










