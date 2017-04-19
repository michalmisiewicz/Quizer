package proz.misiewicz.elkowanie;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.IOException;

/**
 * This class handle server work. Singleton pattern
 */
public class ServerHandler
{
    /**
     * ServerHandler reference
     */
    private static ServerHandler server = null;

    /**
     * Server state
     */
    private boolean isRunning = false;

    /**
     * ServerThread reference
     */
    private ServerThread thread;

    /**
     * Observable Users list
     */
    private ObservableList<User> userList;

    /**
     * Observable Games list
     */
    private ObservableList<Game> gameList;

    /**
     * Observable console messages list
     */
    private SimpleListProperty<String> console;

    /**
     * console ListChangeListener
     */
    private ListChangeListener<String> listener;

    /**
     * Creates new ServerHandler
     */
    private ServerHandler()
    {
        gameList = FXCollections.observableArrayList(game ->
                new Observable[]{game.hostScoreProperty(), game.guestScoreProperty()}
        );

        userList = FXCollections.observableArrayList(user ->
                new Observable[]{user.busyProperty()}
        );

        console = new SimpleListProperty<>(FXCollections.observableArrayList());
    }

    /**
     * Get ServerHandler reference. Singleton pattern
     * @return ServerHandler reference
     */
    public static ServerHandler getInstance()
    {
        if(server == null)
        {
            server = new ServerHandler();
        }
        return server;
    }

    /**
     * Start server
     */
    public void startServer()
    {
        if(!isRunning)
        {
            gameList.clear();
            userList.clear();

            thread = new ServerThread(userList, gameList);
            thread.start();
            isRunning = true;
        }

    }

    /**
     * Stop server
     */
    public void stopServer()
    {
        if(isRunning)
        {
            try
            {
                thread.shutdownThread();
                thread.join();
                isRunning = false;
                gameList.clear();
                userList.clear();
            }
            catch (InterruptedException e) { e.printStackTrace();}
            catch (IOException e) { e.printStackTrace(); }
        }
    }


    public ObservableList<String> getConsole()
    {
        return console.get();
    }

    public SimpleListProperty<String> consoleProperty()
    {
        return console;
    }

    /**
     * Add new console message
     * @param message message
     */
    public void addConsoleMessage(String message)
    {
        console.get().add(message);
    }


    public ObservableList<User> getUserList()
    {
        return userList;
    }


    public ObservableList<Game> getGameList()
    {
        return gameList;
    }

    /**
     * add new online user
     * @param user new user
     */
    public void addUser(User user)
    {
        Platform.runLater(() -> userList.add(user));
    }

    /**
     * remove offline user
     * @param user offline user
     */
    public void removeUser(User user)
    {
        Platform.runLater(() -> userList.remove(user));
    }

    /**
     * add new online game
     * @param game new game
     */
    public void addGame(Game game)
    {
        Platform.runLater(() -> gameList.add(game));
    }

    /**
     * Remove offline game
     * @param game offline game
     */
    public void removeGame(Game game)
    {
        Platform.runLater(() -> gameList.remove(game));
    }

    public void setBusy(User user, boolean value)
    {
        Platform.runLater(() -> user.setBusy(value));
    }

    /**
     * Update game results
     * @param game game
     * @param user user
     * @param score score
     */
    public void updateResults(Game game, User user, int score)
    {
        Platform.runLater(() -> game.updateResults(user, score));
    }

    /**
     * Returns server state
     * @return server state
     */
    public boolean isRunning()
    {
        return isRunning;
    }

    /**
     * Sets console message listener
     * @param listChangeListener message listener
     */
    public void setConsoleMessageListner(ListChangeListener<String> listChangeListener)
    {
        if(listener == null)
        {
            listener = listChangeListener;
            getConsole().addListener(listener);
        }
        else
        {
            getConsole().removeListener(listener);
            listener = listChangeListener;
            getConsole().addListener(listener);
        }
    }

    /**
     * Check if user is online
     * @param username user username
     * @return true if user is online, otherwise false
     */
    public boolean isOnlineUser(String username)
    {
        for(User user: userList)
        {
            if(user.getUsername().equals(username)) return true;
        }
        return false;
    }
}
