package Login;

import Analyzer.AnalyzerController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LoginController {

    @FXML private TabPane tabPane;
    @FXML private Tab analyze;
    @FXML private Tab connect;
    @FXML private Label notification;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    private String dbdriver = "com.mysql.jdbc.Driver";
    private String dburl = "jdbc:mysql://localhost";
    private String dbname = "data_analytics_2017";

    private Connection connection;

    public void initialize() {
        setUp();
    }

    /**
     * Load the JDBC driver and connect to the database.
     */
    @FXML
    private void connectDB() {

        boolean connected = true;

        notification.setText("");
        notification.setTextFill(Color.BLUE);

        if (usernameField.getText().length() != 0 && passwordField.getText().length() != 0) {
            try {
                Class.forName(dbdriver);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                System.exit(0);
            }

            try {
                connection = DriverManager.getConnection((dburl + "/" + dbname),
                        usernameField.getText(), passwordField.getText());
                connection.setClientInfo("autoReconnect", "true");
            } catch (SQLException e) {
                connected = false;
                notification.setTextFill(Color.RED);
                notification.setText("Invalid username or password!");
                analyze.setDisable(true);
                tabPane.getSelectionModel().select(connect);
            }
            if (connected) {
                notification.setTextFill(Color.GREEN);
                notification.setText("Connected!");
                loginButton.setText("Disconnect!");
                loginButton.setOnAction(ae -> closeConnection());
                openAnalyzer();
            }
        } else notification.setText("Please enter MySQL credentials.");
    }

    /**
     * Any overhead that needs to be initialized, such as constraints on text fields.
     */
    private void setUp() {
        passwordField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                if (passwordField.getText().length() > 15) {
                    passwordField.setText(passwordField.getText().substring(0, 15));
                }
            }
        });

        passwordField.setOnKeyPressed(ke -> {
            if (ke.getCode() == KeyCode.ENTER) {
                connectDB();
            }
        });

        /* Only allows input up to 15 characters */
        usernameField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                if (usernameField.getText().length() > 15) {
                    usernameField.setText(usernameField.getText().substring(0, 15));
                }
            }
        });
    }

    /**
     * Closes the MySQL connection and displays an appropriate message
     */
    private void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        notification.setTextFill(Color.RED);
        notification.setText("Disconnected!");
        loginButton.setText("Connect to DB!");
        loginButton.setOnAction(ae -> connectDB());
        analyze.setDisable(true);
        AnalyzerController.setConnection(null);
        tabPane.getSelectionModel().select(connect);
    }

    /**
     * Enables the analysis tab and selects it for convenience
     */
    private void openAnalyzer() {
        analyze.setDisable(false);
        tabPane.getSelectionModel().select(analyze);
        AnalyzerController.setConnection(connection);
    }
}
