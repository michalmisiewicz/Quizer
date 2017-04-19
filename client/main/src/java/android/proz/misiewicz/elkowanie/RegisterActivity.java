package android.proz.misiewicz.elkowanie;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Register Activity Class
 */
public class RegisterActivity extends AppCompatActivity
{
    /**
     * Server Connection reference
     */
    private ServerConnection serverConnection;

    private EditText loginEditText;
    private EditText passwordEditText;
    private EditText passwordRepeatEditText;

    /**
     * onCreate. Setts activity layout
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        serverConnection = ServerConnection.getInstance();

        loginEditText = (EditText) findViewById(R.id.login_register_field);
        passwordEditText = (EditText) findViewById(R.id.password_register_field);
        passwordRepeatEditText = (EditText) findViewById(R.id.repeat_password_field);
    }

    /**
     * Register button onClick method
     * @param view view
     */
    public void register(View view)
    {
        new RegisterTask().execute();
    }

    /**
     * AsyncTask use to register new user
     */
    private class RegisterTask extends AsyncTask<Void, Void, Boolean>
    {
        private ProgressDialog dialog;

        private String login;
        private String password;
        private String passwordRepeat;
        private boolean successfulRegister = false;

        /**
         * initializing components
         */
        @Override
        protected void onPreExecute()
        {
            login = loginEditText.getText().toString();
            password = passwordEditText.getText().toString();
            passwordRepeat = passwordRepeatEditText.getText().toString();


            loginEditText.setError(null);
            passwordRepeatEditText.setError(null);
            passwordEditText.setError(null);

            if(login.isEmpty())
            {
                cancel(true);
                loginEditText.setError(getString(R.string.empty_error));
                return;
            }
            if(password.isEmpty())
            {
                cancel(true);
                passwordEditText.setError(getString(R.string.empty_error));
                return;
            }
            if(passwordRepeat.isEmpty())
            {
                cancel(true);
                passwordRepeatEditText.setError(getString(R.string.empty_error));
                return;
            }
            if(!password.equals(passwordRepeat))
            {
                cancel(true);
                passwordRepeatEditText.setError(getString(R.string.diffrent_password));
                return;
            }
            showLoadingDialog();
        }

        /**
         * attempt to register user
         * @param params
         * @return
         */
        @Override
        protected Boolean doInBackground(Void... params)
        {

            if(!serverConnection.isConnected()) serverConnection.startConnection();
            if (serverConnection.isConnected())
            {
                if (serverConnection.registerUser(login, password))
                {
                    successfulRegister = true;
                }
                return true;
            }
            else return false;
        }

        /**
         * getting task result
         * @param aBoolean
         */
        @Override
        protected void onPostExecute(Boolean aBoolean)
        {
            dialog.cancel();
            if(aBoolean == true)
            {
                if(successfulRegister)
                {
                    Intent i = new Intent();
                    i.putExtra(getString(R.string.registerResult), true);
                    setResult(RESULT_OK, i);
                    finish();
                }
                else
                {
                    loginEditText.setError(getString(R.string.username_taken));
                }
            }
            else
            {
                Toast toast = Toast.makeText(RegisterActivity.this, getString(R.string.server_unreachable), Toast.LENGTH_LONG);
                toast.show();
            }
        }

        /**
         * Shows loading dialog
         */
        private void showLoadingDialog()
        {
            dialog =  new ProgressDialog(RegisterActivity.this);
            dialog.setTitle(getString(R.string.login_title));
            dialog.setMessage(getString(R.string.register_message));
            dialog.setCancelable(false);
            dialog.show();
        }
    }
}
