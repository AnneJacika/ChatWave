
package hellofx;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Modality;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import java.util.*;

public class Main extends Application {
    public transient Label messageLabel = new Label();
    public ManageUser manageUser;
    public String msg;
    public GridPane gridPane;//=new GridPane();
    public transient Client client;
    boolean flag=false;
    public String receiverId = "4";
    public boolean broadcast;
    public transient Server server;
    public GridPaneDetails gridPaneDetails;

    public Main(Label messageLabel, GridPane gridPane, boolean flag, String receiverId,
                boolean broadcast, MenuButton optionsButton, ManageUser manageUser) {
        this.messageLabel = messageLabel != null ? messageLabel : null;
        this.flag = flag;
        this.receiverId = receiverId;
        this.broadcast = broadcast;
        this.manageUser = manageUser;
        this.gridPaneDetails = new GridPaneDetails(gridPane);
    }
    public class MainData {
        private String messageLabelText;
        //private GridPaneData gridPaneData;
        //private ManageUser manageUser;
        private String msg;
        private String receiverId;
        private boolean broadcast;
        private boolean flag;
        private GridPaneDetails gridPaneDetails;
    
        // Getters and setters for each field
        public String getMessageLabelText() { return messageLabelText; }
        public void setMessageLabelText(String messageLabelText) { this.messageLabelText = messageLabelText; }
        /*public GridPaneData getGridPaneData() { return gridPaneData; }
        public void setGridPaneData(GridPaneData gridPaneData) { this.gridPaneData = gridPaneData; }
        public ManageUser getManageUser() { return manageUser; }
        public void setManageUser(ManageUser manageUser) { this.manageUser = manageUser; }*/
        public String getMsg() { return msg; }
        public void setMsg(String msg) { this.msg = msg; }
        public String getReceiverId() { return receiverId; }
        public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
        public boolean isBroadcast() { return broadcast; }
        public void setBroadcast(boolean broadcast) { this.broadcast = broadcast; }
        public boolean isFlag() { return flag; }
        public void setFlag(boolean flag) { this.flag = flag; }
        public GridPaneDetails getGridPaneDetails() { return gridPaneDetails; }
        public void setGridPaneDetails(GridPaneDetails gridPaneDetails) { this.gridPaneDetails = gridPaneDetails; }
       
    }


public void saveToJson() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    // Create and populate a MainData instance
    MainData mainData = new MainData();
    mainData.setMessageLabelText(messageLabel.getText());
    //mainData.setManageUser(manageUser);
    mainData.setMsg(msg);
    mainData.setReceiverId(receiverId);
    mainData.setBroadcast(broadcast);
    mainData.setFlag(flag);
    
    // Set GridPaneDetails instead of directly using the GridPane
    mainData.setGridPaneDetails(new GridPaneDetails(gridPane));

    // Serialize the MainData instance to JSON
    String json = gson.toJson(mainData);

    String fileName = (manageUser != null && manageUser.uno != null) ? manageUser.uno + ".json" : "default.json";

    try (FileWriter writer = new FileWriter(fileName)) {
        writer.write(json);
        System.out.println("JSON file created: " + fileName);
    } catch (IOException e) {
        e.printStackTrace();
    }
}



    // Helper class for GridPane details
    class GridPaneDetails {
        int rows;
        int columns;
        String padding;
        String alignment;
        List<GridPaneChild> children;

        public GridPaneDetails(GridPane gridPane) {
            this.rows = 3; // Set rows manually or calculate dynamically if needed
            this.columns = 3; // Set columns manually or calculate dynamically if needed
            this.padding = gridPane.getPadding() != null ? String.valueOf(gridPane.getPadding().getTop()) : "0";
            this.alignment = gridPane.getAlignment() != null ? gridPane.getAlignment().toString() : "CENTER";
            this.children = new ArrayList<>();

            for (Node node : gridPane.getChildren()) {
                int row = GridPane.getRowIndex(node) != null ? GridPane.getRowIndex(node) : 0;
                int col = GridPane.getColumnIndex(node) != null ? GridPane.getColumnIndex(node) : 0;
                if (node instanceof Label) {
                    children.add(new GridPaneChild("Label", ((Label) node).getText(), row, col));
                } else if (node instanceof Button) {
                    children.add(new GridPaneChild("Button", ((Button) node).getText(), row, col));
                } else if (node instanceof TextField) {
                    children.add(new GridPaneChild("TextField", null, row, col));
                }
            }

        }
        public String getPadding() {
            return padding;
        }
        public int getRows() {
            return rows;
        }
    
        public int getColumns() {
            return columns;
        }
        
        public String getAlignment() {
            return alignment;
        }
        public List<GridPaneChild> getChildren() {
            return children;
        }
    }

    // Helper class for GridPane child elements
    class GridPaneChild {
        String type;
        String text;
        int row;
        int col;

        public GridPaneChild(String type, String text, int row, int col) {
            this.type = type;
            this.text = text;
            this.row = row;
            this.col = col;
        }
        
    public int getCol(){
        return col;
    }
    public int getRow(){
        return row;
    }
    public String getType(){
        return type;
    }
    public String getText(){
        return text;
    }

    }

    public Main() {
        // Set properties for messageLabel
        messageLabel.setPrefWidth(600); // Set a preferred width
        messageLabel.setWrapText(true);  // Allow text to wrap
    }
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("User Management");
        // Create grid layout
        this.gridPane = new GridPane();
        Scene scene = new Scene(gridPane, 500, 600);
        //this.gridPane = new GridPane();
        // Apply CSS styles to the scene
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setScene(scene);
        // Initial UI components
        showLoginOrSignup(gridPane,flag);
        primaryStage.show();
    }

    private void showLoginOrSignup(GridPane gridPane,boolean flag) {
        gridPane.getChildren().clear(); // Clear previous UI elements
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10); // Horizontal gap
        gridPane.setVgap(10);


        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("button"); // Apply button style
        Button signupButton = new Button("Sign Up");
        signupButton.getStyleClass().add("button"); // Apply button style

        GridPane.setHalignment(loginButton, HPos.CENTER);
        GridPane.setHalignment(signupButton, HPos.CENTER);


        gridPane.add(loginButton, 2, 6);
        gridPane.add(signupButton, 2, 8);

        // Handle login action
        loginButton.setOnAction(e -> {
            Platform.runLater(() -> {
                loginOption(gridPane,flag);
            });
        });
        signupButton.setOnAction(e -> {
            Platform.runLater(() -> {
                this.signinOption(gridPane,flag);
            });
        });
    }



    void loginOption(GridPane gridPane,boolean flag) {
        gridPane.getChildren().clear(); // Clear previous UI elements
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10); // Horizontal gap
        gridPane.setVgap(10);
        //saveToJson();

        // Input fields for username and password
        TextField usernameField = new TextField();
        usernameField.getStyleClass().add("text-field"); // Apply text field style

        PasswordField passwordField = new PasswordField();
        passwordField.getStyleClass().add("password-field"); // Apply password field style

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("button"); // Apply button style
        Button forgotPasswordButton = new Button("Forgot Password");
        forgotPasswordButton.getStyleClass().add("button");
        Button backButton = new Button("Back");
        backButton.getStyleClass().add("button");

        GridPane.setHalignment(usernameField, HPos.CENTER);
        GridPane.setHalignment(passwordField, HPos.CENTER);
        GridPane.setHalignment(loginButton, HPos.CENTER);
        GridPane.setHalignment(messageLabel, HPos.CENTER);
        GridPane.setHalignment(forgotPasswordButton, HPos.CENTER);

        // Add components to the grid
        gridPane.add(new Label("Username:"), 2, 2);
        gridPane.add(usernameField, 3, 2);
        gridPane.add(new Label("Password:"), 2, 3);
        gridPane.add(passwordField, 3, 3);
        gridPane.add(loginButton, 4, 6);
        gridPane.add(backButton, 0, 20);
        gridPane.add(messageLabel, 1, 12, 3, 1); // Adjust column span if needed
        manageUser = new ManageUser(messageLabel); // Create instance of ManageUser
        //gridPane.add(forgotPasswordButton,4,7);
        
        // Handle login action
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (manageUser.login(username, password)) {
                this.showUserOptions(gridPane,flag); // Show options after successful login
                this.server = Server.getInstance();
                server.startServer();
                System.out.println("Login succes : "+manageUser.uno);
                if(manageUser.uno!=null){
                    saveToJson();
                    System.out.println("Saving file");
                }

            }



        });
        forgotPasswordButton.setOnAction(e->{
            //String email = emailField.getText().trim();
            String username = usernameField.getText();
            Main mainInstance = new Main();
            manageUser.handleForgotPassword(gridPane,mainInstance, username);
        });
        backButton.setOnAction(e->{
            System.out.println("Back button pressed");
            showLoginOrSignup(gridPane,true);
        });
    }
    void signinOption(GridPane gridpane,boolean flag){
        gridPane.getChildren().clear(); // Clear previous UI elements
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10); // Horizontal gap
        gridPane.setVgap(10);

        // Input fields for username and password
        TextField usernameField = new TextField();
        usernameField.getStyleClass().add("text-field"); // Apply text field style

        PasswordField passwordField = new PasswordField();
        passwordField.getStyleClass().add("password-field"); // Apply password field style

        TextField emailField = new TextField();
        emailField.getStyleClass().add("text-field"); // Apply text field style

        Button signupButton = new Button("Sign Up");
        signupButton.getStyleClass().add("button"); // Apply button style

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("button");

        GridPane.setHalignment(usernameField, HPos.CENTER);
        GridPane.setHalignment(passwordField, HPos.CENTER);
        GridPane.setHalignment(signupButton, HPos.CENTER);
        GridPane.setHalignment(messageLabel, HPos.CENTER);

        gridPane.add(new Label("Username:"), 2, 2);
        gridPane.add(usernameField, 3, 2);
        gridPane.add(new Label("Password:"), 2, 3);
        gridPane.add(passwordField, 3, 3);
        gridPane.add(new Label("Email:"), 2, 4);
        gridPane.add(emailField, 3, 4);
        gridPane.add(signupButton, 4, 6);
        gridPane.add(messageLabel, 1, 12, 3, 1); // Adjust column span if needed
        gridPane.add(backButton, 0, 20);
        manageUser = new ManageUser(messageLabel);


        // Handle signup action
        signupButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String email=emailField.getText();
            manageUser.createUser(username, password,email);
            if (manageUser.login(username, password)) {
                showUserOptions(gridPane,flag); // Show options after successful login
                this.server = Server.getInstance();
                server.startServer();
                if(manageUser.uno!=null){
                    saveToJson();
                }

            }
        });
        backButton.setOnAction(e->{
            System.out.println("Back button pressed");
            showLoginOrSignup(gridPane,true);
        });
    }

    void showUserOptions(GridPane gridPane,boolean flag) {
        gridPane.getChildren().clear(); // Clear previous UI elements
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10); // Horizontal gap
        gridPane.setVgap(10); // Vertical gap

        // Show logged in options
        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("button");

        Button resetPasswordButton = new Button("Reset Password");
        resetPasswordButton.getStyleClass().add("button");
        Button userChatsButton = new Button("My Chats");
        resetPasswordButton.getStyleClass().add("button");
        Button addContactButton = new Button("Add Contact");// ADD

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("button");

        GridPane.setHalignment(messageLabel, HPos.CENTER);
        GridPane.setHalignment(logoutButton, HPos.CENTER);
        GridPane.setHalignment(resetPasswordButton, HPos.CENTER);
        GridPane.setHalignment(userChatsButton, HPos.CENTER);
        GridPane.setHalignment(addContactButton, HPos.CENTER);

        // Add components to the grid
        gridPane.add(messageLabel, 1, 2, 2, 1); // Spanning across columns
        gridPane.add(logoutButton, 2, 4);
        gridPane.add(resetPasswordButton, 2, 5);
        gridPane.add(userChatsButton, 2, 6);
        gridPane.add(addContactButton, 2, 7); // ADD
        gridPane.add(backButton, 0, 20);

        // Handle logout action
        logoutButton.setOnAction(e -> {
            if (manageUser.logout(manageUser.uname)) {
                messageLabel.setText("You have successfully logged out.");
                this.showLoginOrSignup(gridPane,flag); // Show login/sign-up options again
            }
        });

        addContactButton.setOnAction(e -> {
            showAddContactOptions(gridPane,flag); // ADD this fully
        });
        //String receipentId="1";

        // Handle reset password action
        resetPasswordButton.setOnAction(e -> showResetPasswordOptions(gridPane,flag));
        //userChatsButton.setOnAction(e -> showMyChats(gridPane,manageUser.uno,receipentId));
        userChatsButton.setOnAction(e -> fetchUsers(flag));

        backButton.setOnAction(e->{
            System.out.println("Back button pressed");
            loginOption(gridPane,true);
        });
    }
    private void showResetPasswordOptions(GridPane gridPane,boolean flag) {
        gridPane.getChildren().clear(); // Clear previous UI elements
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10); // Horizontal gap
        gridPane.setVgap(10); // Vertical gap
        // Input fields for username, old password, and new password
        TextField usernameField = new TextField();
        usernameField.getStyleClass().add("text-field");
        PasswordField oldPasswordField = new PasswordField();
        oldPasswordField.getStyleClass().add("password-field");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.getStyleClass().add("password-field");
        Button resetButton = new Button("Reset Password");
        resetButton.getStyleClass().add("button");
        Button backButton = new Button("Back");
        backButton.getStyleClass().add("button");
        gridPane.add(backButton, 0, 20);
        // Center the components in their respective cells
        GridPane.setHalignment(usernameField, HPos.CENTER);
        GridPane.setHalignment(oldPasswordField, HPos.CENTER);
        GridPane.setHalignment(newPasswordField, HPos.CENTER);
        GridPane.setHalignment(resetButton, HPos.CENTER);
        // Add components to the grid
        gridPane.add(new Label("Username:"), 2, 2);
        gridPane.add(usernameField, 3, 2);
        gridPane.add(new Label("Old Password:"), 2, 3);
        gridPane.add(oldPasswordField, 3, 3);
        gridPane.add(new Label("New Password:"), 2, 4);
        gridPane.add(newPasswordField, 3, 4);
        gridPane.add(resetButton, 3, 6);

        // Handle reset password confirmation
        resetButton.setOnAction(e -> {
            String username = usernameField.getText();
            String oldPassword = oldPasswordField.getText();
            String newPassword = newPasswordField.getText();
            manageUser.resetPswd(username, oldPassword, newPassword);
            this.showUserOptions(gridPane,flag); // Show user options again after reset
        });
        backButton.setOnAction(e->{
            System.out.println("Back button pressed");
            showUserOptions(gridPane,true);
        });
    }
    private void fetchUsers(boolean flag){
        this.gridPane.getChildren().clear(); // Clear previous UI elements
        // Add a new button to fetch and display users
        Button fetchUsersButton = new Button("Contacts");
        fetchUsersButton.getStyleClass().add("button");
        gridPane.add(fetchUsersButton, 2, 7);
        Button CreateGroupButton = new Button("Create Group");
        CreateGroupButton.getStyleClass().add("button");
        gridPane.add(CreateGroupButton, 2, 8);
        Button broadcastButton = new Button("Broadcast message");
        fetchUsersButton.getStyleClass().add("button");
        gridPane.add(broadcastButton, 2, 9);
        Button backButton = new Button("Back");
        backButton.getStyleClass().add("button");
        gridPane.add(backButton, 0, 20);
        fetchUsersButton.setOnAction(e -> {

            // Fetch users from database
            try (Connection connection = Server.connect();
                 PreparedStatement statement = connection.prepareStatement("SELECT name FROM contactlist WHERE user_id = ?")) {

                statement.setString(1, manageUser .uno); // Use manageUser .uno to filter contacts
                ResultSet resultSet = statement.executeQuery();

                // Create a list to store the user names
                ObservableList<String> users = FXCollections.observableArrayList();

                // Iterate through the result set and add users to the list
                while (resultSet.next()) {
                    users.add(resultSet.getString("name")); // Add contact name to the list
                }
                // Display the users in a list
                displayUsers(gridPane, users,flag);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        CreateGroupButton.setOnAction(e->{
            manageUser.createGroup();
        });
        broadcastButton.setOnAction(e->{
            broadcast(ClientHandler.fetchClientId(manageUser.uname),flag);

        });
        backButton.setOnAction(e->{
            System.out.println("Back button pressed");
            showUserOptions(gridPane,true);
        });
    }
    public synchronized void broadcast(String userId,boolean flag) {
        this.gridPane.getChildren().clear(); // Clear previous UI elements
        this.gridPane.setAlignment(Pos.TOP_CENTER);
        this.gridPane.setHgap(10); // Horizontal gap
        this.gridPane.setVgap(10); // Vertical gap

        // Initialize client if not already initialized
        if (!flag) {
            try {
                client = new Client("localhost", 12345, userId);
                System.out.println("Client initialized broadcast: " + client);
                flag = true;
                server.registerUserMain(userId, this);
            } catch (IOException e) {
                e.printStackTrace();
                return; // Early return on failure
            }
        }

        broadcast = true;
        saveToJson();

        // Create message input field and buttons
        TextField messageField2 = new TextField();
        messageField2.setPromptText("Enter your Broadcast message...");
        Button sendButton = new Button("Send");
        Button backButton = new Button("Back");

        HBox messageBox = new HBox(10); // Set spacing between elements
        messageBox.setPadding(new Insets(5)); // Padding around the message
        messageBox.getChildren().addAll(messageField2, sendButton, backButton);
        messageBox.setAlignment(Pos.BOTTOM_CENTER); // Align at the bottom center
        gridPane.add(messageBox, 0, 2, 2, 1); // Add message box to the GridPane

        // Send button action
        sendButton.setOnAction(e -> {

            String msg = messageField2.getText().trim();
            //System.out.println(broadcast);
            if (!msg.isEmpty()) {
                new Thread(() -> {
                    try {
                        client.sendBroadcastMessage(msg); // Send the broadcast message
                        Platform.runLater(() -> messageField2.clear()); // Clear the field after sending
                        System.out.println("Message sent: " + msg);
                    } catch (Exception ex) {
                        System.out.println("Error sending message: " + ex.getMessage());
                    }
                }).start(); // Start a new thread for sending messages
            } else {
                System.out.println("Message cannot be empty.");
            }
        });

        // Back button action
        backButton.setOnAction(e -> {
            broadcast=false;// Ensure that the UI is in a stable state before switching views
            Platform.runLater(() -> {
                showUserOptions(gridPane,true);
            });
        });

    }

    private synchronized void showMessageBox(String message) {
        // Create a new stage for the message box
        Stage messageBoxStage = new Stage();
        messageBoxStage.initModality(Modality.APPLICATION_MODAL); // Make it modal
        messageBoxStage.setTitle("Information");

        // Create a label to display the message
        Label messageLabel = new Label(message);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(messageLabel);

        Scene scene = new Scene(layout, 300, 150);
        messageBoxStage.setScene(scene);
        messageBoxStage.showAndWait(); // Show the message box and wait for it to be closed
    }

    private synchronized void displayMsg(String msg, TextField messageField, VBox chatBox, ScrollPane scrollPane) {
        if (!msg.trim().isEmpty()) {
            // Clear the message field after sending
            messageField.clear();

            // Display the sent message in the chatBox immediately
            HBox newMessageBox = new HBox();
            Label newMessageLabel = new Label(msg + " [" + new SimpleDateFormat("HH:mm").format(new Date()) + "]");
            newMessageLabel.setStyle("-fx-background-color: lightblue; -fx-padding: 10; -fx-border-radius: 10; -fx-background-radius: 10;");
            newMessageLabel.setWrapText(true);

            // Align the sent message to the right
            newMessageBox.getChildren().add(newMessageLabel);
            newMessageBox.setAlignment(Pos.BOTTOM_RIGHT);
            chatBox.getChildren().add(newMessageBox);

            // Scroll to the bottom to show the latest message
            scrollPane.setVvalue(1.0);
        }
    }
    public synchronized void showMyChats(GridPane gridPane, String currentUserId, String receiverUserId,boolean flag) {
        ScrollPane scrollPane = new ScrollPane(); // Create a ScrollPane
        gridPane.getChildren().clear(); // Clear previous UI elements
        gridPane.setAlignment(Pos.TOP_CENTER);
        gridPane.setHgap(10); // Horizontal gap
        gridPane.setVgap(10);

        // Set up the column constraints for the grid
        gridPane.getColumnConstraints().addAll(new ColumnConstraints(), new ColumnConstraints());

        // Create a MenuButton with options
        MenuButton optionsButton = new MenuButton("Options");
        optionsButton.setStyle("-fx-font-size: 14px;");

        // Add menu items to the MenuButton
        MenuItem blockUserItem = new MenuItem("Block User");
        MenuItem deleteForMe = new MenuItem("Delete for me");
        MenuItem deleteForEveryone=new MenuItem("Delete for everyone");
        optionsButton.getItems().addAll(blockUserItem,deleteForMe,deleteForEveryone);

        // Add the MenuButton to the GridPane at the top-right corner
        gridPane.add(optionsButton, 0, 0);

        if (!flag) {
            try {
                System.out.println("Client initialized: " + client);
                client = new Client("localhost", 12345, currentUserId);
                System.out.println("Client initialize attempt");
                saveToJson();
                //server.registerUserMain(currentUserId, this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Client initialized: " + client);
            flag = true;
        }

        VBox chatBox = new VBox(); // Create the VBox outside the try block
        chatBox.setSpacing(10); // Spacing between messages
        chatBox.setPadding(new Insets(5)); // Padding around the messages

        scrollPane.setContent(chatBox); // Set the content of the ScrollPane
        scrollPane.setPrefWidth(400); // Make the ScrollPane fit the width of the GridPane
        scrollPane.setPrefHeight(300);  // Set a fixed height for the ScrollPane
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // Always show vertical scrollbar
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // Always show vertical scrollbar
        gridPane.add(scrollPane, 0, 1, 2, 12); // Add the ScrollPane to the GridPane

        String query = "SELECT id, sender_id, message, timestamp, image_path,deleted_by_sender, deleted_by_receiver FROM contacts " +
                "WHERE ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) " +
                "AND ((sender_id = ? AND deleted_by_sender = 0) OR (receiver_id = ? AND deleted_by_receiver = 0)) " +
                "ORDER BY timestamp ASC";

        try (Connection connection = Server.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, currentUserId);
            statement.setString(2, receiverUserId);
            statement.setString(3, receiverUserId);
            statement.setString(4, currentUserId);
            statement.setString(5, currentUserId);
            statement.setString(6, currentUserId);

            ResultSet resultSet = statement.executeQuery();
            String lastDate = "";

            Label chatLabel = new Label(ClientHandler.fetchClientName(receiverUserId));
            chatLabel.setPrefWidth(200); // Set a preferred width
            chatLabel.setWrapText(true);
            gridPane.add(chatLabel, 1, 0, 1, 1);

            // List to hold checkboxes for messages
            ArrayList<CheckBox> checkBoxes = new ArrayList<>();

            while (resultSet.next()) {
                String senderId = resultSet.getString("sender_id");
                String message = resultSet.getString("message");
                String imagePath = resultSet.getString("image_path");
                Timestamp timestamp = resultSet.getTimestamp("timestamp");

                // Format the date and time
                Date messageDate = new Date(timestamp.getTime());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

                String currentDate = dateFormat.format(messageDate);
                String time = timeFormat.format(messageDate);

                // If the date changes, add a new date label
                if (!currentDate.equals(lastDate)) {
                    Label dateLabel = new Label(currentDate);
                    dateLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5;"); // Add some padding to the date
                    chatBox.getChildren().add(dateLabel); // Add the date label to the VBox
                    lastDate = currentDate;
                }

                // Create a horizontal box for the message
                HBox messageBox = new HBox();
                messageBox.setSpacing(10); // Spacing between message and time
                messageBox.setPadding(new Insets(5)); // Padding around the message

                CheckBox messageCheckBox = new CheckBox();
                messageCheckBox.setUserData(resultSet.getString("id"));
                checkBoxes.add(messageCheckBox);

                // Handle image messages
                if (imagePath != null && !imagePath.isEmpty()) {
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
                        Image image = new Image(new ByteArrayInputStream(imageBytes));
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(200);  // Adjust image width as needed
                        imageView.setPreserveRatio(true);

                        messageBox.getChildren().addAll(messageCheckBox, imageView);
                        if (senderId.equals(receiverUserId)) {
                            messageBox.setAlignment(Pos.CENTER_LEFT);
                        } else {
                            messageBox.setAlignment(Pos.CENTER_RIGHT);
                        }

                        messageCheckBox.setVisible(false);  // Initially hidden
                    }
                }else {
                    // Handle text messages
                    Label messageLabel = new Label(message + " [" + time + "]");
                    messageLabel.setStyle("-fx-background-color: lightgray; -fx-padding: 10; -fx-border-radius: 10; -fx-background-radius: 10;");
                    messageLabel.setMaxWidth(Double.MAX_VALUE); // Allow the message to expand
                    messageLabel.setWrapText(true);

                    messageBox.getChildren().addAll(messageCheckBox, messageLabel);
                    if (senderId.equals(receiverUserId)) {
                        messageBox.setAlignment(Pos.CENTER_LEFT);
                    } else {
                        messageBox.setAlignment(Pos.CENTER_RIGHT);
                    }

                    messageCheckBox.setVisible(false);  // Initially hidden
                }

                chatBox.getChildren().add(messageBox);
            }
            updateBlockButton(blockUserItem, currentUserId, receiverUserId);

            blockUserItem.setOnAction(e -> {
                if (ManageUser.isBlocked(currentUserId, receiverUserId)) {
                    // Unblock the user
                    ManageUser.unblockUser(currentUserId, receiverUserId);
                    updateBlockButton(blockUserItem, currentUserId, receiverUserId); // Update button text
                    showMessageBox("User unblocked.");
                } else {
                    // Block the user
                    ManageUser.blockUser(currentUserId, receiverUserId);
                    updateBlockButton(blockUserItem, currentUserId, receiverUserId); // Update button text
                    showMessageBox("User blocked.");
                }
            });

            // Create Done button
            Button doneButton = new Button("Done");
            //Button backButton=new Button("Back");
            doneButton.setVisible(false); // Initially hidden
            doneButton.setOnAction(e -> {
                // Loop through checkboxes and delete selected messages
                for (CheckBox checkBox : checkBoxes) {
                    if (checkBox.isSelected()) {
                        String messageIdStr = (String) checkBox.getUserData(); // Get message ID from user data
                        int messageId = Integer.parseInt(messageIdStr); // Convert to integer

                        // Determine if the current user is the sender or receiver
                        boolean isSender = false;
                        String senderId=null;
                        try {
                            String q = "SELECT sender_id FROM contacts WHERE id = ?";
                            try (Connection conn = Server.connect();
                                 PreparedStatement stat = conn.prepareStatement(q)) {
                                stat.setInt(1, messageId);
                                ResultSet result = stat.executeQuery();
                                if (result.next()) {
                                    senderId =  result.getString("sender_id");
                                }
                            }
                            isSender = senderId.equals(currentUserId);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }

                        // Call a method to delete the message from the database
                        deleteMessage(messageId, isSender);
                    }
                }
                // Refresh the chat view after deletion
                Platform.runLater(()->{showMyChats(gridPane, currentUserId, receiverUserId,true);});
                // Hide checkboxes and Done button
                for (CheckBox checkBox : checkBoxes) {
                    checkBox.setVisible(false); // Hide checkboxes
                }
                doneButton.setVisible(false); // Hide Done button
            });

            gridPane.add(doneButton, 0, 14, 2, 1); // Add Done button to the GridPane
            deleteForMe.setOnAction(e->{
                // Enable checkboxes and show the Done button
                for (CheckBox checkBox : checkBoxes) {
                    checkBox.setVisible(true); // Show checkboxes
                }
                doneButton.setVisible(true); // Show Done button
            });

            deleteForEveryone.setOnAction(e -> {
                // Enable checkboxes and show the Done button
                for (CheckBox checkBox : checkBoxes) {
                    checkBox.setVisible(true); // Show checkboxes
                }
                doneButton.setVisible(true); // Show Done button

                // Update the Done button action to delete for both sender and receiver
                doneButton.setOnAction(event -> {
                    for (CheckBox checkBox : checkBoxes) {
                        if (checkBox.isSelected()) {
                            String messageIdStr = (String) checkBox.getUserData(); // Get message ID from user data
                            int messageId = Integer.parseInt(messageIdStr); // Convert to integer

                            // Delete message for both sender and receiver
                            deleteMessageForEveryone(messageId);
                        }
                    }
                    // Refresh the chat view after deletion
                    showMyChats(gridPane, currentUserId, receiverUserId,true);

                    // Hide checkboxes and Done button
                    for (CheckBox checkBox : checkBoxes) {
                        checkBox.setVisible(false); // Hide checkboxes
                    }
                    doneButton.setVisible(false); // Hide Done button
                });
            });


        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions (e.g., show an error message in the UI)
            Label errorLabel = new Label("Error retrieving chats.");
            gridPane.add(errorLabel, 0, 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        HBox messageBox = new HBox();
        messageBox.setSpacing(10); // Spacing between message and time
        messageBox.setPadding(new Insets(5)); // Padding around the message
        TextField messageField = new TextField();
        messageField.setPromptText("Enter your message here...");

        // Add an image upload button
        Button imageUploadButton = new Button("Upload Image");
        imageUploadButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                // Send the image to the server
                client.sendImage(selectedFile, Integer.parseInt(receiverUserId),chatBox);
                System.out.println("Image sent: " + selectedFile.getName());
                //displayImage(selectedFile, messageField, chatBox, scrollPane);
            }
        });

        messageBox.getChildren().add(messageField);
        messageBox.getChildren().add(imageUploadButton);
        messageBox.setAlignment(Pos.BOTTOM_RIGHT);
        gridPane.add(messageBox, 0, 13);

        Button sendButton = new Button("Send");
        sendButton.getStyleClass().add("button");
        Button backButton = new Button("Back");
        backButton.getStyleClass().add("button");
        Button refreshButton=new Button("Refresh");
        refreshButton.getStyleClass().add("button");
        gridPane.add(sendButton, 1, 13);
        gridPane.add(backButton, 0, 20);
        gridPane.add(refreshButton, 1, 20);

        sendButton.setOnAction(e -> {
            String msg = messageField.getText();
            if(ManageUser.isBlocked(currentUserId,receiverUserId)){
                showMessageBox("This user has been blocked by you");
                return;
            }
            if (ManageUser.isBlocked(receiverUserId, currentUserId)) {
                displayMsg(msg, messageField, chatBox, scrollPane);
                return;  // Exit if the receiver has blocked the sender
            }

            if (!msg.trim().isEmpty()) {
                client.sendMessage(receiverUserId, msg);
                messageField.clear();
                System.out.println("Message sent: " + msg); // Debugging output
                displayMsg(msg, messageField, chatBox, scrollPane);
                //showMyChats(gridPane,currentUserId,receiverUserId,true);
            }
        });
        backButton.setOnAction(e -> Platform.runLater(() -> this.fetchUsers(true)));
        refreshButton.setOnAction(e -> Platform.runLater(() -> this.refresh(gridPane,currentUserId,receiverUserId,true)));
        // Force layout refresh after adding all messages
        Platform.runLater(() -> this.gridPane.layout());
    }
    public void refresh(GridPane gridPane,String currentUserId,String receiverUserId,boolean flag){
        this.showMyChats(gridPane,currentUserId,receiverUserId,flag);

    }

    public static void deleteMessage(int messageId, boolean isSender) {
        String checkSql = "SELECT * FROM contacts WHERE id = ?";
        String updateSql = "UPDATE contacts SET deleted_by_sender = ?, deleted_by_receiver = ? WHERE id = ?";
        String deleteSql = "DELETE FROM contacts WHERE id = ?";

        try (Connection connection = Server.connect(); // Use your connection method
             PreparedStatement checkStatement = connection.prepareStatement(checkSql);
             PreparedStatement updateStatement = connection.prepareStatement(updateSql);
             PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {

            // Check if the message is already marked as deleted
            checkStatement.setInt(1, messageId);
            ResultSet resultSet = checkStatement.executeQuery();
            String imagePath = null;

            if (resultSet.next()) {
                int deletedBySender = resultSet.getInt("deleted_by_sender");
                int deletedByReceiver = resultSet.getInt("deleted_by_receiver");
                imagePath = resultSet.getString("image_path");

                // If the message is already marked as deleted by both, delete it from the database and delete the image if present
                if (deletedBySender == 1 && deletedByReceiver == 1) {
                    // Delete the image file if it exists
                    if (imagePath != null && !imagePath.isEmpty()) {
                        File imageFile = new File(imagePath);
                        if (imageFile.exists() && imageFile.delete()) {
                            System.out.println("Image file deleted successfully.");
                        } else {
                            System.out.println("Image file not found or could not be deleted.");
                        }
                    }
                    // Delete the message from the database
                    deleteStatement.setInt(1, messageId);
                    deleteStatement.executeUpdate();
                    System.out.println("Message and associated image (if any) have been permanently deleted.");
                    return;
                }
            }

            // Prepare to update the deletion status based on the current user
            if (isSender) {
                updateStatement.setInt(1, 1); // Mark as deleted by sender
                updateStatement.setInt(2, resultSet.getInt("deleted_by_receiver")); // Keep receiver's status
            } else { // If deleting as receiver
                updateStatement.setInt(1, resultSet.getInt("deleted_by_sender")); // Keep sender's status
                updateStatement.setInt(2, 1); // Mark as deleted by receiver
            }

            updateStatement.setInt(3, messageId);
            updateStatement.executeUpdate();
            System.out.println("Message marked as deleted successfully.");

        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getMessage());
        }
    }

    public static void deleteMessageForEveryone(int messageId) {
        String checkSql = "SELECT image_path FROM contacts WHERE id = ?";
        String updateSql = "UPDATE contacts SET deleted_by_sender = 1, deleted_by_receiver = 1 WHERE id = ?";
        String deleteSql = "DELETE FROM contacts WHERE id = ?";

        try (Connection connection = Server.connect(); // Use your connection method
             PreparedStatement checkStatement = connection.prepareStatement(checkSql);
             PreparedStatement updateStatement = connection.prepareStatement(updateSql);
             PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {

            // First, check if there is an image associated with the message
            checkStatement.setInt(1, messageId);
            ResultSet resultSet = checkStatement.executeQuery();
            String imagePath = null;

            if (resultSet.next()) {
                imagePath = resultSet.getString("image_path");
            }

            // Update the deletion status to mark as deleted by both sender and receiver
            updateStatement.setInt(1, messageId);
            updateStatement.executeUpdate();

            // Delete the image if it exists, then remove the message from the database
            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (imageFile.exists() && imageFile.delete()) {
                    System.out.println("Image file deleted successfully.");
                } else {
                    System.out.println("Image file not found or could not be deleted.");
                }
            }

            // Finally, delete the message from the database
            deleteStatement.setInt(1, messageId);
            deleteStatement.executeUpdate();
            System.out.println("Message and associated image (if any) have been permanently deleted for everyone.");

        } catch (SQLException e) {
            System.err.println("SQL error in deleteMessageForEveryone: " + e.getMessage());
        }
    }


    private void updateBlockButton(MenuItem blockUserItem, String currentUserId, String receiverUserId) {
        if (ManageUser.isBlocked(currentUserId, receiverUserId)) {
            blockUserItem.setText("Unblock User");
        } else {
            blockUserItem.setText("Block User");
        }
    }

    private void displayUsers(GridPane gridPane, ObservableList<String> users,boolean flag) {
        gridPane.getChildren().clear();
        gridPane.setAlignment(Pos.TOP_CENTER);
        gridPane.setHgap(10); // Horizontal gap
        gridPane.setVgap(10);
        // Create a list view to display the users
        ListView<String> usersListView = new ListView<>();
        usersListView.setItems(users);

        Button deleteContact=new Button("Delete contact");
        deleteContact.getStyleClass().add("button");
        gridPane.add(deleteContact, 0, 9);

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("button");
        gridPane.add(backButton, 0, 7);

        // Add the list view to the grid pane
        gridPane.add(usersListView, 0, 0);
        gridPane.add(new Label("Users"), 0, 1);

        // Fetch groups from the database and display them
        ObservableList<String> groups = fetchGroupsFromDB();
        ListView<String> groupsListView = new ListView<>();
        groupsListView.setItems(groups);

        // Add the list view for groups to the grid pane
        gridPane.add(groupsListView, 1, 0);
        gridPane.add(new Label("Groups"), 1, 1);
        backButton.setOnAction(e->{
            System.out.println("Back button pressed");
            fetchUsers(true);
        });


        deleteContact.setOnAction(e->{
            String selectedUser  = usersListView.getSelectionModel().getSelectedItem();
            if (selectedUser  != null) {
                // Confirm deletion
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Delete Contact");
                confirmationAlert.setHeaderText(null);
                confirmationAlert.setContentText("Are you sure you want to delete this contact?");
                Optional<ButtonType> result = confirmationAlert.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // Proceed with deletion
                    try (Connection connection = Server.connect();
                         PreparedStatement deleteContactStatement = connection.prepareStatement("DELETE FROM contactlist WHERE name = ? AND user_id = ?");
                         PreparedStatement deleteChatsStatement = connection.prepareStatement("DELETE FROM contacts WHERE sender_id = ? OR receiver_id = ?")) {

                        int contactId = getContactIdByName(selectedUser , connection);
                        // Delete associated chats
                        deleteChatsStatement.setInt(1, contactId);
                        deleteChatsStatement.setInt(2, contactId);
                        deleteChatsStatement.executeUpdate();

                        // Delete the contact
                        deleteContactStatement.setString(1, selectedUser );
                        deleteContactStatement.setString(2, manageUser .uno);

                        int rowsAffected = deleteContactStatement.executeUpdate();

                        if (rowsAffected > 0) {
                            manageUser.showAlert("Contact deleted successfully.");
                            // Refresh the user list
                            fetchUsers (flag);
                        } else {
                            manageUser.showAlert("Contact not found or could not be deleted.");
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        manageUser.showAlert( "An error occurred while deleting the contact.");
                    }
                }
            } else {
                manageUser.showAlert("Please select a contact to delete.");
            }
        });
        // Add a button to start a chat with the selected user
        Button startChatButton = new Button("Start Chat");
        startChatButton.setOnAction(e -> {
            // Get the selected user
            String selectedUser  = usersListView.getSelectionModel().getSelectedItem();

            // Ensure the selected user is not null
            if (selectedUser  != null) {
                //String[] parts = selectedUser .split(" - ");
                //String receiverName = parts[1];
                // Fetch the receiver's ID from the database based on the selected user's name
                try (Connection connection = Server.connect();
                     PreparedStatement statement = connection.prepareStatement("SELECT id FROM contactlist WHERE name = ? AND user_id = ?")) {

                    statement.setString(1, selectedUser );
                    statement.setString(2, manageUser .uno); // Ensure the user_id matches the current user

                    ResultSet resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        String receiverId = resultSet.getString("id");
                        this.receiverId = receiverId; // Update the receiverId variable

                        System.out.println("Receiver ID updated to: " + receiverId);
                        // Start a chat with the selected user
                        this.showMyChats(gridPane,manageUser.uno ,receiverId, flag);
                    } else {
                        System.out.println("Error: User not found");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
        gridPane.add(startChatButton, 0, 1);

        // Add a button to start a group chat with the selected group
        Button startGroupChatButton = new Button("Start Group Chat");
        startGroupChatButton.setOnAction(e -> {
            String selectedGroup = groupsListView.getSelectionModel().getSelectedItem();
            if (selectedGroup != null) {
                // Start a group chat with the selected group
                startGroupChat(selectedGroup,flag);
            } else {
                System.out.println("Error: No group selected");
            }
        });
        gridPane.add(startGroupChatButton, 1, 2);
    }

    private int getContactIdByName(String contactName, Connection connection) throws SQLException {
        String query = "SELECT id FROM contactlist WHERE name = ? AND user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, contactName);
            statement.setString(2, manageUser.uno); // Ensure the user_id matches the current user
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            } else {
                throw new SQLException("Contact not found.");
            }
        }
    }

    private  ObservableList<String> fetchGroupsFromDB() {
        ObservableList<String> groups = FXCollections.observableArrayList();
        String loggedInUserId=manageUser.uno;
        try (Connection connection = Server.connect()) {
            if (connection != null) {
                // Query to select groups where the logged-in user ID is part of group_members
                String groupQuery = "SELECT group_id, group_name FROM user_groups WHERE FIND_IN_SET(?, group_members)";
                PreparedStatement statement = connection.prepareStatement(groupQuery);
                statement.setString(1, loggedInUserId);  // Set the logged-in user ID

                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    String groupName = resultSet.getString("group_name");

                    groups.add(groupName);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        System.out.println(groups);
        return groups;
    }

    private void startGroupChat(String groupName,boolean flag) {
        List<String> groupMembers = new ArrayList<>();
        String groupId = null;
        String currentUserId = manageUser.uno;
        //String[] parts=groupName.split(" ",2);
        String userGroupName=groupName;
        try (Connection conn = Server.connect()) {
            // Step 1: Fetch groupId based on groupName
            PreparedStatement groupIdStatement = conn.prepareStatement("SELECT group_id FROM user_groups WHERE group_name = ?");
            groupIdStatement.setString(1, userGroupName);
            ResultSet groupIdResult = groupIdStatement.executeQuery();

            if (groupIdResult.next()) {
                groupId = groupIdResult.getString("group_id");
            } else {
                System.out.println("Group not found: " + groupName);
                return; // Exit if the group is not found
            }

            // Step 2: Fetch group members based on groupId
            PreparedStatement groupMembersStatement = conn.prepareStatement("SELECT group_members FROM user_groups WHERE group_id = ?");
            groupMembersStatement.setString(1, groupId);
            ResultSet membersResultSet = groupMembersStatement.executeQuery();

            if (membersResultSet.next()) {
                String membersString = membersResultSet.getString("group_members");
                groupMembers.addAll(Arrays.asList(membersString.split(","))); // Assuming group_members are stored as comma-separated values
            } else {
                System.out.println("No members found for group: " + groupName);
                return; // Exit if no members are found
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return; // Exit if there is a SQL exception
        }

        showGroupChat(this.gridPane, currentUserId, groupId,groupName,flag);
    }

    public void showGroupChat(GridPane gridPane, String currentUserId, String groupId,String groupName,boolean flag) {
        final ScrollPane[] scrollPane = {new ScrollPane()}; // Create a ScrollPane
        gridPane.getChildren().clear(); // Clear previous UI elements
        gridPane.setAlignment(Pos.TOP_CENTER);
        gridPane.setHgap(10); // Horizontal gap
        gridPane.setVgap(10);

        if (!flag) {
            try {
                client = new Client("localhost", 12345, currentUserId);
            } catch (IOException e) {
                e.printStackTrace();
            }
            flag = true;
            saveToJson();
        }

        // Query to fetch group messages
        String query = "SELECT user_id AS sender_id, message, timestamp FROM group_messages " +
                "WHERE group_id = ? ORDER BY timestamp ASC";

        try (Connection connection = Server.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, groupId); // Fetch messages for the group
            ResultSet resultSet = statement.executeQuery();

            VBox chatBox = new VBox(); // Create a VBox to hold chat messages
            chatBox.setSpacing(10); // Spacing between messages
            chatBox.setPadding(new Insets(5)); // Padding around the messages
            scrollPane[0].setContent(chatBox); // Set the content of the ScrollPane
            scrollPane[0].setPrefWidth(Double.MAX_VALUE);
            scrollPane[0].setPrefHeight(300);
            gridPane.add(scrollPane[0], 0, 1, 2, 12); // Add the ScrollPane to the GridPane

            String lastDate = "";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

            while (resultSet.next()) {
                String senderId = resultSet.getString("sender_id");
                String message = resultSet.getString("message");
                Timestamp timestamp = resultSet.getTimestamp("timestamp");
                String sender=ClientHandler.fetchClientName(senderId);

                // Format the date and time
                Date messageDate = new Date(timestamp.getTime());
                String currentDate = dateFormat.format(messageDate);
                String time = timeFormat.format(messageDate);

                // Add date label if the date has changed
                if (!currentDate.equals(lastDate)) {
                    Label dateLabel = new Label(currentDate);
                    dateLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5;");
                    chatBox.getChildren().add(dateLabel);
                    lastDate = currentDate;
                }

                // Create a horizontal box for the message
                HBox messageBox = new HBox();
                messageBox.setSpacing(10);
                messageBox.setPadding(new Insets(5));



                if (senderId.equals(currentUserId)) {
                    Label messageLabel = new Label(message + " [" + time + "]");
                    messageLabel.setStyle("-fx-background-color: lightgray; -fx-padding: 10; -fx-border-radius: 10; -fx-background-radius: 10;");
                    messageLabel.setMaxWidth(Double.MAX_VALUE);
                    messageLabel.setWrapText(true);
                    messageBox.getChildren().add(messageLabel);
                    messageBox.setAlignment(Pos.CENTER_RIGHT);
                } else {
                    Label messageLabel = new Label(sender+":"+message + " [" + time + "]");
                    messageLabel.setStyle("-fx-background-color: lightgray; -fx-padding: 10; -fx-border-radius: 10; -fx-background-radius: 10;");
                    messageLabel.setMaxWidth(Double.MAX_VALUE);
                    messageLabel.setWrapText(true);
                    messageBox.getChildren().add(messageLabel);
                    messageBox.setAlignment(Pos.CENTER_LEFT);
                }
                chatBox.getChildren().add(messageBox);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Error retrieving group chats.");
            gridPane.add(errorLabel, 0, 0);
        }catch(Exception e){
            System.out.println("ERROR:"+e.getMessage());
        } finally{
            HBox messageBox = new HBox();
            messageBox.setSpacing(10);
            messageBox.setPadding(new Insets(5));
            TextField messageField = new TextField();
            messageField.setPromptText("Enter your message here...");
            messageBox.getChildren().add(messageField);
            messageBox.setAlignment(Pos.BOTTOM_RIGHT);
            gridPane.add(messageBox, 0, 13);
            Button sendButton = new Button("Send");
            Button backButton = new Button("Back");
            Button refreshButton = new Button("Refresh");

            gridPane.add(sendButton, 1, 13);
            gridPane.add(backButton, 3, 13);
            gridPane.add(refreshButton, 3, 20);

            sendButton.setOnAction(e -> {
                String msg = messageField.getText();
                if (!msg.trim().isEmpty()) {
                    // Send the message to all group members
                    List<String> groupMembers = getGroupMembers(groupId);
                    client.sendMessageToGroup(groupId, msg, currentUserId, groupMembers);
                    messageField.clear();
                    System.out.println("Group message sent: " + msg);

                    // Add the sent message to the chat box
                    scrollPane[0] = (ScrollPane) gridPane.getChildren().get(0);
                    VBox chatBox = (VBox) scrollPane[0].getContent();
                    HBox sentMessageBox = new HBox();
                    sentMessageBox.setSpacing(10);
                    sentMessageBox.setPadding(new Insets(5));

                    String time = new SimpleDateFormat("HH:mm").format(new Date());
                    String senderName = manageUser.uname;
                    Label sentMessageLabel = new Label(senderName + ": " + msg + " [" + time + "]");
                    sentMessageLabel.setStyle("-fx-background-color: lightgray; -fx-padding: 10; -fx-border-radius: 10; -fx-background-radius: 10;");
                    sentMessageLabel.setMaxWidth(Double.MAX_VALUE);
                    sentMessageLabel.setWrapText(true);

                    sentMessageBox.getChildren().add(sentMessageLabel);
                    sentMessageBox.setAlignment(Pos.CENTER_RIGHT);
                    chatBox.getChildren().add(sentMessageBox);
                }
            });
            backButton.setOnAction(e -> {
                Platform.runLater(() -> {
                    showUserOptions(gridPane,true);
                });
            });
            refreshButton.setOnAction(e -> {
                Platform.runLater(() -> {
                    showGroupChat(gridPane, currentUserId, groupId,groupName, true);
                });
            });
        }
    }


    // Method to retrieve group members from the database
    private List<String> getGroupMembers(String groupId) {
        List<String> members = new ArrayList<>();

        String query = "SELECT group_members FROM user_groups WHERE group_id = ?";

        try (Connection connection = Server.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, Integer.parseInt(groupId)); // Set the group ID (assuming groupId is an integer)
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String groupMembers = resultSet.getString("group_members");
                // Split the groupMembers string into individual user IDs
                if (groupMembers != null && !groupMembers.isEmpty()) {
                    String[] userIds = groupMembers.split(","); // Adjust delimiter as needed
                    // Add each user ID to the members list
                    for (String userId : userIds) {
                        members.add(userId.trim()); // Trim to remove any leading/trailing spaces
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Optionally handle the error, e.g., log it or notify the user
        }

        return members; // Return the list of group members
    }


    private void addContact(String name, String userId) {
        String checkUserQuery = "SELECT id FROM users WHERE username = ?";
        String insertQuery = "INSERT INTO contactlist (id, name, user_id) VALUES (?, ?,?)";

        try (Connection connection = Server.connect();
             PreparedStatement checkStatement = connection.prepareStatement(checkUserQuery)) {

            // Check if the user exists
            checkStatement.setString(1, name);
            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                // User exists, get the ID
                int existingUserId = resultSet.getInt("id");
                // Insert into contactlist using the existing user's ID
                try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                    insertStatement.setInt(1, existingUserId); // Use existing user ID
                    insertStatement.setString(2, name);
                    insertStatement.setString(3, userId);

                    int result = insertStatement.executeUpdate();
                    if (result > 0) {
                        messageLabel.setText("Contact added successfully!");
                    } else {
                        messageLabel.setText("Error adding contact.");
                    }
                }
            } else {
                messageLabel.setText("User  does not exist. Please check the username.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            messageLabel.setText("Error adding contact: " + e.getMessage());
        }
    }

    private void showAddContactOptions(GridPane gridPane,boolean flag) {
        gridPane.getChildren().clear(); // Clear previous UI elements
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10); // Horizontal gap
        gridPane.setVgap(10); // Vertical gap

        // Input fields for name and phone number
        TextField nameField = new TextField();
        nameField.setPromptText("Enter contact name");

        Button addButton = new Button("Add Contact");
        addButton.getStyleClass().add("button");

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("button");

        // Center the components in their respective cells
        GridPane.setHalignment(nameField, HPos.CENTER);
        GridPane.setHalignment(addButton, HPos.CENTER);

        // Add components to the grid
        gridPane.add(new Label("Contact Name:"), 0, 0);
        gridPane.add(nameField, 1, 0);
        gridPane.add(addButton, 1, 2);
        gridPane.add(backButton, 1, 5); // Position the back button below the add button
        gridPane.add(messageLabel, 0, 3, 2, 1); // Display messages

        // Handle add contact action
        addButton.setOnAction(e -> {
            String name = nameField.getText();
            String userId = manageUser.uno; // Get the current user's ID

            if (!name.isEmpty() ) {
                addContact(name, userId); // Call the method to add the contact
            } else {
                messageLabel.setText("Please enter a contact name");
            }
        });

        backButton.setOnAction(e -> {
            this.showUserOptions (gridPane,flag); // Show user options again
        });
    }

    public String getMessage(){
        return msg;

    }
    public static void main(String[] args) {
        launch(args);
    }

}




















