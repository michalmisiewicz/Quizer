package proz.misiewicz.elkowanie;

import java.util.ArrayList;
import java.util.List;

/**
 * Question model
 */
public class Question
{
    /**
     * question content
     */
    private String question;

    /**
     * question answers
     */
    private List<String > answers = new ArrayList<>();

    /**
     * Creates new question model
     * @param question question
     * @param correctAnswer correct answer
     * @param badAnswer1 bad answer
     * @param badAnswer2 bad answer
     * @param badAnswer3 bad answer
     */
    public Question(String question, String correctAnswer, String badAnswer1, String badAnswer2, String badAnswer3)
    {
        this.question = question;
        this.answers.add(correctAnswer);
        this.answers.add(badAnswer1);
        this.answers.add(badAnswer2);
        this.answers.add(badAnswer3);
    }

    /**
     * Return question
     * @return question
     */
    public String getQuestion()
    {
        return question;
    }

    /**
     * Return question answers
     * @return question answers
     */
    public List<String> getAnswers()
    {
        return answers;
    }

    @Override
    public String toString()
    {
        return question;
    }

}
