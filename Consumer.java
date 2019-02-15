import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public abstract class Consumer implements EndUser {

    public int generateUserID(String passQuery) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        int getValue = 0;

        try {
            connection = SQLiteConnection.connectDB();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(passQuery);

            if (resultSet.next()) {
                getValue = Integer.parseInt(resultSet.getString(1));
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        return getValue;
    }

    public String getUserID(Consumer user) {

        String newID = null;

        if(user instanceof Client)
        {
           newID = "CL-" + generateUserID("select count(ClientID)+1 from Clients");
        }

        if(user instanceof Guest)
        {
            newID = "GU-" + generateUserID("select count(GuestID)+1 from Guests");
        }

        return newID;
    }


    public void saveUserToDB(Consumer user) {

        Connection connection = SQLiteConnection.connectDB();
        PreparedStatement pstmt = null;
        String sql = null;

        try {

            if(user instanceof Client)
            {
                Client client = (Client)user;
                String clientID = getUserID(client);
                client.setClientID(clientID);

                sql = "INSERT INTO Clients VALUES(?,?,?,?,?)";
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1,clientID);
                pstmt.setString(2,client.getClientName());
                pstmt.setString(3,client.getContactNo());
                pstmt.setString(4,client.geteMailID());
                pstmt.setString(5,PasswordUtils.hashPassword(client.getPassword(),12));
            }

            else if(user instanceof Guest)
            {
                Guest guest = (Guest)user;
                String guestID = getUserID(guest);
                guest.setGuestID(guestID);

                sql = "INSERT INTO Guests VALUES(?,?,?,?)";
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1,guest.getGuestID());
                pstmt.setString(2,guest.getGuestName());
                pstmt.setString(3,guest.getContactNo());
                pstmt.setString(4,guest.geteMailID());
            }

            pstmt.executeUpdate();
            System.out.println("Registration Successful !");

        } catch (Exception e) {
            System.out.println(e);
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public void searchForServices(Consumer user) {
        int serviceCategory,city;
        Scanner in = new Scanner(System.in);

        System.out.println("LIST OF CITIES : ");
        HashMap<Integer, String> mapCities= fetchCitiesFromDB();

        System.out.printf("\nChoose Your City  : ");
        city = in.nextInt();


        System.out.println("LIST OF SERVICE CATEGORIES : ");
        HashMap<Integer, String> mapServices= fetchServiceTypesFromDB();

        System.out.printf("\nPick One : ");
        serviceCategory = in.nextInt();

        System.out.println();

        boolean partnersFound = getPartnersFromDB(mapToServices(mapServices,serviceCategory), mapToCities(mapCities,city));

        if (partnersFound) {
            if (readyToHire()) {
                // If a guest user wants to make a request then add this details
                if(user instanceof Guest)
                {
                    Guest guest =(Guest)user;
                    guest.addGuestToDB();
                }

                // create Request when ready to hire
                Bookings requests = new Bookings();
                requests.makeRequest(user);
            }
        }
        else {
            System.out.println("Sorry No Partners found for Your Request ! ");
        }
    }

    public String mapToCities(HashMap<Integer,String> mapCities,int index)
    {
        return mapCities.get(index);
    }

    public String mapToServices(HashMap<Integer,String> mapServices,int index)
    {
        return mapServices.get(index);
    }

    public boolean readyToHire() {
        int interest;
        Scanner in = new Scanner(System.in);

        System.out.print("Interested to Hire ? ");
        System.out.println("1.Yes    2.No ");

        interest = in.nextInt();

        if (interest == 1)
            return true;
        else
            return false;
    }

    private boolean getPartnersFromDB(String serviceCategory, String city) {
        String parseQuery = "select *from Partners where ServiceCategory = ? and City = ?";
        boolean resultSetExist = true;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = SQLiteConnection.connectDB();
            preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1, serviceCategory);
            preparedStatement.setString(2, city);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next() == false)
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
        return resultSetExist;
    }

    public HashMap<Integer,String> fetchCitiesFromDB()
    {
        String parseQuery = "select City from Partners";
        Connection connection = SQLiteConnection.connectDB();
        ResultSet resultSet = null;

        HashSet<String> setCities = new HashSet<String>();
        try
        {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(parseQuery);

            while(resultSet.next())
            {
                setCities.add(resultSet.getString("City"));
            }
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        finally {
            try
            {
                connection.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
        }

        HashMap<Integer,String> citiesMap = new HashMap<Integer,String>();
        int index=0;

        for(String str : setCities)
            citiesMap.put(++index,str);

        for(int i=1;i<=citiesMap.size();i++)
            System.out.println(i+" -> "+citiesMap.get(i));

        return citiesMap;

    }

    public HashMap<Integer,String> fetchServiceTypesFromDB()
    {
        String parseQuery = "select ServiceCategory from Partners";
        Connection connection = SQLiteConnection.connectDB();
        ResultSet resultSet = null;

        HashSet<String> setServices = new HashSet<String>();
        try
        {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(parseQuery);

            while(resultSet.next())
            {
                setServices.add(resultSet.getString("ServiceCategory"));
            }
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        finally {
            try
            {
                connection.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
        }

        HashMap<Integer,String> servicesMap = new HashMap<Integer,String>();
        int index=0;

        for(String str : setServices)
            servicesMap.put(++index,str);

        for(int i=1;i<=servicesMap.size();i++)
            System.out.println(i+" -> "+servicesMap.get(i));

        return servicesMap;
    }

    public boolean readyToUpdateRequest() {
        Scanner in = new Scanner(System.in);
        int option;

        System.out.print("Ready to update request ? ");
        System.out.println("1. Yes        2. No");

        option = in.nextInt();

        if (option == 1)
            return true;
        else
            return false;
    }

    public void updateRequest() {
        Scanner in = new Scanner(System.in);
        int option;
        String requestID = null;

        System.out.println("Update Your Request :");
        System.out.print("Enter Your Request ID : ");
        requestID = in.next();

        System.out.println();

        System.out.print("Enter your option 1. Processed 2. Cancelled: ");
        option = in.nextInt();

        Bookings bookings = new Bookings();
        bookings.updateBookingRequest(option, requestID);

        if (option == 1) {
            System.out.println("Update Rating for the Service (1 to 5): ");
            int rating = in.nextInt();
            bookings.updateRatings(rating, requestID);
        }
    }

    public void loadDashboard() {
        Scanner in = new Scanner(System.in);

        int choice;

        do {
            System.out.println("1 -> SEARCH FOR SERVICES");
            System.out.println("2 -> VIEW BOOKING INFO");
            System.out.println("3 -> GO BACK TO MAIN");

            System.out.printf("\n Enter Your Choice : ");
            choice = in.nextInt();

            if (choice == 1)
                searchForServices(this);

            if (choice == 2)
                viewBookingInfo();

        } while (choice != 3);
    }

    public void listOngoingRequests() {
        boolean requestsFound = new Bookings().getOngoingRequests(this);

        if (requestsFound) {
            if (readyToUpdateRequest())
                updateRequest();
        }
        else {
            System.out.println("No Ongoing Requests for You !");
        }
    }
}
