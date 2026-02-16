package com.recruitx.hrone;

import com.recruitx.hrone.utils.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.sql.Connection;

public class HelloController {

    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        try {
            Connection connection = DBConnection.getInstance();

            if (connection != null && !connection.isClosed()) {
                welcomeText.setText("✅ Connected to MySQL successfully");
            } else {
                welcomeText.setText("⚠️ Connection is null or closed");
            }

        } catch (Exception e) {
            welcomeText.setText("❌ Database connection failed");
            e.printStackTrace(); // visible in terminal for debugging
        }
    }
}
