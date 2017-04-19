package android.proz.misiewicz.elkowanie;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Game Activity
 */
public class GameActivity extends AppCompatActivity
{
    /**
     * Game animator
     */
    private ObjectAnimator animator;

    /**
     * question text view
     */
    private TextView questionTextView;

    /**
     * Question list
     */
    private ArrayList<Question> questions;

    /**
     * Question duration
     */
    private final int duration = 10000;

    private int questionCounter;
    /**
     * Game ID
     */
    private int gameId;

    private int questionsNumber;

    /**
     * Map of button answer binding
     */
    private Map<Button, Answer> answerMap;

    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;

    /**
     * Round results
     */
    private int[] results;

    /**
     * Server connection reference
     */
    private ServerConnection serverConnection;

    private int resultSend;

    /**
     * Button color
     */
    private int buttonColor;

    /**
     * Question board
     */
    private RelativeLayout board;

    /**
     * onCreate method. Setts activity layout
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
    }

    /**
     * onStart method. Initializing UI
     */
    @Override
    protected void onStart()
    {
        super.onStart();
        serverConnection = ServerConnection.getInstance();
        questionTextView = (TextView) findViewById(R.id.question_place);
        board = (RelativeLayout) findViewById(R.id.board);

        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);

        button1.setEnabled(false);
        button2.setEnabled(false);
        button3.setEnabled(false);
        button4.setEnabled(false);

        buttonColor = ContextCompat.getColor(this, R.color.secondaryColor);
        answerMap = new HashMap<>();
        questions = (ArrayList<Question>) getIntent().getSerializableExtra(getString(R.string.question));
        gameId = getIntent().getExtras().getInt(getString(R.string.game_id));
        questionsNumber = questions.size();
        results = new int[questionsNumber];
        resultSend = 0;

        questionCounter = 0;

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        animator = ObjectAnimator.ofInt(progressBar, "progress", 0, 1000);
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
                if(questionCounter - 1 < questionsNumber)
                {
                    questionTextView.setText(questions.get(questionCounter - 1).getQuestion());


                    List<Answer> answers = questions.get(questionCounter - 1).getAnswers();
                    Collections.shuffle(answers);

                    if(!answerMap.isEmpty()) answerMap.clear();

                    button1.setBackgroundColor(buttonColor);
                    button2.setBackgroundColor(buttonColor);
                    button3.setBackgroundColor(buttonColor);
                    button4.setBackgroundColor(buttonColor);

                    button1.setEnabled(true);
                    button2.setEnabled(true);
                    button3.setEnabled(true);
                    button4.setEnabled(true);

                    board.setClickable(false);

                    Answer answer1 = answers.get(0);
                    Answer answer2 = answers.get(1);
                    Answer answer3 = answers.get(2);
                    Answer answer4 = answers.get(3);

                    answerMap.put(button1, answer1);
                    answerMap.put(button2, answer2);
                    answerMap.put(button3, answer3);
                    answerMap.put(button4, answer4);

                    button1.setText(answer1.toString());
                    button2.setText(answer2.toString());
                    button3.setText(answer3.toString());
                    button4.setText(answer4.toString());
                }

            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                button1.setEnabled(false);
                button2.setEnabled(false);
                button3.setEnabled(false);
                button4.setEnabled(false);

                board.setClickable(true);

                Iterator iterator = answerMap.entrySet().iterator();
                while (iterator.hasNext())
                {
                    Map.Entry pair = (Map.Entry) iterator.next();
                    Answer answer = (Answer) pair.getValue();
                    if(answer.isCorrect())
                    {
                        Button button = (Button) pair.getKey();
                        button.setBackgroundColor(Color.GREEN);
                    }
                    results[questionCounter-1] = 0;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
                button1.setEnabled(false);
                button2.setEnabled(false);
                button3.setEnabled(false);
                button4.setEnabled(false);


                board.setClickable(true);
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {

            }
        });
    }

    /**
     * Start game round
     * @param view
     */
    public void startAnimation(View view)
    {
        if(questionCounter > 0 && questionCounter % 3 == 0)
        {
            Thread t = new Thread(() ->
            {
                String message = gameId + ":" + results[resultSend] + ":" + results[resultSend + 1] + ":" + results[resultSend + 2];
                resultSend += 3;
                if(resultSend == questionsNumber) message += ":" + "e";
                serverConnection.sendUpdate(message);
            });
            t.start();
        }
        if(questionCounter < questionsNumber)
        {
            questionCounter++;
            animator.start();
        }
        else
        {
            Intent i = new Intent();
            i.putExtra(getString(R.string.results), results);
            setResult(Activity.RESULT_OK, i);
            finish();
        }

    }

    /**
     * Submit round answer
     * @param view
     */
    public void submitAnswer(View view)
    {
        animator.cancel();
        boolean isCorrect = false;

        switch (view.getId())
        {
            case R.id.button1:
            {
                Answer answer = answerMap.get(button1);
                if (answer.isCorrect())
                {
                    button1.setBackgroundColor(Color.GREEN);
                    isCorrect = true;
                    results[questionCounter-1] = 1;
                }
                else
                {
                    button1.setBackgroundColor(Color.RED);
                    results[questionCounter-1] = 0;
                }
                break;
            }

            case R.id.button2:
            {
                Answer answer = answerMap.get(button2);
                if (answer.isCorrect())
                {
                    button2.setBackgroundColor(Color.GREEN);
                    isCorrect = true;
                    results[questionCounter-1] = 1;
                }
                else
                {
                    button2.setBackgroundColor(Color.RED);
                    results[questionCounter-1] = 0;
                }
                break;
            }
            case R.id.button3:
            {
                Answer answer = answerMap.get(button3);
                if (answer.isCorrect())
                {
                    button3.setBackgroundColor(Color.GREEN);
                    isCorrect = true;
                    results[questionCounter-1] = 1;
                }
                else
                {
                    button3.setBackgroundColor(Color.RED);
                    results[questionCounter-1] = 0;
                }
                break;
            }
            case R.id.button4:
            {
                Answer answer = answerMap.get(button4);
                if (answer.isCorrect())
                {
                    button4.setBackgroundColor(Color.GREEN);
                    isCorrect = true;
                    results[questionCounter-1] = 1;
                }
                else
                {
                    button4.setBackgroundColor(Color.RED);
                    results[questionCounter-1] = 0;
                }
                break;
            }
        }

        if(!isCorrect)
        {
            Iterator iterator = answerMap.entrySet().iterator();
            while (iterator.hasNext())
            {
                Map.Entry pair = (Map.Entry) iterator.next();
                Answer answer = (Answer) pair.getValue();
                if(answer.isCorrect())
                {
                    Button button = (Button) pair.getKey();
                    button.setBackgroundColor(Color.GREEN);
                }
            }
        }
    }

    @Override
    public void onBackPressed()
    {
    }
}
