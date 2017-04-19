package proz.misiewicz.elkowanie;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The class is simple game message used by server to handle game flow
 */
public class GameMessage
{
    /**
     * Receiver of message
     */
    private User user;

    /**
     * Content of message
     */
    private String message;

    /**
     * Create game message
     * @param user receiver
     * @param message conten of message
     */
    public GameMessage(User user, String message)
    {
        this.user = user;
        this.message = message;
    }

    /**
     * Sends message to user
     * @throws IOException if a i/o error occurs
     */
    public void send() throws IOException
    {
        user.getSocketChannel().write(ByteBuffer.wrap(message.getBytes()));
    }

    /**
     * Gets content of message
     * @return String
     */
    public String getMessage()
    {
        return  message;
    }

    /**
     * Gets receiver of message
     * @return user
     */
    public User getUser()
    {
        return user;
    }
}
