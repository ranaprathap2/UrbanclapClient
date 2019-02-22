import java.sql.*;
import java.util.*;

public class Client extends Consumer {

    private String clientID;
    private String clientName;
    private String eMailID;
    private String password;

    public boolean loginClient() {
        String userName, password;
        Scanner in = new Scanner(System.in);

        System.out.println("Enter Your eMail ID : ");
        userName = in.next();

        System.out.println("Enter Your Password : ");
        password = in.next();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String dbPassword = null;

        boolean resultSetExists = true;

        String passQuery = "select *from Clients where eMail=?";

        try {
            connection = SQLiteConnection.connectDB();
            preparedStatement = connection.prepareStatement(passQuery);
            preparedStatement.setString(1, userName);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                dbPassword = resultSet.getString("Password");

                //if record is found then load the credentials of the logged in client
                this.clientID = resultSet.getString(1);
                this.clientName = resultSet.getString(2);
                this.eMailID = resultSet.getString(3);
                this.password = resultSet.getString(4);
            }
            else
                resultSetExists =false;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                preparedStatement.close();
                resultSet.close();
                connection.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        if(resultSetExists == false)
        {
            System.out.println("Invalid Login Credentials !");
            return false;
        }
        if (PasswordUtils.checkPasswordWithHash(password,dbPassword))
            return true;
        else
            return false;
    }

    public void registerClient() {
        Scanner in = new Scanner(System.in);
        String confirmPassword;

        System.out.println("Enter Your Name : ");
        clientName = in.nextLine();

        System.out.println("Enter Your eMail ID: ");
        eMailID = in.nextLine();
        do {
            System.out.println("Choose Your Password : ");
            password = in.next();

            System.out.println("Confirm Your Password : ");
            confirmPassword = in.next();

        } while (!validate(password, confirmPassword));

        clientID = getClientID();

        saveClientToDB(clientID,clientName,eMailID,password);
    }

    public String getLoggedInClientID() {
        return this.clientID;
    }
}
