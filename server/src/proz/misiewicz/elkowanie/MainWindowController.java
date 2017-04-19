package proz.misiewicz.elkowanie;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Main window controller
 */
public class MainWindowController implements Initializable
{
    @FXML
    private GridPane contentGrid;

    @FXML
    private AnchorPane temp;

    @FXML
    private Button startServerButton;

    @FXML
    private Button adminButton;

    @FXML
    private Button questionsButton;

    @FXML
    private Button userListButton;

    @FXML
    private Button resultsButton;

    private FXMLLoader loader;

    /**
     * Server reference
     */
    private ServerHandler server;

    private AnchorPane anchorPane = new AnchorPane();


    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        DBHandler db = DBHandler.getInstance();
        db.loadDBDriver();
        db.openConnection();
        loader = new FXMLLoader();
        server = ServerHandler.getInstance();
        Image image = new Image(getClass().getResourceAsStream("res/power-button.png"));
        startServerButton.setGraphic(new ImageView(image));
        image = new Image(getClass().getResourceAsStream("res/admin.png"));
        adminButton.setGraphic(new ImageView(image));
        image = new Image(getClass().getResourceAsStream("res/question.png"));
        questionsButton.setGraphic(new ImageView(image));
        image = new Image(getClass().getResourceAsStream("res/users.png"));
        userListButton.setGraphic(new ImageView(image));
        image = new Image(getClass().getResourceAsStream("res/score.png"));
        resultsButton.setGraphic(new ImageView(image));

        try
        {
            anchorPane = loader.load(getClass().getResource("admin_panel.fxml"));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        contentGrid.getChildren().remove(temp);
        contentGrid.add(anchorPane, 1, 0);
    }

    /**
     * Switch state of server.
     * @param e
     */
    @FXML
    private void startServer(ActionEvent e)
    {
        if(!server.isRunning())
        {
            Thread t = new Thread(() -> server.startServer());
            t.start();
            startServerButton.setStyle("-fx-background-color: #76ff03;");
            startServerButton.setText("Włączony");
        }
        else
        {
            Thread t = new Thread(() -> server.stopServer());
            t.start();
            startServerButton.setStyle("-fx-background-color: #ff1a1a;");
            startServerButton.setText("Wyłączony");
        }
    }

    /**
     * Loads admin pane
     * @param event
     */
    public void adminPane(ActionEvent event)
    {
        contentGrid.getChildren().remove(anchorPane);
        try
        {
            anchorPane = loader.load(getClass().getResource("admin_panel.fxml"));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        contentGrid.add(anchorPane, 1, 0);
    }

    /**
     * Loads questions pane
     * @param event
     */
    public void questionsPane(ActionEvent event)
    {
        contentGrid.getChildren().remove(anchorPane);
        try
        {
            anchorPane = loader.load(getClass().getResource("questions_pane.fxml"));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        contentGrid.add(anchorPane, 1, 0);
    }

    /**
     * Loads user list pane
     * @param event
     */
    public void userListPane(ActionEvent event)
    {
        contentGrid.getChildren().remove(anchorPane);
        try
        {
            anchorPane = loader.load(getClass().getResource("userlist_pane.fxml"));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        contentGrid.add(anchorPane, 1, 0);
    }

    /**
     * loads results pane
     * @param event
     */
    public void resultsPane(ActionEvent event)
    {
        contentGrid.getChildren().remove(anchorPane);
        try
        {
            anchorPane = loader.load(getClass().getResource("resultslist_pane.fxml"));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        contentGrid.add(anchorPane, 1, 0);
    }

}
