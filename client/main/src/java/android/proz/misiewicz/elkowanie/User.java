package android.proz.misiewicz.elkowanie;

/**
 * User model class
 */
public class User
{
    /**
     * User login
     */
    private String login;

    /**
     * Creates new user
     * @param login
     */
    public User(String login)
    {
        this.login = login;
    }

    /**
     * Gets user login
     * @return user name
     */
    public String getLogin()
    {
        return login;
    }
}
