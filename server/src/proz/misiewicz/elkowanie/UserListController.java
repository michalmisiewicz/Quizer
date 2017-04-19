package proz.misiewicz.elkowanie;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * User list controller
 */
public class UserListController implements Initializable
{
    @FXML
    private TableView userTable;

    @FXML
    private TableColumn usernameColumn;

    @FXML
    private TableColumn passwordColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        DBHandler db = DBHandler.getInstance();
        List<UserInfo> users = db.getUsersInfo();

        usernameColumn.prefWidthProperty().bind(userTable.widthProperty().multiply(0.5));
        passwordColumn.prefWidthProperty().bind(userTable.widthProperty().multiply(0.49));

        usernameColumn.setCellValueFactory(new PropertyValueFactory<UserInfo, String>("username"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<UserInfo, String>("password"));

        userTable.setItems(FXCollections.observableArrayList(users));
    }
}
