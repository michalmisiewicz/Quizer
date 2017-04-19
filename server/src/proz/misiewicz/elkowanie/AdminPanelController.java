package proz.misiewicz.elkowanie;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Admin pane controller
 */
public class AdminPanelController implements Initializable
{
    @FXML
    private TextArea console;
    @FXML
    private ListView activeUserList;
    @FXML
    private ListView activeGameList;

    private ServerHandler server;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        server = ServerHandler.getInstance();

        activeUserList.setItems(server.getUserList());
        activeUserList.setCellFactory(t -> new UserListViewCell());
        activeGameList.setItems(server.getGameList());
        activeGameList.setCellFactory(t -> new ResultsListViewCell());

        ObservableList<String> consoleMessages = server.getConsole();
        if(!consoleMessages.isEmpty())
            for(String message: consoleMessages) console.appendText(message + "\n");

        server.setConsoleMessageListner((ListChangeListener<String>) c ->
        {
            Platform.runLater(() ->
            {
                while (c.next())
                {
                    if (c.wasAdded())
                    {
                        List<? extends String> list = c.getAddedSubList();

                        for (String message : list)
                            console.appendText(message + "\n");

                    }
                }
            });
        });
    }
}
