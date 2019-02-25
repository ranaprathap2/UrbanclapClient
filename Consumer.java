import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

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
        String sql = null;

        try {

            if(this instanceof Client)
            {
                Client client = (Client)this;
                String clientID = getUserID();
                client.setClientID(clientID);

                sql = "INSERT INTO Consumers VALUES(?,?,?,?,?)";
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1,clientID);
                pstmt.setString(2,client.getClientName());
                pstmt.setString(3,client.getContactNo());
                pstmt.setString(4,client.geteMailID());
                pstmt.setString(5,PasswordUtils.hashPassword(client.getPassword(),12));
            }

            else if(this instanceof Guest)
            {
                Guest guest = (Guest)this;
                String guestID = getUserID();
                guest.setGuestID(guestID);

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
            System.out.println(e);
        }
    }

    public void searchForServices() {
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
                if(this instanceof Guest)
                {
                    Guest guest =(Guest)this;
                    guest.addGuestToDB();
                }

                // create Request when ready to hire
                Bookings requests = new Bookings();
                requests.makeRequest(this);
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

        Connection connection = SQLiteConnection.connectDB();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1, serviceCategory);
            preparedStatement.setString(2, city);

            ResultSet resultSet = preparedStatement.executeQuery();

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
            connection.close();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return resultSetExist;
    }

    public HashMap<Integer,String> fetchCitiesFromDB()
    {
        String parseQuery = "select City from Partners";
        Connection connection = SQLiteConnection.connectDB();

        HashSet<String> setCities = new HashSet<String>();
        try
        {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(parseQuery);

            while(resultSet.next())
            {
                setCities.add(resultSet.getString("City"));
            }
            connection.close();
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
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
            connection.close();
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
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
        System.out.println("Enter Your Request ID : ");
        requestID = in.next();

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
                searchForServices();

            if (choice == 2)
                viewBookingInfo();

        } while (choice != 3);
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
