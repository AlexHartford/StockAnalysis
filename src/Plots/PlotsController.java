package Plots;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;

/**
 * @author Alex Hartford
 */
public class PlotsController {

    @FXML private static LineChart lineChart;
    @FXML private AnchorPane mainPane;

    public void initialize() {

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Day #");
        yAxis.setLabel("Daily Return");
        lineChart = new LineChart<Number,Number>(xAxis,yAxis);
        lineChart.setPrefSize(1200, 800);
        lineChart.setTitle("Cumulative Returns");
        lineChart.setLegendVisible(true);

        mainPane.getChildren().add(lineChart);
    }

    public static void drawPlots(ArrayList<ArrayList<Double>> plots, ArrayList<String> symbols) {
        int j = 0;
        for (ArrayList<Double> plot : plots) {
            XYChart.Series series = new XYChart.Series();
            series.setName(symbols.get(j));
            for (int i = 0; i < plot.size(); i++) {
                series.getData().add(new XYChart.Data(i, plot.get(i)));
            }
            lineChart.getData().add(series);
            j++;
        }
    }
}
