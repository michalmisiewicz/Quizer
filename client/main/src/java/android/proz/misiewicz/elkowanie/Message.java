package android.proz.misiewicz.elkowanie;

/**
 * Model of Server message
 */
public class Message
{
    public static final int INVITATION = 100;
    public static final int INVITE_ACCEPTANCE = 101;
    public static final int INVITE_REJECTION = 102;
    public static final int PLAYER_TURN = 103;
    public static final int OPPONENT_UPDATE = 104;
    public static final int GAME_RESULTS = 105;
    public static final int NO_OPPONENT = 106;

    /**
     * message content
     */
    private String message;

    /**
     * message type
     */
    private int messageType;

    /**
     * Creates new message
     * @param messageType message type
     * @param message message content
     */
    public Message(int messageType, String message)
    {
        this.message = message;
        this.messageType = messageType;
    }

    /**
     * Return message
     * @return message
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * Check if message is game invitation
     * @return true if message is game invitation, otherwise false
     */
    public boolean isInvitation() { return messageType == INVITATION; }

    /**
     * Check if message is game invitation acceptance
     * @return true if message is game invitation acceptance, otherwise false
     */
    public boolean isInvitationAcceptance() { return messageType == INVITE_ACCEPTANCE; }

    /**
     * Check if message is game invitation rejection
     * @return true if message is game invitation rejection, otherwise false
     */
    public boolean isInvitationRejection() { return messageType == INVITE_REJECTION; }

    /**
     * Check if message is game player turn
     * @return true if message is game player turn, otherwise false
     */
    public boolean isPlayerTurn() { return messageType == PLAYER_TURN; }

    /**
     * Check if message is opponent score update
     * @return true if message is opponent score update, otherwise false
     */
    public boolean isOpponentUpdate() { return messageType == OPPONENT_UPDATE; }

    /**
     * Check if message is games results
     * @return true if message is games results, otherwise false
     */
    public boolean isGameResults() { return messageType == GAME_RESULTS; }

    /**
     * Check if message is games results
     * @return true if message is games results, otherwise false
     */
    public boolean isNoOpponentMessage() { return messageType == NO_OPPONENT; }

}

