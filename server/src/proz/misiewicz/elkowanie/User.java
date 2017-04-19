package proz.misiewicz.elkowanie;

import javafx.beans.property.SimpleBooleanProperty;
import java.nio.channels.SocketChannel;

/**
 * Model of server user
 */
public class User
{
    /**
     * User ID
     */
    private final int id;
    /**
     *  User name
     */
    private String username;
    /**
     * User password
     */
    private String password;
    /**
     * user server state
     */
    private SimpleBooleanProperty busy;
    /**
     * channel to user
     */
    private SocketChannel socketChannel;

    /**
     * Creates new user
     * @param id user ID
     * @param username user name
     * @param password user password
     * @param channel user socket channel used by server to communication
     */
    public User(int id, String username, String password, SocketChannel channel)
    {
        this.id = id;
        this.username = username;
        this.password = password;
        this.socketChannel = channel;
        this.busy = new SimpleBooleanProperty(false);
    }

    /**
     * Gets the value of user ID
     * @return user ID
     */
    public int getId()
    {
        return id;
    }

    /**
     * Returns true if SocketChannel is bind to user
     * @param channel SocketChannel
     * @return true if channel belongs to user
     */
    public boolean isUserSocketChannel(SocketChannel channel)
    {
        return socketChannel == channel;
    }

    /**
     * Gets user channel
     * @return User channel
     */
    public SocketChannel getSocketChannel()
    {
        return socketChannel;
    }

    /**
     * Gets user name
     * @return User name
     */
    public String getUsername()
    {
        return username;
    }

    public boolean getBusy() { return busy.get(); }

    public void setBusy(boolean busy)
    {
        this.busy.set(busy);
    }


    public SimpleBooleanProperty busyProperty()
    {
        return busy;
    }

    /**
     * Override of toString method
     * @return User name
     */
    @Override
    public String toString()
    {
        return username;
    }


}
