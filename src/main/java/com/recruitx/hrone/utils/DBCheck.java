package com.recruitx.hrone.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class DBCheck {
    public static void main(String[] args) {
        try {
            Connection cnx = DBConnection.getInstance();
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM activite LIMIT 1");
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            System.out.println("--- Table structure for 'activite' ---");
            for (int i = 1; i <= columnCount; i++) {
                System.out.println("Column " + i + ": " + rsmd.getColumnName(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
