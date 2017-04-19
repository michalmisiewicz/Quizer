package proz.misiewicz.elkowanie;

import javafx.collections.ObservableList;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;

/**
 * This class implements server thread
 */
public class ServerThread extends Thread
{
    private final String LOGIN_ATTEMPT = "LOG";
    private final String NEW_GAME_REQUEST = "NGR";
    private final String NEW_GAME_INVITATION = "INV";
    private final String SUCCESS = "ACK";
    private final String ERROR = "NAK";
    private final String INVITATION_ACCEPT = "IAC";
    private final String INVITATION_REJECT = "IRJ";
    private final String OPPONENT_FOUND = "OPF";
    private final String OPPONENT_REJECT = "OPR";
    private final String DATABASE_REQUEST = "DBR";
    private final String GAME_UPDATE = "GUP";
    private final String OPPONENT_UPDATE = "OUP";
    private final String GAME_EXIT = "GAE";
    private final String REGISTER = "REG";
    private final String RESULT_REQUEST = "RRQ";
    private final String GAMES_RESULTS = "GRS";
    private final String NO_FREE_USER = "NFU";

    /**
     * NIO server socket channel
     */
    private ServerSocketChannel serverChannel;

    /**
     * server port
     */
    private int port = 4321;

    /**
     * server state
     */
    private boolean isRunning = true;

    /**
     * NIO selector
     */
    private Selector selector;

    /**
     * Database reference
     */
    private DBHandler db = DBHandler.getInstance();

    /**
     * Server handler reference
     */
    private ServerHandler server = ServerHandler.getInstance();

    /**
     * Active users ObservableList
     */
    private ObservableList<User> usersList;

    /**
     * Active games ObservableList
     */
    private ObservableList<Game> gamesList;

    /**
     * Creates new ServerThread
     * @param usersList Active users ObservableList reference
     * @param gamesList Active games ObservableList reference
     */
    public ServerThread(ObservableList<User> usersList, ObservableList<Game> gamesList)
    {
        this.usersList = usersList;
        this.gamesList = gamesList;
    }


    @Override
    public void run()
    {
        try
        {
            db.openConnection();
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(port));

            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            server.addConsoleMessage("Serwer rozpoczął nasłuch na porcie: " + port);

            while(isRunning)
            {

                selector.select();

                Iterator keys = selector.selectedKeys().iterator();
                while(keys.hasNext())
                {
                    SelectionKey key = (SelectionKey) keys.next();
                    keys.remove();

                    if(!key.isValid()) continue;

                    if(key.isAcceptable())
                    {
                        accept(key);
                    }
                    else if(key.isReadable())
                    {
                        read(key);
                    }
                }
            }
        } catch (ClosedSelectorException e) { }
        catch (IOException e) { e.printStackTrace(); }
        catch (Exception e) { e.printStackTrace(); }
        finally
        {
            try
            {
                if(selector.isOpen()) selector.close();
                if(serverChannel.isOpen())
                {
                    serverChannel.socket().close();
                    serverChannel.close();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            server.addConsoleMessage("Serwer został wyłączony");
        }
    }

    /**
     * Accept new server connection
     * @param key SelectionKey
     * @throws IOException
     */
    private void accept(SelectionKey key) throws IOException
    {
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
    }

    /**
     * Read connect users requests
     * @param key
     * @throws Exception
     */
    private void read(SelectionKey key) throws Exception
    {
        SocketChannel channel = (SocketChannel) key.channel();
        Socket socket = channel.socket();

        User activeUser = getUser(channel);

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int byteRead = channel.read(buffer);

        if(byteRead == -1)
        {
            socket.close();
            channel.close();

            if(activeUser != null)
            {
                server.removeUser(activeUser);
                server.addConsoleMessage("Użytkownik " + activeUser + " wylogował się");

            }
            key.cancel();
            return;
        }

        String msg = new String(buffer.array(), 0, byteRead);

        if (msg.startsWith(LOGIN_ATTEMPT))
        {
            msg = msg.substring(3);
            String[] auth = msg.split(":");

            if(db.validateUser(auth[0], auth[1]) && !server.isOnlineUser(auth[0]))
            {
                msg = SUCCESS;
                buffer = ByteBuffer.wrap(msg.getBytes());
                channel.write(buffer);

                int id = db.getUserId(auth[0]);
                User newUser = new User(id, auth[0], auth[1], channel);
                server.addUser(newUser);
                server.addConsoleMessage("Użytkownik " + newUser + " zalogował się");
            }
            else
            {
                msg = ERROR;
                buffer = ByteBuffer.wrap(msg.getBytes());
                channel.write(buffer);
            }
        }
        else if(msg.startsWith(NEW_GAME_REQUEST))
        {
            User opponent = findFreeUser(activeUser);
            if(opponent != null)
            {
                msg = NEW_GAME_INVITATION + activeUser;
                buffer = ByteBuffer.wrap(msg.getBytes());
                opponent.getSocketChannel().write(buffer);
            }
            else channel.write(ByteBuffer.wrap(NO_FREE_USER.getBytes()));
        }
        else if(msg.startsWith(INVITATION_ACCEPT))
        {
            String username = msg.substring(3);
            User inviter = getUser(username);
            msg = OPPONENT_FOUND + activeUser;
            buffer = ByteBuffer.wrap(msg.getBytes());
            inviter.getSocketChannel().write(buffer);

            int id = db.createGame(inviter, activeUser);
            Game newGame = new Game(id, inviter, activeUser);
            server.setBusy(inviter, true);
            server.setBusy(activeUser, true);
            server.addGame(newGame);
            server.addConsoleMessage("Gra pomiędzy " + newGame.getHostUsername() + " vs " + newGame.getGuestUsername()
                    + " została rozpoczęta");
            GameMessage message = newGame.getNextStep();
            message.send();
        }
        else if(msg.startsWith(INVITATION_REJECT))
        {
            String username = msg.substring(3);
            User inviter = getUser(username);
            msg = OPPONENT_REJECT + activeUser;
            buffer = ByteBuffer.wrap(msg.getBytes());
            inviter.getSocketChannel().write(buffer);
        }
        else if(msg.startsWith(DATABASE_REQUEST))
        {
            String  questions = db.getQuestionsInString();
            CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
            channel.write(encoder.encode(CharBuffer.wrap(questions + '*')));
        }
        else if(msg.startsWith(GAME_UPDATE))
        {
            msg = msg.substring(3);
            String[] results = msg.split(":");
            if(results.length > 5) throw new Exception();
            else
            {
                int gameId = Integer.valueOf(results[0]);
                Game game = getGame(gameId);

                int score = Integer.valueOf(results[1]) + Integer.valueOf(results[2]) + Integer.valueOf(results[3]);
                server.updateResults(game, activeUser, score);

                if (results.length == 5)
                {
                    if (game.isGameEnd())
                    {
                        db.writeGameResults(game);
                        server.removeGame(game);
                        String res = results[1] + ":" + results[2] + ":" + results[3];
                        User opponent = game.getOpponent(activeUser.getUsername());
                        String text = OPPONENT_UPDATE + res;
                        opponent.getSocketChannel().write(ByteBuffer.wrap((text).getBytes()));
                        server.addConsoleMessage("Gra pomiędzy " + game.getHostUsername() + " vs " + game.getGuestUsername()
                               + " została zakończona z wynikiem " + game.getHostScore() + " - " + game.getGuestScore());
                    } else
                    {
                        GameMessage message = game.getNextStep();
                        String tmp = message.getMessage();
                        String[] split = tmp.split(":", 2);
                        String text = split[0] + ":" + results[1] + ":" + results[2] + ":" + results[3] + ":" + split[1];
                        message.getUser().getSocketChannel().write(ByteBuffer.wrap(text.getBytes()));
                    }
                } else if (results.length == 4)
                {
                    User opponent = game.getOpponent(activeUser.getUsername());
                    String text = results[1] + ":" + results[2] + ":" + results[3];
                    text = OPPONENT_UPDATE + text;
                    opponent.getSocketChannel().write(ByteBuffer.wrap((text).getBytes()));
                }

            }
        }
        else if(msg.startsWith(GAME_EXIT))
        {
            server.setBusy(activeUser, false);
        }
        else if(msg.startsWith(REGISTER))
        {
            msg = msg.substring(3);
            String[] results = msg.split(":");

            if(db.registerUser(results[0], results[1]))
            {
                msg = SUCCESS;
                buffer = ByteBuffer.wrap(msg.getBytes());
                channel.write(buffer);
                server.addConsoleMessage("Zarejestrowano nowego użytkownika - " + results[0]);
            }
            else
            {
                msg = ERROR;
                buffer = ByteBuffer.wrap(msg.getBytes());
                channel.write(buffer);
            }
        }
        else if(msg.startsWith(RESULT_REQUEST))
        {
            String results = db.getResultsInString(activeUser);
            channel.write(ByteBuffer.wrap((GAMES_RESULTS + results).getBytes()));
        }
    }

    /**
     * Shutdown Server Thread
     * @throws IOException
     */
    public void shutdownThread() throws IOException
    {
        isRunning = false;
        selector.close();
    }

    /**
     * Finds free user
     * @param inviter requesting user
     * @return active user if there is free online user. Otherwise null
     */
    private User findFreeUser(User inviter)
    {
        for(User user: usersList)
        {
            if(user != inviter && !user.getBusy()) return user;
        }
        return null;
    }

    /**
     * Return user
     * @param channel NIO SocketChannel
     * @return user
     */
    private User getUser(SocketChannel channel)
    {
        for(User user: usersList)
        {
            if(user.isUserSocketChannel(channel)) return user;
        }
        return null;
    }

    /**
     * Return user
     * @param username user name
     * @return user
     */
    private User getUser(String username)
    {
        for(User user: usersList)
        {
            if(user.getUsername().equals(username)) return user;
        }
        return null;
    }

    /**
     * Return game
     * @param gameId Game ID
     * @return game
     */
    private Game getGame(int gameId)
    {
        for(Game game: gamesList)
        {
            if(game.getId() == gameId ) return game;
        }
        return null;
    }
}
