import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Scanner;

public abstract class Consumer implements EndUser {

    public int generateClientID(String passQuery) {
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

    public String getClientID() {
        String newID = "CL-" + generateClientID("select count(ClientID)+1 from Clients");
        return newID;
    }

    public boolean validate(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }

    public void saveClientToDB(String clientID,String clientName,String eMailID,String password) {
        Connection connection = SQLiteConnection.connectDB();
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO Clients VALUES(?,?,?,?)";
        String hashedPassword = PasswordUtils.hashPassword(password,12);

        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, clientID);
            pstmt.setString(2, clientName);
            pstmt.setString(3, eMailID);
            pstmt.setString(4, hashedPassword);

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

    public void loadDashboard() {
        System.out.println("Logged in as Client: " );
        Scanner in = new Scanner(System.in);

        int choice = 0;

        do {
            System.out.println("1 -> SEARCH FOR SERVICES");
            System.out.println("2 -> VIEW BOOKINGS");
            System.out.println("3 -> GO BACK TO MAIN");

            choice = in.nextInt();

            if (choice == 1)
                searchForServices();

            if (choice == 2)
                viewBookings();

        } while (choice != 3);
    }

    public void searchForServices() {
        int serviceCategory = 0, city = 0;
        Scanner in = new Scanner(System.in);

        listServicesOffered();
        System.out.print("Choose Your Service Category : ");
        serviceCategory = in.nextInt();

        System.out.println();

        listCitiesOffered();
        System.out.print("Choose Your City : ");
        city = in.nextInt();

        boolean partnersFound = getPartnersFromDB(keyServiceMap(serviceCategory), keyCityMap(city));

        if (partnersFound) {
            if (readyToHire()) {
                //create Request when ready to hire
                Bookings requests = new Bookings();
                requests.makeRequest(this);
            }
        } else {
            System.out.println("Sorry No Partners found for Your Request ! ");
        }
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

    private boolean getPartnersFromDB(String profession, String city) {
        String parseQuery = "select *from Partners where Profession = ? and City = ?";
        boolean resultSetExist   = true;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = SQLiteConnection.connectDB();
            preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1, profession);
            preparedStatement.setString(2, city);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next() == false)
                resultSetExist = false;
            else {
                System.out.println("------------------------------------------------------------------------------------------------------");
                System.out.println(" PartnerID                 PartnerName        ContactNo          ExperienceServing      AverageRating");
                System.out.println("------------------------------------------------------------------------------------------------------");
                do {
                    System.out.printf("%10s    %25s      %10s               %d                    %3.1f\n",resultSet.getString("PartnerID"),resultSet.getString("Name"),resultSet.getString("ContactNo"),resultSet.getInt("ExperienceServing"),resultSet.getDouble("AverageRating"));
                } while (resultSet.next());
                System.out.println("------------------------------------------------------------------------------------------------------");

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

    private String keyCityMap(int key) {
        HashMap<Integer, String> cityMap = new HashMap<Integer, String>();

        cityMap.put(1, "Pondicherry");
        cityMap.put(2, "Chennai");
        cityMap.put(3, "Bangalore");
        cityMap.put(4, "Hyderabad");
        cityMap.put(5, "Mumbai");
        cityMap.put(6, "Calcutta");
        cityMap.put(7, "Delhi");

        return cityMap.get(key);
    }

    private String keyServiceMap(int key) {
        HashMap<Integer, String> serviceMap = new HashMap<Integer, String>();

        serviceMap.put(1, "Beauty & Spa");
        serviceMap.put(2, "Computer & Appliance Repairs");
        serviceMap.put(3, "Weddings & Events");
        serviceMap.put(4, "Health & Fitness");
        serviceMap.put(5, "Tutors & Lessons");

        return serviceMap.get(key);

    }

    private void listCitiesOffered() {
        System.out.println("1 -> Pondicherry   2 -> Chennai   3 -> Bangalore ");
        System.out.println("4 -> Hyderabad     5 -> Mumbai    6 -> Calcutta ");
        System.out.println("7 -> Delhi");
    }

    private void listServicesOffered() {
        System.out.println("1 -> Beauty & Spa");
        System.out.println("2 -> Computer & Appliance Repair");
        System.out.println("3 -> Weddings & Events");
        System.out.println("4 -> Health & Fitness");
        System.out.println("5 -> Tutors & Lessons");
    }

    public void viewBookings() {
        Scanner in = new Scanner(System.in);
        int option = 0;

        do {
            System.out.println("1 -> ONGOING REQUESTS");
            System.out.println("2 -> HISTORY ");
            System.out.println("3 -> BACK TO DASHBOARD ");

            System.out.print("Enter Option : ");
            option = in.nextInt();

            if (option == 1)
                listOngoingRequests();

            if (option == 2)
                listBookingHistory();

            if (option == 3)
                System.out.println("Invalid Choice !");

        } while (option != 3);

    }

    private void listOngoingRequests() {
        boolean requestsFound = new Bookings().getOngoingRequests(clientID);

        if (requestsFound) {
            if (readyToUpdateRequest())
                updateRequest();
        } else {
            System.out.println("No Ongoing Requests for You !");
        }
    }

    private boolean readyToUpdateRequest() {
        Scanner in = new Scanner(System.in);
        int option = 0;

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
        int option = 0;
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

    private void listBookingHistory() {
        new Bookings().getBookingHistory(clientID);



    }
