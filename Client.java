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
        String dbPassword;

        String passQuery = "select *from Consumers where eMail=? AND ConsumerID like 'CL-%'";

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
                this.password = dbPassword;

                if (PasswordUtils.checkPasswordWithHash(password,dbPassword))
                {
                    System.out.println("-------------------------------------------");
                    System.out.println("Logged in as Client , Your ClientID :"+getClientID());
                    System.out.println("Welcome "+getClientName()+" !");
                    System.out.println("-------------------------------------------");
                    System.out.println();

                    connection.close();
                    return true;
                }
                else
                    System.out.println("Password Incorrect !");
            }
            else
                System.out.println("Invalid Login Credentials !");

            connection.close();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        return false;
    }

    private boolean verifyMail(String mail)
    {
        String parseQuery = "select *from Consumers where eMail=? AND ConsumerID like 'CL-%'";

        try
        {
            Connection connection = SQLiteConnection.connectDB();
            PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1,mail);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()!=false)
            {
                connection.close();
                return true;
            }
            // also close the connection when no resultSet exist and then return false
            connection.close();
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
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

        do {
            System.out.println("Enter Your eMail ID: ");
            eMailID = in.nextLine();

            if(verifyMail(eMailID))
                System.out.println("The eMail ID you have entered already exist !, Try an alternate eMail ID");
            else
                break;

        }while(true);

        do {
            System.out.println("Choose Your Password : ");
            password = in.next();

            System.out.println("Confirm Your Password : ");
            confirmPassword = in.next();

            if(!PasswordUtils.validate(password, confirmPassword))
                System.out.println("Password Mismatch Enter Again !");
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
        int option;

        do {
            System.out.println("1 -> ONGOING REQUESTS");
            System.out.println("2 -> HISTORY ");
            System.out.println("3 -> BACK TO DASHBOARD ");

            System.out.print("Enter Option : ");

            if(InputUtils.checkIntegerMismatch(in))
            {
                option = in.nextInt();

                if (option == 1)
                    listOngoingRequests();

                else if (option == 2)
                    listBookingHistory();

                else if(option == 3)
                    break;

                else
                    System.out.println("Invalid Choice !");
            }
        } while (true);
    }

    private void listOngoingRequests() {
        Bookings request = new Bookings();
        boolean requestsFound = request.getOngoingRequests(getClientID(),getClientName());

        if (requestsFound && readyToUpdateRequest()) {
                updateRequest();
        }
        else {
            System.out.println("No Ongoing Requests for You !");
        }
    }
}
