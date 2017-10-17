import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StockAnalyzer extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Login/login.fxml"));
        Parent root = (Parent) fxmlLoader.load();
        primaryStage.setTitle("Stock Analyzer");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        scene.getStylesheets().add(StockAnalyzer.class.getResource("Login/login.css").toExternalForm());

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
