package application;

import javafx.application.Application;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.application.Platform;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;


/**
 * Simple Single Sign-On (SSO) system using Triple DES encryption.
 * 
 * This program allows users to attempt login three times, simulating a basic
 * Single Sign-On (SSO) system. The user provides a username and password, and
 * if the credentials match the predefined values, an encrypted token is
 * generated using Triple DES encryption. The program demonstrates basic
 * password and username checking, token generation, and encryption.
 * 
 * @author Matthew Hazelton
 * @author Michael Hanna
 */
//Main class for the JavaFX application
public class Main extends Application {

 // Single Sign-On controller instance
 private SSOController ssoController = new SSOController();
 private Label messageLabel;
 private int loginAttempts = 0;
 private TextField encryptionKeyField;
 private Button viewKeyButton;
 private boolean keyDisplayed = false;
 private Button copyKeyButton;
 private Label welcomeLabel;
 // Entry point for the application
 public static void main(String[] args) {
     launch(args);
 }

 // Start method where the UI is initialized
 @Override
 public void start(Stage primaryStage) {
     primaryStage.setTitle("Single Sign-On with JavaFX");
     VBox vbox = new VBox(10); // 10 is the spacing between nodes
     vbox.setAlignment(Pos.CENTER);

     // Title Label
     Label titleLabel = new Label("The Vault");
     titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

     // Message Label for instruction
     Label instructionLabel = new Label("Please enter your username and password below:");

     // Load default user image (blank profile) with a fixed size of 200x200 pixels
     Image defaultUserImage = new Image("https://www.pngarts.com/files/10/Default-Profile-Picture-Download-PNG-Image.png", 200, 200, false, false);
     ImageView userImageView = new ImageView(defaultUserImage);

     TextField usernameField = new TextField();
     usernameField.setPromptText("Enter username");
     usernameField.setMinWidth(150); // Set the minimum width for the username field
     usernameField.setMaxWidth(300);

     PasswordField passwordField = new PasswordField();
     passwordField.setPromptText("Enter password");
     passwordField.setMinWidth(150); // Set the minimum width for the password field
     passwordField.setMaxWidth(300);

     Button loginButton = new Button("Login");
     messageLabel = new Label();

     // Set event handler for the login button
     loginButton.setOnAction(event -> handleLogin(usernameField.getText(), passwordField.getText(), userImageView));

     copyKeyButton = new Button("Copy Key");
     copyKeyButton.setDisable(true);
     copyKeyButton.setOnAction(event -> copyEncryptionKeyToClipboard());

     Button verifyKeyButton = new Button("Verify Encryption Key");
     verifyKeyButton.setOnAction(event -> showEncryptionKeyDialog(primaryStage));

     encryptionKeyField = new TextField();
     encryptionKeyField.setPromptText("Enter Encryption Key");
     encryptionKeyField.setMaxWidth(200);

     welcomeLabel = new Label();
     vbox.getChildren().addAll(titleLabel, instructionLabel, userImageView, usernameField, passwordField,
             loginButton, welcomeLabel, encryptionKeyField, verifyKeyButton, copyKeyButton);

     // Set the scene
     primaryStage.setScene(new Scene(vbox, 400, 500)); // Increased height for better image display
     primaryStage.show();
 }


 private void showEncryptionKeyDialog(Stage primaryStage) {
     // Create a TextInputDialog for entering the encryption key
     TextInputDialog dialog = new TextInputDialog();
     dialog.setTitle("Enter Encryption Key");
     dialog.setHeaderText("Enter the encryption key you received after successful login:");
     dialog.setContentText("Encryption Key:");

     // Set the owner window to the current active window
     dialog.initOwner(Platform.isFxApplicationThread() ? primaryStage : null);

     // Set the modality to APPLICATION_MODAL to block user input to other windows
     dialog.initModality(Modality.APPLICATION_MODAL);

     // Set the style to DECORATED to include the window decorations provided by the platform
     dialog.initStyle(StageStyle.DECORATED);

     // Get the TextField of the dialog
     TextField textField = dialog.getEditor();

     // Enable the TextField
     textField.setDisable(false);

     // Show the dialog and wait for user input
     dialog.showAndWait().ifPresent(this::validateEncryptionKey);
 }

 // Check if the entered key matches the generated key
 private String enteredEncryptionKey; // Add this variable at the top of your class

 private void validateEncryptionKey(String enteredKey) {
     // Store the entered key in the class variable
     enteredEncryptionKey = enteredKey;

     // Check if the entered key matches the generated key
     if (enteredKey.equals(ssoController.getSecretKey())) {
         showAlert("Key Validation", "Encryption Key is valid!", Alert.AlertType.INFORMATION);
         // Enable the "Copy Key" button
         copyKeyButton.setDisable(false);

         // Display the image and welcome message for the signed-on user
         displayUserImageAndWelcomeMessage();
     } else {
         showAlert("Key Validation", "Invalid Encryption Key. Please try again.", Alert.AlertType.ERROR);
         // Disable the "Copy Key" button
         copyKeyButton.setDisable(true);
     }
 }

 private void displayUserImageAndWelcomeMessage() {
     Stage userImageAndWelcomeStage = new Stage();
     userImageAndWelcomeStage.setTitle("Welcome!");

     // Find the signed-on user's account
     String signedOnUser = ssoController.getSignedOnUser(); // Add this method to SSOController
     AccountInfo signedOnAccount = null;
     for (AccountInfo account : ssoController.accounts) {
         if (signedOnUser.equals(account.getUsername())) {
             signedOnAccount = account;
             break;
         }
     }

     if (signedOnAccount != null) {
         Image userImage = new Image(signedOnAccount.getImageUrl());
         ImageView userImageView = new ImageView(userImage);
         Label welcomeLabel = new Label("Hello, " + signedOnUser + "!");
         VBox vbox = new VBox(10);
         vbox.setAlignment(Pos.CENTER);
         vbox.getChildren().addAll(userImageView, welcomeLabel);

         // Set the scene
         userImageAndWelcomeStage.setScene(new Scene(vbox));
         userImageAndWelcomeStage.show();
     } else {
         // Handle the case where the signed-on user's account is not found
         System.err.println("Account not found for signed-on user: " + signedOnUser);
     }
 }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


//https://www.pngarts.com/files/10/Default-Profile-Picture-Download-PNG-Image.png - // default iamge


    private void handleLogin(String username, String password, ImageView userImageView) {
        String encryptedToken = ssoController.login(username, password);

        if (encryptedToken != null) {
            // Set the correct key in the encryptionKeyField
            encryptionKeyField.setText(ssoController.getSecretKey());

            // Retrieve the imageUrl for the logged-in user
            String imageUrl = ssoController.getImageUrl(username);

            // Display the user image in the same pane
            displayUserImageInPane(imageUrl, userImageView);

            // Set the flag to true when a successful login occurs
            keyDisplayed = true;

            // Enable the "Copy Key" button
            copyKeyButton.setDisable(false);

            // Display the welcome message
            showWelcomeMessage(username);

        } else {
            // Increment login attempts
            loginAttempts++;

            if (loginAttempts < 3) {
                messageLabel.setText("Invalid credentials. Please try again. Attempts left: " + (3 - loginAttempts));
            } else {
                Platform.runLater(() -> {
                    messageLabel.setText("Maximum login attempts reached. Closing in 10 seconds.");
                    showAlert("Auto-Closing", "Maximum login attempts reached. Closing in 10 seconds.", Alert.AlertType.INFORMATION);
                });

                // Schedule a task to close the application after 10 seconds
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                scheduler.schedule(() -> Platform.exit(), 10, TimeUnit.SECONDS);
                scheduler.shutdown();
            }

            // Display an error message for invalid username or password
            showAlert("Login Error", "Invalid username or password. Please try again.", Alert.AlertType.ERROR);
        }
        
        
    }

    private void showWelcomeMessage(String username) {
        welcomeLabel.setText("Hello, " + username + "! Welcome!"); // Set the welcome message
    }
    	
    	
    	
    private void displayUserImageInPane(String imageUrl, ImageView userImageView) {
        // Load user image
        Image userImage = new Image(imageUrl);
        userImageView.setImage(userImage); // Update the user image
        // Additional code to handle the rest of the UI updates
    }
    
    // welcome message with kitty waving
    
   // https://i.pinimg.com/originals/c1/1b/ab/c11babf074dd0adc406cd7f49ab43dfb.gif // cite me!
    
    /**
     * Controller for Single Sign-On operations.
     */
    public static class SSOController {

        // Define multiple accounts with different images
        private final AccountInfo[] accounts = {
                new AccountInfo("user1", "pass1", "https://fanaru.com/chowder/image/229615-chowder-chowder-with-moustache.jpg"), // chowder
                new AccountInfo("user2", "pass2", "https://img1.wikia.nocookie.net/__cb20130308233520/bravestwarriors/images/4/49/BW_-_Catbug_(Ep)_40.png"), // catbug
                new AccountInfo("user3", "pass3", "https://i.pinimg.com/originals/80/1c/1c/801c1c5bf0e37e62952555a216d6f77b.png"), // garfeild - remember to cite sources for these images you dont own them
                new AccountInfo("user4", "pass4", "https://i.scdn.co/image/ab67706c0000bebb3bad4c5d2a6bc979975406be") // jfk
        };

        // Common image URL for all users
        private final String commonImageUrl = "https://i.pinimg.com/originals/c1/1b/ab/c11babf074dd0adc406cd7f49ab43dfb.gif";
        private String signedOnUser;

        // Generate a random Triple DES key
        private final String secretKey = generateRandomKey();

        public String getImageUrl(String username) {
            // Return the common image URL for all users
            return commonImageUrl;
        }

        public String getSignedOnUser() {
            return signedOnUser;
        }

        /**
         * Simulates user login and generates an encrypted token.
         *
         * @param user The provided username.
         * @param pass The provided password.
         * @return Encrypted token if login is successful, otherwise null.
         */
        public String login(String user, String pass) {
            for (AccountInfo account : accounts) {
                if (user.equals(account.getUsername()) && pass.equals(account.getPassword())) {
                    // Generate a token for the authenticated user
                    String token = generateToken(user);
                    // Encrypt the token using Triple DES
                    String encryptedToken = encrypt(token, secretKey);

                    // Set the signed-on user
                    signedOnUser = user;

                    return encryptedToken;
                }
            }
            return null; // Invalid credentials
        }

        public String getSecretKey() {
            return secretKey;
        }
        
        /**
         * Generates a simple token based on the username and current timestamp.
         *
         * @param username The username for which the token is generated.
         * @return The generated token.
         */
        public String generateToken(String username) {
            String secretKey = getSecretKey();  // Get the actual secret key
            return "User:" + username + ":Timestamp:" + System.currentTimeMillis() + ":Key:" + secretKey;
        }

        /**
         * Encrypts the given plain text using the secret key.
         *
         * @param plainText The text to be encrypted.
         * @return The Base64-encoded encrypted text.
         */
        public String encryptToken(String plainText) {
            return encrypt(plainText, getSecretKey());
        }
        /**
         * Triple DES encryption of a given plain text using a secret key.
         *
         * @param plainText The text to be encrypted.
         * @param key The secret key used for encryption.
         * @return The Base64-encoded encrypted text.
         */
        private String encrypt(String plainText, String key) {
            try {
                DESedeKeySpec keySpec = new DESedeKeySpec(Base64.getDecoder().decode(key));
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
                SecretKey secretKey = keyFactory.generateSecret(keySpec);

                Cipher cipher = Cipher.getInstance("DESede");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);

                // Encrypt the plain text
                byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
                // Encode the encrypted bytes to Base64 for better representation
                return Base64.getEncoder().encodeToString(encryptedBytes);
            } catch (Exception e) {
                // Print an error message if encryption fails
                System.err.println("Error during encryption: " + e.getMessage());
                return null;
            }
        }

        /**
         * Generates a random Triple DES key.
         *
         * @return The Base64-encoded random key.
         */
        private String generateRandomKey() {
            try {
                SecureRandom secureRandom = new SecureRandom();
                byte[] keyBytes = new byte[24]; // Use 24 bytes for 192-bit key
                secureRandom.nextBytes(keyBytes);
                // Set parity bits for each 8-byte block
                for (int i = 0; i < 24; i += 8) {
                    keyBytes[i + 7] = keyBytes[i + 6] = (byte) ((keyBytes[i] & 0xFE) >>> 1);
                }
                return Base64.getEncoder().encodeToString(keyBytes);
            } catch (Exception e) {
                System.err.println("Error generating random key: " + e.getMessage());
                return null;
            } 
        }
    }
    private void copyEncryptionKeyToClipboard() {
        String encryptionKey = encryptionKeyField.getText();
        if (encryptionKey != null && !encryptionKey.isEmpty()) {
            ClipboardContent content = new ClipboardContent();
            content.putString(encryptionKey);
            Clipboard.getSystemClipboard().setContent(content);
            showAlert("Key Copied", "Encryption Key has been copied to the clipboard.", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Copy Key Error", "Encryption Key is empty.", Alert.AlertType.ERROR);
        }
    }




    /**
     * Class to represent account information.
     */
    private static class AccountInfo {
        private final String username;
        private final String password;
        private final String imageUrl;

        public AccountInfo(String username, String password, String imageUrl) {
            this.username = username;
            this.password = password;
            this.imageUrl = imageUrl;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }

    }



