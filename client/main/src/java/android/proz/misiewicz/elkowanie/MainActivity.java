package android.proz.misiewicz.elkowanie;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Login Activity
 */
public class MainActivity extends AppCompatActivity
{
    /**
     * Register request code
     */
    private static final int REGISTER = 1234;

    /**
     * Server connection reference
     */
    private ServerConnection serverConnection;

    /**
     * Error TextView
     */
    private TextView error;

    /**
     * onStart method. Initializing ui
     */
    @Override
    protected void onStart()
    {
        super.onStart();
        serverConnection = ServerConnection.getInstance();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String serverAddress = sharedPref.getString(getString(R.string.pref_addr_key), "");
        serverConnection.setServerAddress(serverAddress);
        
        error = (TextView) findViewById(R.id.error_message);
        if(!isNetworkAvailable())
        {
            error.setText(getString(R.string.no_connection));
            error.setVisibility(View.VISIBLE);
        }
        else error.setVisibility(View.INVISIBLE);
    }

    /**
     * onCreate. Setts activity layout
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        EditText loginEditText = (EditText) findViewById(R.id.login_field);
        EditText passwordEditText = (EditText) findViewById(R.id.password_field);
        loginEditText.setText("");
        passwordEditText.setText("");
    }

    /**
     * back button onClick update
     */
    @Override
    public void onBackPressed()
    {
        if(serverConnection.isConnected()) serverConnection.stopConnection();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == R.id.server_address_settings)
        {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Gets results of startActivityForResults
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == REGISTER)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                Toast toast = Toast.makeText(this, getString(R.string.successful_register), Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    /**
     * Login attempt
     * @param view
     */
    public void login(View view)
    {
        if(isNetworkAvailable())
        {
           new LoginTask().execute();
        }
        else
        {
            error.setText(getString(R.string.no_connection));
            error.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Navigate to register Activity
     * @param view
     */
    public void registerPage(View view)
    {
        if(isNetworkAvailable())
        {
            Intent i = new Intent(this, RegisterActivity.class);
            startActivityForResult(i, REGISTER);
        }
        else
        {
            error.setText(getString(R.string.no_connection));
            error.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Check if network is available
     * @return true if network is available, otherwise false
     */
    private boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    /**
     * Login attemp AsyncTask
     */
    private class LoginTask extends AsyncTask<Void, Void, Boolean>
    {
        private ProgressDialog dialog;
        private EditText loginEditText;
        private EditText passwordEditText;
        private TextView error;

        private String login;
        private String password;
        private boolean successfulLogin = false;

        /**
         * UI init
         */
        @Override
        protected void onPreExecute()
        {
            loginEditText = (EditText) findViewById(R.id.login_field);
            passwordEditText = (EditText) findViewById(R.id.password_field);
            error = (TextView) findViewById(R.id.error_message);

            login = loginEditText.getText().toString();
            password = passwordEditText.getText().toString();

            passwordEditText.setError(null);
            loginEditText.setError(null);

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

            showLoadingDialog();
        }

        /**
         * attempts server log in
         * @param params
         * @return
         */
        @Override
        protected Boolean doInBackground(Void... params)
        {
            if(!serverConnection.isConnected()) serverConnection.startConnection();
            if (serverConnection.isConnected())
            {
                if (serverConnection.logIn(login, password))
                {
                    successfulLogin = true;
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
                if(successfulLogin)
                {
                    Intent i = new Intent(MainActivity.this, MenuActivity.class);
                    startActivity(i);
                }
                else
                {
                    error.setText(getString(R.string.bad_login));
                    error.setVisibility(View.VISIBLE);
                }
            }
            else
            {
                Toast toast = Toast.makeText(MainActivity.this, getString(R.string.server_unreachable), Toast.LENGTH_LONG);
                toast.show();
            }
        }

        /**
         * Shows loading dialog
         */
        private void showLoadingDialog()
        {
            dialog =  new ProgressDialog(MainActivity.this);
            dialog.setTitle(getString(R.string.login_title));
            dialog.setMessage(getString(R.string.login_message));
            dialog.setCancelable(false);
            dialog.show();
        }
    }


}
