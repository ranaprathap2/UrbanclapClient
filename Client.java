import java.util.Scanner;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Client extends Consumer {

    private String clientID;
    private String clientName;
    private String contactNo;
    private String eMailID;
    private String password;

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID)
    {
        this.clientID = clientID;
    }

    public String getClientName() {
        return clientName;
    }

    public String getContactNo() {
        return contactNo;
    }

    public String geteMailID() {
        return eMailID;
    }

    public String getPassword() {
        return password;
    }

    public boolean loginUser() {
        String userName, password;
        Scanner in = new Scanner(System.in);

        System.out.println("Enter Your eMail ID : ");
        userName = in.next();

        System.out.println("Enter Your Password : ");
        password = in.next();

        Connection connection = SQLiteConnection.connectDB();
        String dbPassword = null;

        boolean resultSetExists = true;

        String passQuery = "select *from Consumers where eMail=?";

        try
        {
            PreparedStatement preparedStatement = connection.prepareStatement(passQuery);
            preparedStatement.setString(1, userName);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                dbPassword = resultSet.getString("Password");

                //if record is found then load the credentials of the logged in client
                this.clientID = resultSet.getString(1);
                this.clientName = resultSet.getString(2);
                this.contactNo = resultSet.getString(3);
                this.eMailID = resultSet.getString(4);
                this.password = resultSet.getString(5);
            }
            else
                resultSetExists =false;

            connection.close();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        if(!resultSetExists)
        {
            System.out.println("Invalid Login Credentials !");
            return false;
        }
        else
        {
            if (PasswordUtils.checkPasswordWithHash(password,dbPassword))
            {
                System.out.println("-------------------------------------------");
                System.out.println("Logged in as Client , Your ClientID :"+getClientID());
                System.out.println("Welcome "+getClientName()+" !");
                System.out.println("-------------------------------------------");
                System.out.println();

                return true;
            }
            else
                System.out.println("Password Incorrect !");
        }

        return false;
    }

    public void registerClient() {
        Scanner in = new Scanner(System.in);
        String confirmPassword;

        System.out.println("Enter Your Name : ");
        clientName = in.nextLine();

        System.out.println("Enter Your ContactNo : ");
        contactNo = in.nextLine();

        System.out.println("Enter Your eMail ID: ");
        eMailID = in.nextLine();

        do {
            System.out.println("Choose Your Password : ");
            password = in.next();

            System.out.println("Confirm Your Password : ");
            confirmPassword = in.next();

            if(!PasswordUtils.validate(password, confirmPassword))
            {
                System.out.println("Password Mismatch Enter Again !");
                continue;
            }
            else
                break;

        } while (true);

        clientID = getUserID();
        saveUserToDB();
    }

    private void listBookingHistory() {
        new Bookings().getBookingHistory(clientID);
    }

    public void viewBookingInfo() {
        Scanner in = new Scanner(System.in);
        int option=0;

        do {
            System.out.println("1 -> ONGOING REQUESTS");
            System.out.println("2 -> HISTORY ");
            System.out.println("3 -> BACK TO DASHBOARD ");

            System.out.print("Enter Option : ");

            if(in.hasNextInt())
            {
                option = in.nextInt();

                if(!(option>0 && option<4))
                {
                    System.out.println("Invalid Choice !");
                    continue;
                }
            }
            else
            {
                System.out.println("Input Mismatch ! Enter a valid Integer value.");
                in.next();
                continue;
            }

            if (option == 1)
                listOngoingRequests();

            if (option == 2)
                listBookingHistory();

        } while (option != 3);
    }

    public void listOngoingRequests() {
        Bookings request = new Bookings();
        boolean requestsFound = request.getOngoingRequests(this);

        if (requestsFound) {
            if (readyToUpdateRequest())
                updateRequest();
        }
        else {
            System.out.println("No Ongoing Requests for You !");
        }
    }
}
