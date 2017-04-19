package android.proz.misiewicz.elkowanie;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This class implement game server connection. Singleton pattern
 */

public class ServerConnection
{
    private final String LOGIN_ATTEMPT = "LOG";
    private final String NEW_GAME_REQUEST = "NGR";
    private final String NEW_GAME_INVITATION = "INV";
    private final String SUCCESS = "ACK";
    private final String INVITATION_ACCEPT = "IAC";
    private final String INVITATION_REJECT = "IRJ";
    private final String OPPONENT_FIND = "OPF";
    private final String OPPONENT_REJECT = "OPR";
    private final String ACTIVEPLAYER_TURN = "APT";
    private final String DATABASE_REQUEST = "DBR";
    private final String GAME_UPDATE = "GUP";
    private final String OPPONENT_UPDATE = "OUP";
    private final String GAME_EXIT = "GAE";
    private final String REGISTER = "REG";
    private final String RESULT_REQUEST = "RRQ";
    private final String GAMES_RESULTS = "GRS";
    private final String NO_FREE_USER = "NFU";

    /**
     * Question buffer size
     */
    private final int BUF_SIZE = 102400;

    /**
     * Server address
     */
    private String serverAddress = "192.168.0.102";
    /**
     * server port
     */
    private int serverPort = 4321;

    private InputStream in;
    private OutputStream out;

    /**
     * communication socket
     */
    private Socket socket;

    /**
     * Keep connection state
     */
    private boolean isConnected = false;

    /**
     * Login attempt timeout
     */
    private int loginTimeout = 3000;

    /**
     * Active user
     */
    private User activeUser;

    /**
     * Server message queue
     */
    private BlockingQueue<Message> messagesQueue = new LinkedBlockingQueue<>();

    /**
     * Keep message listener state
     */
    private boolean isListening;

    /**
     * message reader executor service
     */
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Question list
     */
    private List<Question> questions = null;

    /**
     * ServerConnection instance
     */
    private static ServerConnection serverConnection = null;

    /**
     * creates new ServerConnection
     */
    private ServerConnection(){}

    /**
     * Return ServerConnection instance
     * @return ServerConnection instance
     */
    public static ServerConnection getInstance()
    {
        if(serverConnection == null) serverConnection = new ServerConnection();
        return serverConnection;
    }

    /**
     * Check if application is connected with server
     * @return
     */
    public boolean isConnected() { return isConnected;}


    public void setServerAddress(String serverAddress)
    {
        this.serverAddress = serverAddress;
    }

    /**
     * start server connection
     * @return true on connection establishment, otherwise false
     */
    public boolean startConnection()
    {
        try
        {
            socket = new Socket();
            socket.connect(new InetSocketAddress(serverAddress, serverPort), loginTimeout);
            out = socket.getOutputStream();
            in = socket.getInputStream();
            isConnected = true;
            return true;
        } catch (SocketTimeoutException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * stops ServerConnection in new thread
     */
    public void stopConnection()
    {
        new Thread(() ->
        {
            if(socket != null)
            {
                try
                {
                    socket.close();
                    isConnected = false;
                }catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * try to authenticate user
     * @param username user name
     * @param password user password
     * @return true on authentication success, otherwise false
     */
    public boolean logIn(String username, String password)
    {
        try
        {
            out.write((LOGIN_ATTEMPT + username + ':' + password).getBytes());
            byte[] buf = new byte[1024];
            int byteRead = in.read(buf);
            byte[] data = new byte[byteRead];
            System.arraycopy(buf, 0, data, 0, byteRead);
            String msg = new String(data);

            if(msg.equals(SUCCESS))
            {
                activeUser = new User(username);
                return true;
            }
            else return false;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * tries to register user
     * @param username user name
     * @param password user password
     * @return true on registration success, otherwise false
     */
    public boolean registerUser(String username, String password)
    {
        try
        {
            out.write((REGISTER + username + ':' + password).getBytes());
            byte[] buf = new byte[1024];
            int byteRead = in.read(buf);

            String msg = new String(buf, 0, byteRead);

            if(msg.equals(SUCCESS))
            {
                return true;
            }
            else return false;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Send new game request
     */
    public void sendNewGameRequest()
    {
        sendMessage(NEW_GAME_REQUEST);
    }

    /**
     * Send game acceptance
     */
    public void acceptInvitation(String username)
    {
        sendMessage(INVITATION_ACCEPT + username);
    }

    /**
     * Send game rejection
     */
    public void rejectInvitation(String username)
    {
        sendMessage(INVITATION_REJECT + username);
    }

    /**
     * Send game update
     */
    public void sendUpdate(String result) { sendMessage(GAME_UPDATE + result);}

    /**
     * Send game exit notification
     */
    public void sendGameRoomExitNotification() { sendMessage(GAME_EXIT);}

    /**
     * Send games results request
     */
    public void sendResultRequest() { sendMessage(RESULT_REQUEST);}

    /**
     * sends message in new thread
     * @param message Message to send
     */
    private void sendMessage(String message)
    {
        new Thread(() ->
        {
            try
            {
                out.write((message).getBytes());
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * starts messages listening in new thread
     */
    public void startMessagesListening()
    {
        new Thread(() ->
        {
            isListening = true;
            while (isListening)
            {
                try
                {
                    Message message = receiveMessage();
                    if (message != null) messagesQueue.put(message);
                } catch (IOException e)
                {
                    e.printStackTrace();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * receives message from server
     * @return received message
     * @throws IOException when i/o exception occur
     */
    private Message receiveMessage() throws IOException
    {
        byte[] buf = new byte[1024];
        int byteRead = in.read(buf);
        if(byteRead == -1) return null;
        byte[] data = new byte[byteRead];
        System.arraycopy(buf, 0, data, 0, byteRead);
        String msg = new String(data);

        if(msg.startsWith(NEW_GAME_INVITATION))
        {
            return new Message(Message.INVITATION, msg.substring(3));
        }
        else if(msg.startsWith(OPPONENT_FIND))
        {
            return new Message(Message.INVITE_ACCEPTANCE, msg.substring(3));
        }
        else  if(msg.startsWith(OPPONENT_REJECT))
        {
            return new Message(Message.INVITE_REJECTION, msg.substring(3));
        }
        else  if(msg.startsWith(ACTIVEPLAYER_TURN))
        {
            return new Message(Message.PLAYER_TURN, msg.substring(3));
        }
        else  if(msg.startsWith(OPPONENT_UPDATE))
        {
            return new Message(Message.OPPONENT_UPDATE, msg.substring(3));
        }
        else if(msg.startsWith(GAMES_RESULTS))
        {
            return new Message(Message.GAME_RESULTS, msg.substring(3));
        }
        else if(msg.startsWith(NO_FREE_USER))
        {
            return new Message(Message.NO_OPPONENT, null);
        }
        else return null;
    }

    /**
     * stop messages listening
     */
    public void stopListening()
    {
        isListening = false;
    }

    /**
     * Checks listener state
     * @return
     */
    public boolean isListening() { return  isListening; }

    /**
     * reads message from message queue
     * @return message
     * @throws InterruptedException when interrupted
     * @throws PollBlockingQueueTimeout when poll timeout occur
     */
    public Message readMessage() throws InterruptedException, PollBlockingQueueTimeout
    {
        Message message = messagesQueue.poll(100, TimeUnit.MILLISECONDS);
        if(message == null) throw new PollBlockingQueueTimeout();
        else return message;
    }

    /**
     * Submit new message reader
     * @param callable message reader
     * @return message reader result
     */
    public Future<Boolean> submitMessagesReader(Callable<Boolean> callable)
    {
        return executorService.submit(callable);
    }

    /**
     * Fetches questions from server
     * @return true on success, otherwise false
     * @throws IOException
     */
    public boolean fetchQuestions() throws IOException
    {
        sendMessage(DATABASE_REQUEST);
        byte[] buf = new byte[BUF_SIZE];
        String msg = "";
        while(true)
        {
            if(msg.length() != 0 && msg.charAt(msg.length() - 1) == '*') break;

            int byteRead = in.read(buf);
            if(byteRead == -1) return false;
            msg += new String(buf, 0, byteRead, "UTF-8");

        }

        msg = msg.substring(0,msg.length()-2);

        List<Question> list = new ArrayList<>();

        String[] questions = msg.split(":");

        if(questions.length == 0) return false;

        for(String row : questions)
        {
            String[] fields = row.split("#");
            Question question = new Question(
                    Integer.valueOf(fields[0]),
                    fields[1],
                    fields[2],
                    fields[3],
                    fields[4],
                    fields[5]
            );

            list.add(question);
        }
        this.questions = list;
        return true;
    }

    /**
     * Return question
     * @param id question id
     * @return question
     */
    public Question getQuestion(int id)
    {
        return questions.get(id);
    }

    /**
     * Checks if questions where downloaded
     * @return
     */
    public boolean hasQuestions()
    {
        if(questions == null) return false;
        return true;
    }

    /**
     * Returns active user
     * @return active user
     */
    public User getActiveUser() { return  activeUser; }
}
