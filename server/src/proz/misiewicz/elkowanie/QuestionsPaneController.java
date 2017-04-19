package proz.misiewicz.elkowanie;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Question pane controller
 */
public class QuestionsPaneController implements Initializable
{
    @FXML
    private ListView questionsList;

    @FXML
    private Label rightAnswer;

    @FXML
    private Label badAnswer1;

    @FXML
    private Label badAnswer2;

    @FXML
    private Label badAnswer3;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        DBHandler db = DBHandler.getInstance();
        List<Question> questions = db.getQuestions();
        questionsList.setItems(FXCollections.observableArrayList(questions));

        questionsList.setOnMouseClicked(e ->
        {
            Question question = (Question) questionsList.getSelectionModel().getSelectedItem();
            List<String> answers = question.getAnswers();
            rightAnswer.setText(answers.get(0));
            badAnswer1.setText(answers.get(1));
            badAnswer2.setText(answers.get(2));
            badAnswer3.setText(answers.get(3));
        });
    }
}
