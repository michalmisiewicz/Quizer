package proz.misiewicz.elkowanie;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.io.IOException;

/**
 * Custom listView cell for admin pane
 */
public class ResultsListViewCell extends ListCell<Game>
{
    @FXML
    private GridPane gridPane;

    @FXML
    private Label hostUsername;

    @FXML
    private Label hostScore;

    @FXML
    private Label guestUsername;

    @FXML
    private Label guestScore;

    @Override
    protected void updateItem(Game item, boolean empty)
    {
        super.updateItem(item, empty);

        if(empty || item == null)
        {
            setText(null);
            setGraphic(null);
        }
        else
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("results_row.fxml"));
            loader.setController(this);

            try
            {
                loader.load();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            hostUsername.setText(item.getHostUsername());
            hostScore.setText(String.valueOf(item.getHostScore()));
            guestUsername.setText(item.getGuestUsername());
            guestScore.setText(String.valueOf(item.getGuestScore()));

            if(item.getHostScore() > item.getGuestScore())
            {
                hostScore.setTextFill(Color.web("#76ff03"));
                guestScore.setTextFill(Color.web("#ff1a1a"));
            }
            else if(item.getHostScore() < item.getGuestScore())
            {
                hostScore.setTextFill(Color.web("#ff1a1a"));
                guestScore.setTextFill(Color.web("#76ff03"));
            }
            else
            {
                hostScore.setTextFill(Color.web("#007acc"));
                guestScore.setTextFill(Color.web("#007acc"));
            }

            setGraphic(gridPane);
            setText(null);
        }
    }
}
