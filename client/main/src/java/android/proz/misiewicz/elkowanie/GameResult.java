package android.proz.misiewicz.elkowanie;

/**
 * Created by Michał on 2017-01-16.
 */

/**
 * Model of Game result
 */
public class GameResult
{
    private String hostUsername;
    private String guestUsername;
    private int hostScore;
    private int guestScore;

    /**
     * Creates new gamer result
     * @param hostUsername host username
     * @param guestUsername guest username
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

    @Override
    public String toString()
    {
        return hostUsername + " " + hostScore + " - " + guestScore + " " + guestUsername;
    }
}