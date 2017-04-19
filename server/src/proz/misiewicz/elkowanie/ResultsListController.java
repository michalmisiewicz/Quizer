package proz.misiewicz.elkowanie;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Result list controller
 */
public class ResultsListController implements Initializable
{
    @FXML
    private ListView resultsListView;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        DBHandler db = DBHandler.getInstance();
        resultsListView.setItems(FXCollections.observableArrayList(db.getResults()));
        resultsListView.setCellFactory(t -> new GamesResultsCell());
    }
}
