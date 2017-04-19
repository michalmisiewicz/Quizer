package proz.misiewicz.elkowanie;

/**
 * Model of game result
 */
public class GameResult
{
    private String hostUsername;
    private String guestUsername;
    private int hostScore;
    private int guestScore;

    /**
     * Creates game result
     * @param hostUsername host name
     * @param guestUsername guest name
     * @param hostScore host score
     * @param guestScore guest score
     */
    public GameResult(String hostUsername, String guestUsername, int hostScore, int guestScore)
    {
        this.hostUsername = hostUsername;
        this.guestUsername = guestUsername;
        this.hostScore = hostScore;
        this.guestScore = guestScore;
    }

    /**
     * Gets the value of host name
     * @return host name
     */
    public String getHostUsername()
    {
        return hostUsername;
    }


    /**
     * Gets the value of guest name
     * @return guest name
     */
    public String getGuestUsername()
    {
        return guestUsername;
    }

    /**
     * Gets the value of host score
     * @return host score
     */
    public int getHostScore()
    {
        return hostScore;
    }

    /**
     * Gets the value of guest score
     * @return guest score
     */
    public int getGuestScore()
    {
        return guestScore;
    }
}
