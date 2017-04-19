package proz.misiewicz.elkowanie;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.io.IOException;

/**
 * User listView custom cell
 */
public class UserListViewCell extends ListCell<User>
{
    @FXML
    private GridPane gridPane;

    @FXML
    private Label username;

    @FXML
    private Label status;

    @Override
    protected void updateItem(User item, boolean empty)
    {
        super.updateItem(item, empty);

        if(empty || item == null)
        {
            setText(null);
            setGraphic(null);
        }
        else
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("user_row.fxml"));
            loader.setController(this);

            try
            {
                loader.load();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            username.setText(item.getUsername());

            if(item.getBusy() == true)
            {
                status.setText("Zajęty");
                status.setTextFill(Color.web("#ff1a1a"));
            }
            else
            {
                status.setText("Wolny");
                status.setTextFill(Color.web("#76ff03"));
            }

            setGraphic(gridPane);
            setText(null);
        }
    }
}
