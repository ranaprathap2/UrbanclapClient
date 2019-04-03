import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public abstract class Consumer implements EndUser {

    public int generateUserID(String passQuery) {
        Connection connection = SQLiteConnection.connectDB();
        int getValue = 0;

        try
        {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(passQuery);

            if (resultSet.next()) {
                getValue = Integer.parseInt(resultSet.getString(1));
            }
            connection.close();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }

        return getValue;
    }

    public String getUserID() {

        String newID = null;

        if(this instanceof Client)
        {
           newID = "CL-" + generateUserID("select count(ConsumerID)+1 from Consumers where ConsumerID like 'CL-%'");
        }

        if(this instanceof Guest)
        {
            newID = "GU-" + generateUserID("select count(ConsumerID)+1 from Consumers where ConsumerID like 'GU-%'");
        }

        return newID;
    }


    public void saveUserToDB() {

        Connection connection = SQLiteConnection.connectDB();
        PreparedStatement pstmt = null;
        String sql;

        try {

            if(this instanceof Client)
            {
                Client client = (Client)this;
                //String clientID = getUserID();
                client.setClientID(getUserID());

                sql = "INSERT INTO Consumers VALUES(?,?,?,?,?)";
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1,client.getClientID());
                pstmt.setString(2,client.getClientName());
                pstmt.setString(3,client.getContactNo());
                pstmt.setString(4,client.geteMailID());
                pstmt.setString(5,PasswordUtils.hashPassword(client.getPassword()));
            }

            else if(this instanceof Guest)
            {
                Guest guest = (Guest)this;
                //String guestID = getUserID();
                guest.setGuestID(getUserID());

                sql = "INSERT INTO Consumers (ConsumerID,Name,ContactNo,eMail) VALUES(?,?,?,?)";
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1,guest.getGuestID());
                pstmt.setString(2,guest.getGuestName());
                pstmt.setString(3,guest.getContactNo());
                pstmt.setString(4,guest.geteMailID());
            }

            pstmt.executeUpdate();
            System.out.println("Registration Successful !");
            connection.close();

        }
        catch (Exception e)
        {
            System.out.println("Check in saveUserToDB : "+e);
        }
    }

    public void searchForServices() {
        int serviceCategory,city;
        Scanner in = new Scanner(System.in);

        System.out.println("LIST OF CITIES : ");
        ArrayList<String> citiesListFromDB= fetchCitiesFromDB();
        viewDBListItems(citiesListFromDB);

        do
        {
            System.out.printf("\nChoose Your City  : ");
            if(InputUtils.checkIntegerMismatch(in))
            {
                city = in.nextInt();

                if(city>0 && city<=citiesListFromDB.size()+1)
                    break;
                else
                    System.out.println("Invalid Input, Enter an Integer from the Above ResultSet !");
            }
        }while(true);

        System.out.println("LIST OF SERVICE CATEGORIES : ");
        ArrayList<String> servicesListFromDB = fetchServiceTypesFromDB();
        viewDBListItems(servicesListFromDB);

        do
        {
            System.out.printf("\nPick One : ");

            if(InputUtils.checkIntegerMismatch(in))
            {
                serviceCategory = in.nextInt();

                if(serviceCategory>0 && serviceCategory<=servicesListFromDB.size()+1)
                    break;
                else
                    System.out.println("Invalid Input, Enter an Integer from the Above ResultSet !");
            }
        }while(true);

        System.out.println();

        Boolean partnersFound = viewPartnersFromDB(servicesListFromDB.get(serviceCategory-1), citiesListFromDB.get(city-1));

        if (partnersFound) {
            if (userReadyToHire()) {
                // If a guest user wants to make a request then add this details
                if(this instanceof Guest)
                {
                    Guest guest =(Guest)this;
                    guest.addGuestToDB();
                }

                // create Request when ready to hire
                Bookings requests = new Bookings();
                requests.makeRequest(this,citiesListFromDB.get(city-1),servicesListFromDB.get(serviceCategory-1));
            }
        }
        else {
            System.out.println("Sorry No Partners found for Your Request ! ");
        }
    }

    public void viewDBListItems(ArrayList<String> setFromDB)
    {
        int index=0;

        for(String str : setFromDB)
            System.out.println(++index+" -> "+str);
    }

    public boolean userReadyToHire() {
        int interest;
        Scanner in = new Scanner(System.in);

        do
        {
            System.out.print("Interested to Hire ? ");
            System.out.println("1.Yes    2.No ");

            if(InputUtils.checkIntegerMismatch(in))
            {
                interest = in.nextInt();

                if(interest == 1)
                    return true;
                if(interest==2)
                    break;
                else
                    System.out.println("Invalid Choice ! Enter an Integer from the Options.");
            }
        }while(true);

       return false;
    }

    private Boolean viewPartnersFromDB(String serviceCategory, String city) {
        String parseQuery = "select *from Partners where ServiceCategory = ? and City = ?";
        Boolean resultSetExist = true;

        Connection connection = SQLiteConnection.connectDB();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1, serviceCategory);
            preparedStatement.setString(2, city);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next())
                resultSetExist = false;
            else {
                System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------");
                System.out.println(" PartnerID              PartnerName         ContactNo                     Profession               ExperienceServing      AverageRating");
                System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------");
                do {
                    System.out.printf("%10s  %25s      %10s     %30s                 %d                    %3.1f\n", resultSet.getString("PartnerID"), resultSet.getString("Name"), resultSet.getString("ContactNo"),resultSet.getString("Profession"), resultSet.getInt("ExperienceServing"), resultSet.getDouble("AverageRating"));
                } while (resultSet.next());
                System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------");

                System.out.println();
            }
            connection.close();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return resultSetExist;
    }


    public ArrayList<String> fetchCitiesFromDB()
    {
        String parseQuery = "select City from Partners";
        Connection connection = SQLiteConnection.connectDB();

        HashSet<String> setCities = new HashSet<String>();
        try
        {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(parseQuery);

            while(resultSet.next())
                setCities.add(resultSet.getString("City"));

            connection.close();
        }
        catch(Exception e)
        {
                System.out.println(e.getMessage());
        }

        return new ArrayList<String>(setCities);
    }

    public ArrayList<String> fetchServiceTypesFromDB()
    {
        String parseQuery = "select ServiceCategory from Partners";

        HashSet<String> setServices = new HashSet<String>();
        try
        {
            Connection connection = SQLiteConnection.connectDB();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(parseQuery);

            while(resultSet.next())
                setServices.add(resultSet.getString("ServiceCategory"));

            connection.close();
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }

        return new ArrayList<String>(setServices);
    }

    public boolean userReadyToUpdateRequest() {
        Scanner in = new Scanner(System.in);
        int option;
        do
        {
            System.out.print("Ready to update request ? ");
            System.out.println("1. Yes        2. No");

            if(InputUtils.checkIntegerMismatch(in))
            {
                option = in.nextInt();

                if (option == 1)
                    return true;

                else if(option==2)
                    break;

                else
                    System.out.println("Invalid Choice ! Enter an integer from the Options.");
            }
        }while(true);

        return false;
    }

    private boolean verifyRequestID(String consumerID,String requestID)
    {
        try
        {
            Connection connection = SQLiteConnection.connectDB();

            String parseQuery = "select RequestID from Bookings where ConsumerID=? AND Status='Unprocessed'";
            PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1,consumerID);

            ResultSet resultSet = preparedStatement.executeQuery();


            while(resultSet.next())
            {
                if(resultSet.getString("RequestID").equals(requestID))
                {
                    // If partner is verified close the connection and return true
                    connection.close();
                    return true;
                }
            }
            // Close the connection when the partner is not found and proceed to statements after catch
            connection.close();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }

        System.out.println("Invalid Request ID, Choose a RequestID from the ResultSet !");
        return false;
    }

    public void updateRequest() {
        Scanner in = new Scanner(System.in);
        int option;

        String requestID;
        String consumerID = null;
        Bookings bookings = new Bookings();

        System.out.println("Update Your Request :");

        do {
            System.out.println("Enter Your Request ID : ");
            requestID = in.next().toUpperCase();

            if(this instanceof Client)
            {
                Client client = (Client)this;
                consumerID = client.getClientID();
            }
            else if(this instanceof Guest)
            {
                Guest guest = (Guest)this;
                consumerID = guest.getGuestID();
            }

            if(verifyRequestID(consumerID,requestID))
                break;

        }while(true);

        System.out.print("Enter your option 1. Processed 2. Cancelled: ");

        while(true) {
            if(InputUtils.checkIntegerMismatch(in))
            {
                option = in.nextInt();
                // If processed ask for rating else skip
                if(option == 1)
                {
                    int rating = askRatingForTheServicedRequest();
                    bookings.updateRatings(rating, requestID);
                    break;
                }

                else if(option ==2)
                    break;

                else
                    System.out.println("Invalid Choice ! Enter a Valid Option .");
            }
        }

        bookings.updateBookingRequest(option, requestID);
    }

    public int askRatingForTheServicedRequest()
    {
        int rating;
        Scanner in = new Scanner(System.in);
        System.out.println("Update Rating for the Service (1 to 5): ");

        while(true){
            if(InputUtils.checkIntegerMismatch(in))
            {
                rating = in.nextInt();
                if(rating>0 && rating<6)
                    break;
                else
                    System.out.println("Invalid Rating , Input a Rating Value b/w 1 and 5");
            }
        }
        return rating;
    }

    public void loadDashboard() {
        Scanner in = new Scanner(System.in);
        int choice;

        do {
            System.out.println("1 -> SEARCH FOR SERVICES");
            System.out.println("2 -> VIEW BOOKING INFO");
            System.out.println("3 -> GO BACK TO MAIN");

            System.out.printf("\n Enter Your Choice : ");

            if(InputUtils.checkIntegerMismatch(in))
            {
                choice = in.nextInt();

                if (choice == 1)
                    searchForServices();

                else if (choice == 2)
                    viewBookingInfo();

                else if(choice == 3)
                    break;

                else
                {
                    System.out.println("Invalid Choice !");
                    continue;
                }
            }
        } while (true);
    }
}