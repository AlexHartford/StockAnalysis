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

    private boolean stockValuesNeedCalculating;
    private boolean portfolioValueNeedsCalculating;
    private boolean portfolioReturnNeedsCalculating;
    private boolean spyValueNeedsCalculating;

    private double portfolioValue = 1.00;

    private int numRows = 250;

    private ArrayList<String> stockSymbols;
    private String CELG = "CELG";
    private String FB = "FB";
    private String GOOG = "GOOG";
    private String NVDA = "NVDA";

    private String SPY = "SPY";

    private ArrayList<Double> portfolioAllocations;
    private double CELGweight = 0.3;
    private double FBweight = 0.3;
    private double GOOGweight = 0.2;
    private double NVDAweight = 0.2;

    private double SPYweight = 1.0;

    public void initialize() {
        setUp();
    }

    /**
     * Upon pressing the calculate button, ensure input is valid and grab the appropriate data from the DB.
     */
    @FXML
    private void getData() {

        calculateStockValues(portfolioAllocations);
        calculatePortfolioValue();
        calculatePortfolioReturn();
//        sharpen();
        sharpest();
        double cumulativeReturn = calculateCumulativeReturn();
        portfolioLabel.setText(String.valueOf(cumulativeReturn));
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
            errorMsg.setText("");
            return true;
        }
        errorMsg.setText("Invalid Start or End Date!");
        return false;
    }

    /**
     * Calculates the value for each stock and updates the portfolio accordingly.
     * Only need to run this once each time you adjust your portfolio.
     * TODO: Add radio button or toggle switch to GUI
     */
    private void calculateStockValues(ArrayList<Double> portfolioAllocations) {
        if (parseDates() && stockValuesNeedCalculating) {
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

    private double calculateAverage() {

        double average = 0;
        if (parseDates()) {

            ResultSet results = null;
            Statement statement = null;
            String query = "select * from portfolio WHERE Date >= '" + startDate + "' AND Date <= '" + endDate + "'";
            try {
                statement = connection.createStatement();
                results = statement.executeQuery(query);
                ResultSetMetaData resultSetMetaData = (ResultSetMetaData) results
                        .getMetaData();

                while (results.next()) {
                    average += (results.getFloat("PortfolioCumulativeReturn") - results.getFloat("SPYcumulativeReturn")) / numRows;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return average;
    }

    private double calculateStandardDeviation(double average) {
        double standardDeviation = 0;
        double sum = 0;

        if (parseDates()) {

            ResultSet results = null;
            Statement statement = null;
            String query = "select * from portfolio WHERE Date >= '" + startDate + "' AND Date <= '" + endDate + "'";
            try {
                statement = connection.createStatement();
                results = statement.executeQuery(query);
                ResultSetMetaData resultSetMetaData = (ResultSetMetaData) results
                        .getMetaData();

                while (results.next()) {
                    sum += Math.pow(Math.abs((results.getFloat("PortfolioCumulativeReturn") - results.getFloat("SPYcumulativeReturn")) - average), 2);
                }
                standardDeviation = Math.sqrt(sum / numRows);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return standardDeviation;
    }

    /**
     * Calculate the Sharpe ratio
     */
    private double sharpen(double average, double standardDeviation) {
//        double average = calculateAverage();
//        double standardDeviation = calculateStandardDeviation(average);
        double sharpe = Math.sqrt(numRows) * average / standardDeviation;
//        double cumulativeReturn = calculateCumulativeReturn();
        sharpeLabel.setText(String.valueOf(sharpe));
        dailyAvgLabel.setText(String.valueOf(average));
        dailyStdDevLabel.setText(String.valueOf(standardDeviation));
        return sharpe;
    }

    private double magic(ArrayList<Double> allocations) {

        calculateStockValues(allocations);
        calculatePortfolioValue();
        calculatePortfolioReturn();
        double average = calculateAverage();
        double standardDeviation = calculateStandardDeviation(average);
        return sharpen(average, standardDeviation);
    }

    private void sharpest() {
        double sharpestRatio = 0;
        ArrayList<Double> allocations = new ArrayList<>();
        ArrayList<Double> bestAllocations = new ArrayList<>();

        for (int i = 0; i <= 10; i += 1) {
            for (int j = 0; j <= 10; j += 1) {
                for (int k = 0; k <= 10; k += 1) {
                    for (int l = 0; l <= 10; l += 1) {
                        if (i + j + k + l == 10) {
//                            System.out.println(i / 10.0 + " " + j / 10.0 + " " + k / 10.0 + " " + l / 10.0);
                            allocations.clear();
                            allocations.add(i / 10.0);
                            allocations.add(j / 10.0);
                            allocations.add(k / 10.0);
                            allocations.add(l / 10.0);
                            double sharpe = magic(allocations);
                            if (sharpe > sharpestRatio) {
                                sharpestRatio = sharpe;
                                bestAllocations.clear();
                                bestAllocations.addAll(allocations);
                            }
                        }
                    }
                }
            }
        }
        System.out.println("THE SHARPEST RATIO IS!!!!! " + sharpestRatio);
        for (double d : bestAllocations) {
            System.out.print(d + " ");
        }
    }

    private double calculateCumulativeReturn() {
        double cumulativeReturn = 0;

        if (parseDates()) {

            ResultSet results = null;
            Statement statement = null;
            String query = "select * from portfolio WHERE Date = '2016-10-06' OR Date = '2017-10-03'";
            try {
                statement = connection.createStatement();
                results = statement.executeQuery(query);
                ResultSetMetaData resultSetMetaData = (ResultSetMetaData) results
                        .getMetaData();
                results.next();
                double first = results.getFloat("PortfolioValue");
                results.next();
                double last = results.getFloat("PortfolioValue");

                cumulativeReturn = (last - first) / first;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return cumulativeReturn;
    }

    /**
     * Calculates the portfolio's value by day
     * Only need to run this once each time you adjust your portfolio.
     * TODO: Add radio button or toggle switch to GUI
     */
    private void calculatePortfolioValue() {
        if (parseDates() && portfolioValueNeedsCalculating) {

            String updateQuery = "UPDATE portfolio SET portfolioValue = "
                    + CELG + "value + " + FB + "value + " + GOOG + "value + " + NVDA + "value"
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

    /**
     * Calculates the portfolio's return by day
     * TODO: Add radio button or toggle switch to GUI
     */
    private void calculatePortfolioReturn() {
        if (parseDates() && portfolioReturnNeedsCalculating) {

            String updateQuery = "UPDATE portfolio SET portfolioCumulativeReturn = "
                    + "portfolioValue / " + portfolioValue
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

        stockValuesNeedCalculating = true;
        portfolioValueNeedsCalculating = true;
        portfolioReturnNeedsCalculating = true;
        spyValueNeedsCalculating = false;

        if (spyValueNeedsCalculating) {
            stockSymbols.add(SPY);
            portfolioAllocations.add(SPYweight);
        }

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
