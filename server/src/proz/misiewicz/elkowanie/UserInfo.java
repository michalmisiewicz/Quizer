package proz.misiewicz.elkowanie;

import javafx.beans.property.SimpleStringProperty;

/**
 * Class is used to hold information about server users
 */
public class UserInfo
{
    /**
     * User name
     */
    private SimpleStringProperty username;

    /**
     * User password
     */
    private SimpleStringProperty password;

    /**
     * Creates new user info
     * @param username user name
     * @param password user password
     */
    public UserInfo(String username, String password)
    {
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
    }

    /**
     * Gets user name
     * @return User name
     */
    public String getUsername()
    {
        return username.get();
    }

    /**
     * User name property
     * @return User name property
     */
    public SimpleStringProperty usernameProperty()
    {
        return username;
    }

    /**
     * Gets user password
     * @return User password
     */
    public String getPassword()
    {
        return password.get();
    }

    /**
     * User password property
     * @return User password property
     */
    public SimpleStringProperty passwordProperty()
    {
        return password;
    }

}
