package proz.misiewicz.elkowanie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implement Database connection. Singleton pattern
 */
public class DBHandler
{
    /**
     * Database handler reference
     */
    private final static DBHandler instance = new DBHandler();

    /**
     * Database driver
     */
    private final String driver = "org.sqlite.JDBC";


    /**
     * Database URL
     */
    private final String DB_URL = "jdbc:sqlite:db/server.db";

    /**
     * Database connection
     */
    private Connection connection;

    /**
     * Creates new DBHandler
     */
    private DBHandler()
    {
    }

    /**
     * Returns DBHandler instance
     * @return DBHandler instance
     */
    public static DBHandler getInstance() { return instance; }

    /**
     * Load database driver
     */
    public void loadDBDriver()
    {
        try
        {
            Class.forName(driver);
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Open database connection
     */
    public void openConnection()
    {
        try
        {
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(false);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Close database connection
     */
    public void closeConnection()
    {
        if(connection != null)
        {
            try
            {
                connection.close();
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
            finally
            {
                connection = null;
            }
        }
    }

    /**
     * Check if user exist in database
     * @param username user name
     * @param password user password
     * @return true is identities are correct, otherwise false
     */
    public boolean validateUser(String username, String password)
    {
        if(connection != null)
        {
            try
            {
                PreparedStatement stm = connection.prepareStatement("SELECT COUNT(*) FROM players WHERE login = ? and password = ?");
                stm.setString(1, username);
                stm.setString(2, password);
                stm.execute();
                ResultSet result = stm.getResultSet();

                if(result.getInt(1) == 1) return true;
                else return false;
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Returns user ID
     * @param username user name
     * @return
     */
    public int getUserId(String username)
    {
        if(connection != null)
        {
            try
            {
                PreparedStatement stm = connection.prepareStatement("SELECT ID FROM players WHERE login = ?");
                stm.setString(1, username);
                stm.execute();
                ResultSet result = stm.getResultSet();

                return result.getInt(1);

            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * Gets Questions in String, ready to send
     * @return Questions string
     */
    public String getQuestionsInString()
    {
        String questions = new String();

        if(connection != null)
        {
            String count_sql = "SELECT COUNT(*)  FROM QUESTIONS";
            String question_sql = "SELECT * FROM QUESTIONS";

            try
            {
                Statement stm = connection.createStatement();

                ResultSet result = stm.executeQuery(count_sql);

                if(result.getInt(1) != 0)
                {
                    result = stm.executeQuery(question_sql);


                    while (result.next())
                    {
                        questions += result.getInt(1) + "#" +
                                result.getString(2) + "#" +
                                result.getString(3) + "#" +
                                result.getString(4) + "#" +
                                result.getString(5) + "#" +
                                result.getString(6);

                        questions += ":";
                    }
                }
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return questions;
    }

    /**
     * Returns question number in database
     * @return question number
     */
    public int getQuestionsNumber()
    {
        if(connection != null)
        {
            String count_sql = "SELECT COUNT(*) FROM QUESTIONS";

            try
            {
                Statement stm = connection.createStatement();
                ResultSet result = stm.executeQuery(count_sql);

                return result.getInt(1);
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * Create new game
     * @param host host
     * @param guest guest
     * @return game ID
     */
    public int createGame(User host, User guest)
    {
        if (connection != null)
        {
            String insert_sql = "INSERT INTO GAMES (HOST, GUEST) VALUES (" + host.getId() + ", " + guest.getId() + ')';
            try
            {
                String[] generatedColumns = { "ID" };
                PreparedStatement stm = connection.prepareStatement(insert_sql, generatedColumns);
                stm.executeUpdate();
                ResultSet result = stm.getGeneratedKeys();
                result.next();
                connection.commit();
                return result.getInt(1);

            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * Write game results
     * @param game game
     */
    public void writeGameResults(Game game)
    {
        if(connection != null)
        {
            String update_sql = "UPDATE GAMES SET HOST_SCORE = " + game.getHostScore() + ", GUEST_SCORE = " + game.getGuestScore()
                    + " WHERE ID = " + game.getId();
            try
            {
                Statement stm = connection.createStatement();
                stm.executeUpdate(update_sql);
                connection.commit();

            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Register new user in database
     * @param username user name
     * @param password user password
     * @return true on success, otherwise false
     */
    public boolean registerUser(String username, String password)
    {
        if(connection != null)
        {
            try
            {
                PreparedStatement stm = connection.prepareStatement("SELECT COUNT(*)  FROM PLAYERS WHERE LOGIN = ?");
                stm.setString(1, username);
                stm.execute();
                ResultSet result = stm.getResultSet();

                if(result.getInt(1) == 0)
                {
                    stm = connection.prepareStatement("INSERT INTO PLAYERS (LOGIN, PASSWORD) VALUES (?, ?)");
                    stm.setString(1, username);
                    stm.setString(2, password);
                    stm.execute();
                    connection.commit();
                    return true;
                }
                else return false;
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Returns questions
     * @return question list
     */
    public List<Question> getQuestions()
    {
        List<Question> questions = new ArrayList<>();

        if(connection != null)
        {
            String count_sql = "SELECT COUNT(*)  FROM QUESTIONS";
            String question_sql = "SELECT * FROM QUESTIONS";

            try
            {
                Statement stm = connection.createStatement();
                ResultSet result = stm.executeQuery(count_sql);

                if(result.getInt(1) != 0)
                {
                    result = stm.executeQuery(question_sql);

                    while (result.next())
                    {
                        questions.add(new Question(
                                result.getString(2),
                                result.getString(3),
                                result.getString(4),
                                result.getString(5),
                                result.getString(6)));
                    }
                }
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return questions;
    }

    /**
     * Return user info
     * @return UserInfo list
     */
    public List<UserInfo> getUsersInfo()
    {
        String sql = "SELECT login, password FROM PLAYERS";

        List<UserInfo> list = new ArrayList<>();

        if(connection != null)
        {
            try
            {
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(sql);

                while (result.next())
                {
                    UserInfo user = new UserInfo(
                            result.getString(1),
                            result.getString(2)
                    );
                    list.add(user);
                }
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * Return GamesResults
     * @return GameResult list
     */
    public List<GameResult> getResults()
    {
        String sql = "SELECT h.login, g.login, host_score, guest_score FROM PLAYERS h, PLAYERS g, GAMES " +
                "WHERE GAMES.HOST = h.id and GAMES.GUEST = g.id ORDER BY START_TIME";

        List<GameResult> list = new ArrayList<>();

        if(connection != null)
        {
            try
            {
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(sql);

                while (result.next())
                {
                    GameResult gameResult = new GameResult(
                            result.getString(1),
                            result.getString(2),
                            result.getInt(3),
                            result.getInt(4)
                    );
                    list.add(gameResult);
                }
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * Return GamesResults in String, ready to send
     * @return GameResult string
     */
    public String getResultsInString(User user)
    {
        String results = new String();

        if(connection != null)
        {
            String count_sql = "SELECT COUNT(*) FROM GAMES WHERE HOST = " + user.getId() + " OR GUEST = " + user.getId();
            String question_sql = "SELECT h.login, g.login, host_score, guest_score FROM PLAYERS h, PLAYERS g, GAMES " +
                    "WHERE (GAMES.HOST = h.id and GAMES.GUEST = g.id) and (HOST = " + user.getId()
                    + " OR GUEST = " + user.getId() + ") ORDER BY START_TIME DESC";

            try
            {
                Statement stm = connection.createStatement();
                ResultSet result = stm.executeQuery(count_sql);

                if(result.getInt(1) != 0)
                {
                    result = stm.executeQuery(question_sql);

                    int i = 0;
                    while (result.next())
                    {
                        if(i++ == 10) break;

                        results += result.getString(1) + "#" +
                                result.getString(2) + "#" +
                                result.getInt(3) + "#" +
                                result.getInt(4);

                        results += ":";
                    }
                }
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return results;
    }
}
