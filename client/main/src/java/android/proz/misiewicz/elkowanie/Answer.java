package android.proz.misiewicz.elkowanie;

import java.io.Serializable;

/**
 * Model of Question answer
 */

public class Answer implements Serializable
{
    /**
     * SUID
     */
    private static final long serialVersionUID = 2925247705091907488L;

    private String answer;
    private Boolean isCorrect;

    /**
     * Creates new answer
     * @param answer answer
     */
    public Answer(String answer)
    {
        this.answer = answer;
        this.isCorrect = false;
    }

    /**
     * Creates new answer
     * @param answer answer
     * @param isCorrect is answer correct
     */
    public Answer(String answer, Boolean isCorrect)
    {
        this.answer = answer;
        this.isCorrect = isCorrect;
    }

    /**
     * Chceck if answer is correct
     * @return true when answer is correct
     */
    public Boolean isCorrect()
    {
        return isCorrect;
    }

    @Override
    public String toString()
    {
        return answer;
    }
}
