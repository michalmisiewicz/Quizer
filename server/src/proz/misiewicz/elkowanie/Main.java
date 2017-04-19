package proz.misiewicz.elkowanie;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Application main class
 */
public class Main extends Application
{
    /**
     * Application loading method
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.setTitle("Quizer serwer");
        primaryStage.setScene(new Scene(root, 1400, 800));
        primaryStage.show();
    }

    /**
     * Resources release before application stop working.
     * @throws Exception
     */
    @Override
    public void stop() throws Exception
    {
        super.stop();
        ServerHandler.getInstance().stopServer();
        DBHandler.getInstance().closeConnection();
    }

    /**
     * main function
     * @param args
     */
    public static void main(String[] args)
    {
        launch(args);
    }
}
