package hellofx;

import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.*;

public class ManageUser {
    String uname;
    String uno;
    private String pswd;
    private String email="";
    private boolean stat = false;
    private Label messageLabel;
    private int generatedOtp;
    public static GridPane gridPane = new GridPane();
    

    public ManageUser(Label messageLabel) {
        this.messageLabel = messageLabel;
    }

    private static Connection connect() {
        String url = "jdbc:mysql://localhost:3306/mydemo"; // Replace with your DB URL
        String user = "root"; // Replace with your DB username
        String password = "abcdefg7"; // Replace with your DB password

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void createUser(String name, String pswd, String email) {
        this.uname = name;
        // Validate password
        if (!isValidPassword(pswd)) {
            return; // Exit if password is not valid
        }
        this.pswd = pswd;
        this.email = email;
        String checkUserQuery = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkUserQuery)) {

            checkStmt.setString(1, this.uname);
            ResultSet resultSet = checkStmt.executeQuery();
            resultSet.next();
            int userCount = resultSet.getInt(1);

            // Check if the user already exists
            if (userCount > 0) {
                messageLabel.setText("The user already exists.");
                return; // Exit the method if user exists
            }

            // If user does not exist, proceed to insert
            String insertQuery = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, this.uname);
                stmt.setString(2, this.pswd);
                stmt.setString(3, this.email);
                int result = stmt.executeUpdate();

                if (result > 0) {
                    // Retrieve the generated user ID (optional)
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);

                        // Proceed to insert the user into the contacts table
                        String insertContactQuery = "INSERT INTO contacts (sender_id, receiver_id, message, status) VALUES (?, ?, 'default', 'sent')";
                        try (PreparedStatement contactStmt = conn.prepareStatement(insertContactQuery)) {
                            contactStmt.setInt(1, userId);  // Set sender_id as the new user's ID
                            contactStmt.setInt(2, 4);  // In contacts, this could be a placeholder if needed

                            int contactResult = contactStmt.executeUpdate();

                            if (contactResult > 0) {
                                messageLabel.setText("User created successfully!"); // Only show success message after both insertions
                            } else {
                                messageLabel.setText("Error adding user to contacts.");
                            }
                        }

                    } else {
                        messageLabel.setText("Error retrieving user ID.");
                    }

                } else {
                    messageLabel.setText("Error creating user.");
                }
            }

        }  catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error creating user: " + e.getMessage());
        }
    }

    public boolean isValidPassword(String s) {
        if (s.length() < 8) {
            messageLabel.setText("Password is too short!");
            return false;
        }

        boolean hasLetter = false, hasDigit = false, hasSpecial = false;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLetter(s.charAt(i))) {
                hasLetter = true;
            }
            if (Character.isDigit(s.charAt(i))) {
                hasDigit = true;
            }
            if (!Character.isLetter(s.charAt(i)) && !Character.isDigit(s.charAt(i))) {
                hasSpecial = true;
            }
        }

        if (hasLetter && hasDigit && hasSpecial) {
            return true;
        } else {
            messageLabel.setText("Password must contain letters, digits, and special characters!");
            return false;
        }
    }

    public boolean login(String name, String pswd) {
        System.out.println("Login method called for username: " + name);
        if (!isValidPassword(pswd)) {
            return false; // If not valid, stop further processing
        }

        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setString(2, pswd);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                if (stat) {
                    messageLabel.setText("User is already logged in.");
                    return false;
                } else {
                    this.stat = true;
                    this.uname = name;
                    this.pswd = pswd;
                    messageLabel.setText("Login successful for " + name);

                    // Get user ID for the logged-in user
                    String insertQuery = "SELECT id FROM users WHERE username = ?";


                    System.out.println("Trying to retrieve user ID for username: " + this.uname);

                    try (PreparedStatement stmnt = conn.prepareStatement(insertQuery)) {
                        stmnt.setString(1, this.uname);
                        ResultSet rSet = stmnt.executeQuery();
                        if (rSet.next()) {
                            this.uno = rSet.getString("id");
                            System.out.println("User ID retrieved: " + this.uno);
                            return true;
                        }
                        else {
                            System.out.println("User not found for username: " + this.uname);
                        }
                        conn.close();
                        return true;
                    }

                }
            } else {
                messageLabel.setText("Invalid username or password.");
                conn.close();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error during login: " + e.getMessage());
            return false;
        }
    }

    public boolean logout(String name) {
        if (this.stat) {
            this.stat = false;
            messageLabel.setText(name + " has logged out successfully.");
            return true;
        } else {
            messageLabel.setText("User is not logged in.");
            return false;
        }
    }

    public void resetPswd(String username, String oldPswd, String newPswd) {
        // Check if the new password is valid
        if (!isValidPassword(newPswd)) {
            messageLabel.setText("Invalid new password.");
            return;
        }

        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, oldPswd);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Old password is correct; proceed to update the new password
                String updateQuery = "UPDATE users SET password = ? WHERE username = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                    updateStmt.setString(1, newPswd);
                    updateStmt.setString(2, username);

                    int result = updateStmt.executeUpdate();

                    if (result > 0) {
                        messageLabel.setText("Password reset successful.");
                    } else {
                        messageLabel.setText("Error resetting password.");
                    }
                }
            } else {
                messageLabel.setText("Old password is incorrect or username not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error resetting password: " + e.getMessage());
        }
    }

    private void storeOtpForVerification(int otp) {
        generatedOtp = otp;
    }

    // Method to handle the "Forgot Password" logic
    public void handleForgotPassword(GridPane gridPane,Main main,String name){
        gridPane.getChildren().clear();
        System.out.println("Id in manage "+name);
        try (Connection connection = connect()) {
            String query = "SELECT email FROM users WHERE username = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if (!resultSet.next()) {
                showAlert("Email not found in the database.");
                return;
            }
            else if (resultSet.next()) {
                email= resultSet.getString("email");
            }

            //String username = resultSet.getString("username");
            int otp = sendOtpEmail(email,name);
            storeOtpForVerification(otp);  // Store OTP in user data for later verification

            // Ask the user to input the OTP
            Label otpLabel = new Label("Enter OTP:");
            TextField otpField = new TextField();
            Button verifyOtpButton = new Button("Verify OTP");

            gridPane.add(otpLabel, 0, 0);
            gridPane.add(otpField, 1, 0);
            gridPane.add(verifyOtpButton, 1, 1);

            verifyOtpButton.setOnAction(event -> {
                verifyOtp(otpField, otp);
                if (Integer.parseInt(otpField.getText()) == otp) {
                    // Allow user to reset password
                    Label newPasswordLabel = new Label("Enter new password:");
                    PasswordField newPasswordField = new PasswordField();
                    Button resetPasswordButton = new Button("Reset Password");

                    gridPane.getChildren().clear();
                    gridPane.add(newPasswordLabel, 0, 0);
                    gridPane.add(newPasswordField, 1, 0);
                    gridPane.add(resetPasswordButton, 1, 1);

                    resetPasswordButton.setOnAction(resetEvent -> {
                        String newPassword = newPasswordField.getText();
                        if (isValidPassword(newPassword)) {
                            updatePasswordInDatabase(name, newPassword);
                            gridPane.getChildren().clear();
                            Button goToMainButton = new Button("Go to Main");
                            gridPane.add(goToMainButton, 0, 0);
                            goToMainButton.setOnAction(goToMainEvent -> {
                                main.loginOption(gridPane, stat);; // Navigate to the main user options
                            });
                        } else {
                            showAlert("Invalid new password.");
                        }
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("An error occurred: " + e.getMessage());
        }
    }

    private int sendOtpEmail(String email, String username) throws MessagingException, UnsupportedEncodingException {
        Random rand = new Random();
        int otp = 100000 + rand.nextInt(900000);


        final String fromEmail = "noreplychattapp@gmail.com";  // Use your email
        final String password = "vyanwenzphhuuckk";  // Use your email password
        final String toEmail = email;

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(fromEmail, "NoReply-JD"));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail, username));
        msg.setSubject("OTP Verification");
        msg.setText("Dear " + username + ",\n\nYour OTP for password reset is: " + otp + "\n\nRegards,\nTeam");

        Transport.send(msg);

        return otp;
    }

    private void updatePasswordInDatabase(String username, String newPassword) {
        try (Connection conn = connect()) {
            String updateQuery = "UPDATE users SET password = ? WHERE username = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setString(1, newPassword);
            updateStmt.setString(2, username);

            int result = updateStmt.executeUpdate();

            if (result > 0) {
                showAlert("Password updated successfully.");

            } else {
                showAlert("Failed to update password.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("An error occurred: " + e.getMessage());
        }
        //Main.showUserOptions(new GridPane());
    }

    private void verifyOtp(TextField otpField, int expectedOtp) {
        if (Integer.parseInt(otpField.getText()) == expectedOtp) {
            showAlert("OTP Verified!");
        } else {
            showAlert("Incorrect OTP.");
        }
    }

    void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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


    public static void sendNotificationEmail(String email, String username, String senderId, String messageContent) throws MessagingException, UnsupportedEncodingException {
        final String fromEmail = "noreplychattapp"; // Use your email
        final String password = "vyanwenzphhuuckk"; // Use your email password
        final String toEmail = email;

        System.out.println("Sending to "+email);

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(fromEmail, "NoReply-JD"));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail, username));
        msg.setSubject("New Pending Message Notification from "+fetchClientName(senderId));
        msg.setText("Dear " + username + ",\n\nYou have a new message from " + ClientHandler.fetchClientName(senderId) + ":\n\n" + messageContent + "\n\nPlease check your messages.\n\nRegards,\nTeam");

        Transport.send(msg);
    }
    public static boolean notifyPendingMessages() {
        System.out.println("Sending notification:notifyPending");
        String query = "SELECT sender_id, receiver_id, message FROM contacts WHERE status = 'pending'";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String senderId = resultSet.getString("sender_id");
                String receiverId = resultSet.getString("receiver_id");
                String messageContent = resultSet.getString("message");

                // Retrieve the receiver's email (assuming you have a method for that)
                String receiverEmail = getEmailById(receiverId); // You need to implement this method
                String receiverUsername = ClientHandler.fetchClientName(receiverId); // You need to implement this method

                // Prepare and send the notification email
                sendNotificationEmail(receiverEmail, receiverUsername, senderId, messageContent);
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean notifyPendingMessages(String recipientId) {
        System.out.println("Sending notification:notifyPending");
        String query = "SELECT sender_id, message FROM contacts WHERE receiver_id = ? AND status = 'pending'";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, recipientId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String senderId = resultSet.getString("sender_id");
                String messageContent = resultSet.getString("message");

                // Fetch the receiver's email and username
                String receiverEmail = getEmailById(recipientId); // Make sure this is implemented
                String receiverUsername = ClientHandler.fetchClientName(recipientId); // Make sure this is implemented

                // Prepare and send the notification email if email and username are valid
                if (receiverEmail != null && receiverUsername != null) {
                    sendNotificationEmail(receiverEmail, receiverUsername, senderId, messageContent);

                } else {
                    System.err.println("Email or username not found for recipientId: " + recipientId);
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    private static String getEmailById(String receiverId) {
        String insertQuery = "SELECT email FROM users WHERE id = ?";
        System.out.println("Trying to retrieve user email for id: " + receiverId);
        try(Connection conn=Server.connect()){
            try (PreparedStatement stmnt = conn.prepareStatement(insertQuery)) {
                stmnt.setString(1, receiverId);
                ResultSet rSet = stmnt.executeQuery();
                if (rSet.next()) {
                    String em=rSet.getString("email");
                    System.out.println("Email retrieved: "  );
                    return em;
                }
                else {
                    System.out.println("email not found for : " );
                }
                conn.close();
                return "annejacika8@gmail.com";
            } catch (SQLException e) {
                e.printStackTrace();
                return "annejacika8@gmail.com";
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return "";
    }
    public void createGroup() {
        
        TextInputDialog groupNameDialog = new TextInputDialog();
        groupNameDialog.setTitle("Create Group");
        groupNameDialog.setHeaderText("Enter Group Name:");
        groupNameDialog.setContentText("Group Name:");
        Optional<String> groupNameResult = groupNameDialog.showAndWait();
        
        groupNameResult.ifPresent(groupName -> {
            
            List<String> users = fetchUsersFromDB(Integer.parseInt(uno));
            if (!users.isEmpty()) {
                
                List<String> selectedUsers = displayUserSelectionDialog(users);

                if (!selectedUsers.isEmpty()) {
            
                    saveGroupToDB(groupName, selectedUsers);
                } else {
                    showAlert("Please select at least one user to create a group.");
                }
            } else {
                showAlert("No users found in the database.");
            }
        });
    }
    
    
    private List<String> fetchUsersFromDB(int userId) {
        List<String> users = new ArrayList<>();
    
        // First query: Get names from contactlist table where user_id matches
        String query1 = "SELECT DISTINCT name FROM contactlist WHERE user_id = ?";
        
        // Second query: Get name from users table where id matches
        String query2 = "SELECT username FROM users WHERE id = ?";
    
        try (Connection conn = connect()) {
            if (conn != null) {
                // Execute the first query
                try (PreparedStatement pstmt1 = conn.prepareStatement(query1)) {
                    pstmt1.setInt(1, userId);
                    ResultSet rs1 = pstmt1.executeQuery();
                    while (rs1.next()) {
                        users.add(rs1.getString("name"));
                    }
                }
    
                // Execute the second query
                try (PreparedStatement pstmt2 = conn.prepareStatement(query2)) {
                    pstmt2.setInt(1, userId);
                    ResultSet rs2 = pstmt2.executeQuery();
                    if (rs2.next()) {
                        users.add(rs2.getString("username"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    
    private List<String> displayUserSelectionDialog(List<String> users) {
        // Create a new Stage for the dialog
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Select Users");
        dialogStage.initModality(Modality.APPLICATION_MODAL); // Make it modal
        dialogStage.setMinWidth(300);
        dialogStage.setMinHeight(400);

        // Create a VBox to hold the checkboxes and buttons
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Create a list of checkboxes
        List<CheckBox> checkboxes = new ArrayList<>();
        for (String user : users) {
            CheckBox checkbox = new CheckBox(user);
            checkboxes.add(checkbox);
            vbox.getChildren().add(checkbox);
        }
        // Create OK and Cancel buttons
        Button okButton = new Button("OK");
        Button cancelButton = new Button("Cancel");

        // Layout for buttons (Horizontally aligned)
        HBox buttonBox = new HBox(10, okButton, cancelButton); // 10px spacing between buttons
        buttonBox.setAlignment(Pos.CENTER);
        vbox.getChildren().add(buttonBox);

        Scene scene = new Scene(vbox);
        dialogStage.setScene(scene);

        List<String> selectedUsers = new ArrayList<>();

        // Handle OK button click
        okButton.setOnAction(e -> {
            selectedUsers.clear(); // Clear the list before adding new selections
            for (CheckBox checkbox : checkboxes) {
                if (checkbox.isSelected()) {
                    selectedUsers.add(checkbox.getText());
                }
            }
            dialogStage.close(); // Close the dialog
        });

        // Handle Cancel button click
        cancelButton.setOnAction(e -> {
            dialogStage.close(); // Close the dialog without saving selections
        });

        dialogStage.showAndWait(); // Show the dialog and wait for it to close

        return selectedUsers; // Return the selected users
    }

    private void saveGroupToDB(String groupName, List<String> selectedUsers) {
        try (Connection conn = connect()) {
            if (conn != null) {
                // Convert selected user usernames to their corresponding IDs
                List<String> userIds = new ArrayList<>();
                for (String username : selectedUsers) {
                    // Retrieve user ID from the database based on the username
                    String userId = getUserIdByUsername(conn, username);
                    if (userId != null) {
                        userIds.add(userId);
                    }
                }
                // Convert the list of user IDs to a comma-separated string
                String membersString = String.join(",", userIds);

                // Insert group name and members string into the groups table
                String groupQuery = "INSERT INTO user_groups (group_name, group_members) VALUES (?, ?)";
                PreparedStatement groupStmt = conn.prepareStatement(groupQuery);
                groupStmt.setString(1, groupName);
                groupStmt.setString(2, membersString);
                groupStmt.executeUpdate();

                showAlert("Group Created");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper method to retrieve user ID based on username
    private String getUserIdByUsername(Connection conn, String username) {
        String userId = null;
        String query = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                userId = rs.getString("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userId;
    }



    // Method to block a user
    public static void blockUser(String blockerId, String blockedId) {
        String sql = "INSERT INTO blocked_users (blocker_id, blocked_id) VALUES (?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, blockerId);
            pstmt.setString(2, blockedId);
            pstmt.executeUpdate();
            System.out.println("User  " + blockerId + " has blocked user " + blockedId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void unblockUser(String blockerId,String blockedId){
        String query = "DELETE FROM blocked_users WHERE blocker_id = ? AND blocked_id = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, blockerId);
            stmt.setString(2, blockedId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions appropriately
        }
    }

    // Check if a user is blocked
    public static boolean isBlocked(String blockerId, String blockedId) {
        String sql = "SELECT * FROM blocked_users WHERE blocker_id = ? AND blocked_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, blockerId);
            pstmt.setString(2, blockedId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // If a record exists, the user is blocked
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}



























