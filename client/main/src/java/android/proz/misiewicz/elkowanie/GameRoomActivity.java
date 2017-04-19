package android.proz.misiewicz.elkowanie;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Game Room Activity
 */
public class GameRoomActivity extends AppCompatActivity
{
    /**
     * Game request code
     */
    private static final int REQUEST_CODE = 1234;

    private static final String PLAYER_RESULT = "player_result";
    private static final String OPPONENT_RESULT = "opponent_result";

    private ServerConnection serverConnection = ServerConnection.getInstance();

    private Button playButton;

    /**
     * Message reader state
     */
    private boolean isListening;

    /**
     * Questions list
     */
    private ArrayList<Question> questions;

    /**
     * Game ID
     */
    private int gameId;

    /**
     * Player results
     */
    private ArrayList<int[]> playerResults;

    /**
     * Opponent results
     */
    private ArrayList<int[]> opponentResults;

    private TextView plQue1;
    private TextView plQue2;
    private TextView plQue3;
    private TextView plQue4;
    private TextView plQue5;
    private TextView plQue6;
    private TextView plQue7;
    private TextView plQue8;
    private TextView plQue9;
    private TextView plRou1;
    private TextView plRou2;
    private TextView plRou3;
    private TextView plScore;

    private TextView opQue1;
    private TextView opQue2;
    private TextView opQue3;
    private TextView opQue4;
    private TextView opQue5;
    private TextView opQue6;
    private TextView opQue7;
    private TextView opQue8;
    private TextView opQue9;
    private TextView opRou1;
    private TextView opRou2;
    private TextView opRou3;
    private TextView opScore;

    /**
     * onCreate. Setts activity layout
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_room);

        if(!getIntent().hasExtra(getString(R.string.is_new_game)))
        {
            if (savedInstanceState != null)
            {
                playerResults = (ArrayList<int[]>) savedInstanceState.getSerializable(PLAYER_RESULT);
                opponentResults = (ArrayList<int[]>) savedInstanceState.getSerializable(OPPONENT_RESULT);
            }
        }
        else
        {
            playerResults = new ArrayList<>();
            opponentResults = new ArrayList<>();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putSerializable(PLAYER_RESULT, playerResults);
        outState.putSerializable(OPPONENT_RESULT, opponentResults);
    }

    /**
     * onStart method. Initializing UI
     */
    @Override
    protected void onStart()
    {
        super.onStart();

        TextView opponent = (TextView) findViewById(R.id.opponent_username);
        TextView player = (TextView) findViewById(R.id.player_username);

        plQue1 = (TextView) findViewById(R.id.que_pl_1);
        plQue2 = (TextView) findViewById(R.id.que_pl_2);
        plQue3 = (TextView) findViewById(R.id.que_pl_3);
        plQue4 = (TextView) findViewById(R.id.que_pl_4);
        plQue5 = (TextView) findViewById(R.id.que_pl_5);
        plQue6 = (TextView) findViewById(R.id.que_pl_6);
        plQue7 = (TextView) findViewById(R.id.que_pl_7);
        plQue8 = (TextView) findViewById(R.id.que_pl_8);
        plQue9 = (TextView) findViewById(R.id.que_pl_9);

        opQue1 = (TextView) findViewById(R.id.que_op_1);
        opQue2 = (TextView) findViewById(R.id.que_op_2);
        opQue3 = (TextView) findViewById(R.id.que_op_3);
        opQue4 = (TextView) findViewById(R.id.que_op_4);
        opQue5 = (TextView) findViewById(R.id.que_op_5);
        opQue6 = (TextView) findViewById(R.id.que_op_6);
        opQue7 = (TextView) findViewById(R.id.que_op_7);
        opQue8 = (TextView) findViewById(R.id.que_op_8);
        opQue9 = (TextView) findViewById(R.id.que_op_9);

        plRou1 = (TextView) findViewById(R.id.pl_r1_score);
        plRou2 = (TextView) findViewById(R.id.pl_r2_score);
        plRou3 = (TextView) findViewById(R.id.pl_r3_score);

        opRou1 = (TextView) findViewById(R.id.op_r1_score);
        opRou2 = (TextView) findViewById(R.id.op_r2_score);
        opRou3 = (TextView) findViewById(R.id.op_r3_score);

        plScore = (TextView) findViewById(R.id.player_score);
        opScore = (TextView) findViewById(R.id.opponent_score);

        playButton = (Button) findViewById(R.id.play_button);

        String opponentName = getIntent().getExtras().getString(getString(R.string.opponent_username));
        User activeUser = serverConnection.getActiveUser();

        playButton.setBackgroundColor(Color.GRAY);
        opponent.setText(opponentName);
        player.setText(activeUser.getLogin());

        playButton.setEnabled(false);
        playButton.setText(R.string.wait_button);

        questions = new ArrayList<>();

        Callable<Boolean> reader = () ->
        {
            isListening = true;

            while (isListening)
            {
                try
                {
                    Message message = serverConnection.readMessage();
                    if (message != null)
                    {
                        if (message.isPlayerTurn())
                        {
                            String[] data = message.getMessage().split(":");
                            if (data.length == 4)
                            {
                                gameId = Integer.valueOf(data[0]);
                                for (int i = 1; i < data.length; i++)
                                    questions.add(serverConnection.getQuestion(Integer.valueOf(data[i]) - 1));
                            }
                            else if(data.length == 7 || data.length == 10)
                            {
                                gameId = Integer.valueOf(data[0]);
                                int[] opp_res = new int[3];
                                opp_res[0] = Integer.valueOf(data[1]);
                                opp_res[1] = Integer.valueOf(data[2]);
                                opp_res[2] = Integer.valueOf(data[3]);
                                opponentResults.add(opp_res);
                                for (int i = 4; i < data.length; i++)
                                    questions.add(serverConnection.getQuestion(Integer.valueOf(data[i]) - 1));
                            }

                            runOnUiThread(() ->
                            {
                                updateUI();
                                playButton.setEnabled(true);
                                playButton.setBackgroundColor(Color.GREEN);
                                playButton.setText(R.string.play);
                            });
                        }
                        else if (message.isOpponentUpdate())
                        {
                            String[] results = message.getMessage().split(":");
                            if(results.length == 3)
                            {
                                int[] opp_res = new int[3];
                                opp_res[0] = Integer.valueOf(results[0]);
                                opp_res[1] = Integer.valueOf(results[1]);
                                opp_res[2] = Integer.valueOf(results[2]);
                                opponentResults.add(opp_res);
                                runOnUiThread(() ->
                                {
                                    updateUI();
                                });
                            }
                        }
                    }
                } catch (PollBlockingQueueTimeout e) {  }
                catch (InterruptedException e) { e.printStackTrace(); }
            }
            return false;
        };
        serverConnection.submitMessagesReader(reader);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        updateUI();
    }

    /**
     * Update UI
     */
    private void updateUI()
    {
        int red = ContextCompat.getColor(this, R.color.red);
        int green = ContextCompat.getColor(this, R.color.green);

        if(playerResults.size() == 3)
        {
            int[] firstRound = playerResults.get(0);
            int round1 = firstRound[0] + firstRound[1] + firstRound[2];
            plRou1.setText(String.valueOf(round1));
            plQue1.setBackgroundColor(firstRound[0] == 1 ? green: red);
            plQue2.setBackgroundColor(firstRound[1] == 1 ? green: red);
            plQue3.setBackgroundColor(firstRound[2] == 1 ? green: red);

            int[] secondRound = playerResults.get(1);
            int round2 = secondRound[0] + secondRound[1] + secondRound[2];
            plRou2.setText(String.valueOf(round2));
            plQue4.setBackgroundColor(secondRound[0] == 1 ? green: red);
            plQue5.setBackgroundColor(secondRound[1] == 1 ? green: red);
            plQue6.setBackgroundColor(secondRound[2] == 1 ? green: red);

            int[] thirdRound = playerResults.get(2);
            int round3 = thirdRound[0] + thirdRound[1] + thirdRound[2];
            plRou3.setText(String.valueOf(round3));
            plQue7.setBackgroundColor(thirdRound[0] == 1 ? green: red);
            plQue8.setBackgroundColor(thirdRound[1] == 1 ? green: red);
            plQue9.setBackgroundColor(thirdRound[2] == 1 ? green: red);

            plScore.setText(String.valueOf(round1 + round2 + round3));
        }
        else if (playerResults.size() == 2)
        {
            int[] firstRound = playerResults.get(0);
            int round1 = firstRound[0] + firstRound[1] + firstRound[2];
            plRou1.setText(String.valueOf(round1));
            plQue1.setBackgroundColor(firstRound[0] == 1 ? green: red);
            plQue2.setBackgroundColor(firstRound[1] == 1 ? green: red);
            plQue3.setBackgroundColor(firstRound[2] == 1 ? green: red);

            int[] secondRound = playerResults.get(1);
            int round2 = secondRound[0] + secondRound[1] + secondRound[2];
            plRou2.setText(String.valueOf(round2));
            plQue4.setBackgroundColor(secondRound[0] == 1 ? green: red);
            plQue5.setBackgroundColor(secondRound[1] == 1 ? green: red);
            plQue6.setBackgroundColor(secondRound[2] == 1 ? green: red);

            plScore.setText(String.valueOf(round1 + round2));
        }
        else if(playerResults.size() == 1)
        {
            int[] firstRound = playerResults.get(0);
            int round1 = firstRound[0] + firstRound[1] + firstRound[2];
            plRou1.setText(String.valueOf(round1));
            plQue1.setBackgroundColor(firstRound[0] == 1 ? green: red);
            plQue2.setBackgroundColor(firstRound[1] == 1 ? green: red);
            plQue3.setBackgroundColor(firstRound[2] == 1 ? green: red);
            plScore.setText(String.valueOf(round1));
        }

        if(opponentResults.size() == 3)
        {
            int[] firstRound = opponentResults.get(0);
            int round1 = firstRound[0] + firstRound[1] + firstRound[2];
            opRou1.setText(String.valueOf(round1));
            opQue1.setBackgroundColor(firstRound[0] == 1 ? green: red);
            opQue2.setBackgroundColor(firstRound[1] == 1 ? green: red);
            opQue3.setBackgroundColor(firstRound[2] == 1 ? green: red);

            int[] secondRound = opponentResults.get(1);
            int round2 = secondRound[0] + secondRound[1] + secondRound[2];
            opRou2.setText(String.valueOf(round2));
            opQue4.setBackgroundColor(secondRound[0] == 1 ? green: red);
            opQue5.setBackgroundColor(secondRound[1] == 1 ? green: red);
            opQue6.setBackgroundColor(secondRound[2] == 1 ? green: red);

            int[] thirdRound = opponentResults.get(2);
            int round3 = thirdRound[0] + thirdRound[1] + thirdRound[2];
            opRou1.setText(String.valueOf(round3));
            opQue7.setBackgroundColor(thirdRound[0] == 1 ? green: red);
            opQue8.setBackgroundColor(thirdRound[1] == 1 ? green: red);
            opQue9.setBackgroundColor(thirdRound[2] == 1 ? green: red);

            opScore.setText(String.valueOf(round1 + round2 + round3));
        }
        else if(opponentResults.size() == 2)
        {
            int[] firstRound = opponentResults.get(0);
            int round1 = firstRound[0] + firstRound[1] + firstRound[2];
            opRou1.setText(String.valueOf(round1));
            opQue1.setBackgroundColor(firstRound[0] == 1 ? green: red);
            opQue2.setBackgroundColor(firstRound[1] == 1 ? green: red);
            opQue3.setBackgroundColor(firstRound[2] == 1 ? green: red);

            int[] secondRound = opponentResults.get(1);
            int round2 = secondRound[0] + secondRound[1] + secondRound[2];
            opRou2.setText(String.valueOf(round2));
            opQue4.setBackgroundColor(secondRound[0] == 1 ? green: red);
            opQue5.setBackgroundColor(secondRound[1] == 1 ? green: red);
            opQue6.setBackgroundColor(secondRound[2] == 1 ? green: red);

            opScore.setText(String.valueOf(round1 + round2));
        }
        else if(opponentResults.size() == 1)
        {
            int[] firstRound = opponentResults.get(0);
            int round1 = firstRound[0] + firstRound[1] + firstRound[2];
            opRou1.setText(String.valueOf(round1));
            opQue1.setBackgroundColor(firstRound[0] == 1 ? green: red);
            opQue2.setBackgroundColor(firstRound[1] == 1 ? green: red);
            opQue3.setBackgroundColor(firstRound[2] == 1 ? green: red);

            opScore.setText(String.valueOf(round1));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                int[] results = data.getIntArrayExtra(getString(R.string.results));

                if(results.length == 6)
                {
                    int[] tab1 = { results[0], results[1], results[2]};
                    int[] tab2 = { results[3], results[4], results[5]};
                    playerResults.add(tab1);
                    playerResults.add(tab2);
                }
                else playerResults.add(results);
            }
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        stopListening();
    }

    /**
     * Stop messages reading
     */
    private void stopListening() { isListening = false; }

    /**
     * Start new round
     * @param view view
     */
    public void startRound(View view)
    {
        stopListening();
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra(getString(R.string.game_id), gameId);
        i.putExtra(getString(R.string.question), questions);

        startActivityForResult(i, REQUEST_CODE);
    }

    @Override
    public void onBackPressed()
    {
        serverConnection.sendGameRoomExitNotification();
        super.onBackPressed();
    }
}
