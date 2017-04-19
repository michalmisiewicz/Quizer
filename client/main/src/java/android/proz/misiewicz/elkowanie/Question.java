package android.proz.misiewicz.elkowanie;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Question model class
 */
public class Question implements Serializable
{
    /**
     * SUID
     */
    private static final long serialVersionUID = 6015438663729955476L;

    /**
     * Question ID
     */
    private int id;

    /**
     * Question content
     */
    private String question;

    /**
     * Question answers
     */
    private List<Answer> answers = new ArrayList<>();

    /**
     * Creates new question
     * @param id question ID
     * @param question question content
     * @param correctAnswer correct answer
     * @param badAnswer1 bad answer
     * @param badAnswer2 bad answer
     * @param badAnswer3 bad answer
     */
    public Question(int id, String question, String correctAnswer, String badAnswer1, String badAnswer2, String badAnswer3)
    {
        this.id = id;
        this.question = question;
        this.answers.add(new Answer(correctAnswer, true));
        this.answers.add(new Answer(badAnswer1));
        this.answers.add(new Answer(badAnswer2));
        this.answers.add(new Answer(badAnswer3));
    }

    /**
     * Return question content
     * @return question content
     */
    public String getQuestion()
    {
        return question;
    }


    /**
     * Return question answers
     * @return question answers
     */
    public List<Answer> getAnswers()
    {
        return answers;
    }

    @Override
    public String toString()
    {
        return id + " " + question + " " + answers;
    }

}
