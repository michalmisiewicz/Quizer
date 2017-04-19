package proz.misiewicz.elkowanie;

import javafx.beans.property.SimpleIntegerProperty;

import java.util.*;

/**
 * Class contains whole information about active game
 */
public class Game
{
    private final String ACTIVEPLAYER_TURN = "APT";

    /**
     * Number of question in one game
     */
    private final int GAME_LENGHT = 9;

    /**
     * Game id
     */
    private int id;

    /**
     * Game host
     */
    private User host;

    /**
     * Game guest
     */
    private User guest;

    /**
     * Database reference
     */
    private DBHandler db;

    /**
     * List of game question number
     */
    private List<Integer> questionList;

    /**
     * List of GameMessages
     */
    private Queue<GameMessage> gameFlow;

    /**
     * Host score
     */
    private SimpleIntegerProperty hostScore;

    /**
     * Guest score
     */
    private SimpleIntegerProperty guestScore;

    /**
     * Creates new game
     * @param id game ID
     * @param host inviter
     * @param guest guest
     */
    public Game(int id, User host, User guest)
    {
        this.id = id;
        this.host = host;
        this.guest = guest;
        this.db = DBHandler.getInstance();
        this.questionList = generateQuestion(GAME_LENGHT);
        this.gameFlow = prepareMessages();
        this.hostScore = new SimpleIntegerProperty(0);
        this.guestScore = new SimpleIntegerProperty(0);
    }

    /**
     * Creates queue of GameMessages used to controle game flow
     * @return GameMessage queue
     */
    private Queue<GameMessage> prepareMessages()
    {
        Queue<GameMessage> queue = new LinkedList<>();

        queue.add(new GameMessage(host, ACTIVEPLAYER_TURN + id + ":" + questionList.get(0) + ":" + questionList.get(1) + ":" + questionList.get(2)));
        queue.add(new GameMessage(guest, ACTIVEPLAYER_TURN + id + ":" + questionList.get(0) + ":" + questionList.get(1) + ":" + questionList.get(2) + ":"
                + questionList.get(3) + ":" + questionList.get(4) + ":" + questionList.get(5)));
        queue.add(new GameMessage(host, ACTIVEPLAYER_TURN + id + ":" + questionList.get(3) + ":" + questionList.get(4) + ":" + questionList.get(5) + ":"
                + questionList.get(6) + ":" + questionList.get(7) + ":" + questionList.get(8)));
        queue.add(new GameMessage(guest, ACTIVEPLAYER_TURN + id + ":" + questionList.get(GAME_LENGHT - 3) + ":" + questionList.get(GAME_LENGHT - 2) + ":" + questionList.get(GAME_LENGHT - 1)));
        return queue;
    }

    /**
     * Get next GameMessage. Message is removed from queue
     * @return GameMessage
     */
    public GameMessage getNextStep()
    {
        return gameFlow.remove();
    }

    @Override
    public String toString()
    {
        return host.toString() + " vs " + guest.toString();
    }

    /**
     * Generates n different question number
     * @param n - question amount
     * @return - list of integer
     */
    private List<Integer> generateQuestion(int n)
    {
        Random generator = new Random();
        List<Integer> list = new ArrayList<>();
        int questionAmount = db.getQuestionsNumber();

        if(n > questionAmount) return null;

        while(list.size() != n)
        {
            int x = generator.nextInt(questionAmount) + 1;
            if(!list.contains(x)) list.add(x);
        }
        return list;
    }

    /**
     * Gets game ID
     * @return game ID
     */
    public int getId()
    {
        return id;
    }

    /**
     * Get user opponent
     * @param username user name
     * @return opponent
     */
    public User getOpponent(String username)
    {
        if(username.equals(host.getUsername())) return  guest;
        else return host;
    }

    /**
     * Chceck if game is end
     * @return True if game is end
     */
    public boolean isGameEnd()
    {
        return gameFlow.isEmpty();
    }

    public int getHostScore()
    {
        return hostScore.get();
    }

    public int getGuestScore()
    {
        return guestScore.get();
    }

    public SimpleIntegerProperty hostScoreProperty()
    {
        return hostScore;
    }

    public SimpleIntegerProperty guestScoreProperty()
    {
        return guestScore;
    }

    /**
     * Updates user game result
     * @param user update target
     * @param score update score
     */
    public void updateResults(User user, int score)
    {
        if(user == host) hostScore.set(hostScore.getValue() + score);
        else if(user == guest) guestScore.set(guestScore.getValue() + score);
    }

    /**
     * Gets host user name
     * @return User name
     */
    public String getHostUsername()
    {
        return  host.getUsername();
    }

    /**
     * Gets guest user name
     * @return User name
     */
    public String getGuestUsername()
    {
        return  guest.getUsername();
    }


}
