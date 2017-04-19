package android.proz.misiewicz.elkowanie;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Menu Activity class
 */
public class MenuActivity extends AppCompatActivity
{
    /**
     * server connection reference
     */
    private ServerConnection serverConnection;

    /**
     * message reader state
     */
    private boolean isListening;

    /**
     * message reader
     */
    private Callable<Boolean> reader;

    /**
     * Result of message reader
     */
    private Future<Boolean> result;

    /**
     * Contains information about user choice
     */
    private boolean accept;

    /**
     * Progress dialog
     */
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        serverConnection = ServerConnection.getInstance();
        TextView userName = (TextView) findViewById(R.id.username_textview);
        userName.setText(serverConnection.getActiveUser().getLogin());
        accept = false;
        dialog = new ProgressDialog(this);
        reader = () ->
        {
            isListening = true;
            while (isListening)
            {
                try
                {
                    Message message = serverConnection.readMessage();
                    if (message != null)
                    {
                        if (message.isInvitation())
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    showInvitationDialog(message.getMessage());
                                }
                            });
                        } else if (message.isInvitationAcceptance())
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    startGame(message.getMessage());
                                }
                            });
                            accept = true;
                        } else if (message.isInvitationRejection())
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    makeToast(message.getMessage());
                                }
                            });
                        }
                        else if(message.isNoOpponentMessage())
                        {
                            runOnUiThread(() ->
                            {
                                Toast toast = Toast.makeText(MenuActivity.this, getString(R.string.no_user), Toast.LENGTH_LONG);
                                toast.show();
                            });
                        }
                    }
                } catch (PollBlockingQueueTimeout e) { if(accept) isListening = false; }
                catch (InterruptedException e) { e.printStackTrace(); }
            }
            if(!accept)return false;
            else return true;
        };

        if(serverConnection.hasQuestions())
        {
            if (!serverConnection.isListening())
            {
                serverConnection.startMessagesListening();
            }
            result = serverConnection.submitMessagesReader(reader);
        }
        else
        {
            DownloadQuestions downloadQuestions = new DownloadQuestions();
            downloadQuestions.execute();
        }
    }

    @Override
    public void onBackPressed()
    {
        serverConnection.stopListening();
        serverConnection.stopConnection();
        isListening = false;
        super.onBackPressed();
    }

    /**
     * Sends Game request
     * @param view
     */
    public void newGame(View view)
    {
        serverConnection.sendNewGameRequest();
    }

    /**
     * Makes reject toast
     * @param username user name
     */
    private void makeToast(String username)
    {
        Toast toast = Toast.makeText(this, "U\u017Cytkownik " + username + " odrzuci\u0142 twoje zaproszenie", Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * Show invitation dialog
     * @param username user name
     */
    private void showInvitationDialog(String username)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.invitation_dialog_title));
        builder.setMessage(username + " " + getString(R.string.invite_message));
        builder.setPositiveButton(R.string.accept_invitation, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                serverConnection.acceptInvitation(username);
                accept = true;
                startGame(username);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                serverConnection.rejectInvitation(username);
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Start new game
     * @param username opponent user name
     */
    private void startGame(String username)
    {
        boolean startGame = false;

        while (!result.isDone()) continue;
        try
        {
            startGame = result.get();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } catch (ExecutionException e)
        {
            e.printStackTrace();
        }

        if(startGame)
        {
            Intent i = new Intent(this, GameRoomActivity.class);
            i.putExtra(getString(R.string.opponent_username), username);
            i.putExtra(getString(R.string.is_new_game), true);
            startActivity(i);
        }
    }

    /**
     * Async Task used to download questions
     */
    private class DownloadQuestions extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected void onPreExecute()
        {
            showLoadingDialog(dialog);
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            try
            {
                if (serverConnection.fetchQuestions()) return true;
                else return false;
            } catch (IOException e)
            {
                e.printStackTrace();
                System.exit(0);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean)
        {
            accept = false;
            if(aBoolean == true)
            {
                dialog.cancel();
                if (!serverConnection.isListening()) serverConnection.startMessagesListening();
                result = serverConnection.submitMessagesReader(reader);
            }
        }

        /**
         * Shows loading dialog
         * @param dialog
         */
        private void showLoadingDialog(ProgressDialog dialog)
        {
            dialog.setTitle(getString(R.string.question_download));
            dialog.setMessage(getString(R.string.wait_message));
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    /**
     * Log out
     * @param view view
     */
    public void exit(View view)
    {
        serverConnection.stopListening();
        serverConnection.stopConnection();
        isListening = false;
        finish();
    }

    /**
     * About button handler
     * @param view view
     */
    public void about(View view)
    {
        showAboutDialog();
    }

    /**
     * Show About dialog
     */
    private void showAboutDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.about_dialog_title));
        TextView msg = new TextView(this);
        msg.setText(getString(R.string.app_name) + "\n" + "Wersja: 1.0\n" + "Autor: Micha\u0142 Misiewicz");
        msg.setPadding(10, 10, 10, 10);
        msg.setGravity(Gravity.CENTER);
        msg.setTextSize(18);
        builder.setView(msg);

        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Result button handler
     * @param view
     */
    public void getResults(View view)
    {
        new FetchResults().execute();
    }

    /**
     * AsyncTask used to fetch results from server.
     */
    public class FetchResults extends AsyncTask<Void, Void, Boolean>
    {
        private List<GameResult> list;

        @Override
        protected void onPreExecute()
        {
            accept = true;
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            while (!result.isDone()) continue;
            try
            {
                result.get();
                serverConnection.sendResultRequest();

                boolean isDone = false;
                while (!isDone)
                {
                    try
                    {
                        Message message = serverConnection.readMessage();
                        if (message != null)
                        {
                            if (message.isGameResults())
                            {
                                String msg = message.getMessage();
                                list = new ArrayList<>();

                                String[] results = msg.split(":");

                                if(results.length == 1 && results[0].isEmpty()) return false;

                                for(String row : results)
                                {
                                    if(row.isEmpty()) continue;

                                    String[] fields = row.split("#");
                                    GameResult gameResult = new GameResult(
                                            fields[0],
                                            fields[1],
                                            Integer.valueOf(fields[2]),
                                            Integer.valueOf(fields[3]));


                                    list.add(gameResult);
                                }
                                isDone = true;
                            }
                        }
                    } catch (PollBlockingQueueTimeout e) {  }
                    catch (InterruptedException e) { e.printStackTrace(); }
                }
                return true;
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            } catch (ExecutionException e)
            {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean)
        {
            if(aBoolean)
            {
                showResultsDialog();
            }
            else
            {
                Toast toast = Toast.makeText(MenuActivity.this, getString(R.string.no_results), Toast.LENGTH_LONG);
                toast.show();
            }

            accept = false;
            if (!serverConnection.isListening())
            {
                serverConnection.startMessagesListening();
            }
            result = serverConnection.submitMessagesReader(reader);
        }

        /**
         * Shows results dialog
         */
        private void showResultsDialog()
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
            builder.setTitle(getString(R.string.resultsTitle));

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MenuActivity.this, android.R.layout.select_dialog_singlechoice);
            for(GameResult gameResult: list)
                arrayAdapter.add(gameResult.toString());

            builder.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                }
            });
            builder.show();
        }
    }

}
