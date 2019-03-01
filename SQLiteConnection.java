import java.sql.*;

public class SQLiteConnection {

    public static Connection connectDB()
    {
        Connection c = null;

        try
        {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite://Users/rana-pt2631/SQLite/UrbanClap.db");
        }
        catch (Exception e)
        {
            System.out.println(e);
        }

        return c;
    }
}
