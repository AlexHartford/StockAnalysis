package Analyzer;

import Login.LoginController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * @author Alex Hartford
 */
public class AnalyzerController {

    @FXML private Label errorMsg;
    @FXML private TextField endDateField;
    @FXML private TextField startDateField;
    @FXML private Button calcButton;
    @FXML private Label dailyStdDevLabel;
    @FXML private Label dailyAvgLabel;
    @FXML private Label sharpeLabel;
    @FXML private Label portfolioLabel;

    private static Connection connection;

    private Date startDate, endDate;

    private double portfolioValue = 1.00;

    private ArrayList<String> stockSymbols;
    private String CELG = "CELG";
    private String FB = "FB";
    private String GOOG = "GOOG";
    private String NVDA = "NVDA";

    private ArrayList<Double> portfolioAllocations;
    private double CELGweight = 0.3;
    private double FBweight = 0.3;
    private double GOOGweight = 0.2;
    private double NVDAweight = 0.2;

    public void initialize() {
        setUp();
    }

    /**
     * Calculate the Sharpe ratio
     */
    private void sharpen() {
//        E[Ra - Rb] / sqroot(var[Ra-Rb])
        float assetReturn;
        float benchmarkAssetReturn;
        float mean;
        float stdDev;
    }

    /**
     * Upon pressing the calculate button, ensure input is valid and grab the appropriate data from the DB.
     */
    @FXML
    private void getData() {
        if (parseDates()) {

            ResultSet results = null;
            Statement statement = null;
            String query = "select * from portfolio";
            try {
                statement = connection.createStatement();
                results = statement.executeQuery(query);
                ResultSetMetaData resultSetMetaData = (ResultSetMetaData) results
                        .getMetaData();

//                while (results.next()) {
//                    System.out.print(results.getDate("date"));
//                    System.out.print(" | ");
//                    System.out.println(results.getFloat("CELGcumulativeReturn"));
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            errorMsg.setText("");
        } else {
            errorMsg.setText("Invalid Start or End Date!");
        }
    }

    /**
     * Uses regex to ensure the user enters the date properly.
     * @return true if the user has entered a proper date string, else false
     */
    private boolean parseDates() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String pattern = ("\\d{4}-\\d{2}-\\d{2}");

        String startDateString = "";
        String endDateString = "";

        if (!startDateField.getText().isEmpty() && !endDateField.getText().isEmpty()) {
            startDateString = startDateField.getText();
            endDateString = endDateField.getText();
        }

        if (startDateString.matches(pattern) && endDateString.matches(pattern)) {
            java.util.Date utilStartDate = null;
            java.util.Date utilEndDate = null;
            try {
                utilStartDate = sdf.parse(startDateString);
                utilEndDate = sdf.parse(endDateString);
                startDate = new java.sql.Date(utilStartDate.getTime());
                endDate = new java.sql.Date(utilEndDate.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    /**
     * Calculates the value for each stock and updates the portfolio accordingly.
     */
    private void calculateStockValues() {
        if (parseDates()) {
            for (int i = 0; i < stockSymbols.size(); i++) {
                String updateQuery = "UPDATE portfolio SET "
                        + stockSymbols.get(i) + "value = "
                        + portfolioAllocations.get(i) + " * " + portfolioValue + " * " + stockSymbols.get(i) + "cumulativeReturn"
                        + " WHERE Date >= '" + startDate + "' AND Date <= '" + endDate + "'";
                Statement statement = null;
                try {
                    statement = connection.createStatement();
                    statement.executeUpdate(updateQuery);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Any overhead that needs to be initialized.
     */
    private void setUp() {
        stockSymbols = new ArrayList<>();
        stockSymbols.add(CELG);
        stockSymbols.add(FB);
        stockSymbols.add(GOOG);
        stockSymbols.add(NVDA);

        portfolioAllocations = new ArrayList<>();
        portfolioAllocations.add(CELGweight);
        portfolioAllocations.add(FBweight);
        portfolioAllocations.add(GOOGweight);
        portfolioAllocations.add(NVDAweight);
    }

    /**
     * Allows the login controller to share the DB connection with the analysis tool.
     * Upon disconnection, will set the connection to null.
     * @param connection - the MySQL DB object
     */
    public static void setConnection(Connection connection) {
        AnalyzerController.connection = connection;
    }
}
