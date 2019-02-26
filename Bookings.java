import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Date;
import java.text.SimpleDateFormat;

public class Bookings {

    private String requestID;
    private String consumerID;
    private String partnerID;
    private Date  dateOfRequest;
    private Date dateOfBooking;
    private String status;
    private int rating;

    Bookings()
    {
        requestID="";
        consumerID="";
        partnerID="";
        dateOfRequest = new Date();
        dateOfBooking = new Date();
        status = "Unprocessed";
        rating = 0;
    }

    public void makeRequest(Consumer consumer)
    {
        String bookingDateAsString=null;
        Scanner in = new Scanner(System.in);

        requestID = getRequestID();

        System.out.println("Enter Partner ID : ");
        partnerID = in.next();

        if(consumer instanceof Client)
        {
            Client client = (Client)consumer;
            consumerID = client.getClientID();
        }

        if(consumer instanceof Guest)
        {
            Guest guest = (Guest)consumer;
            consumerID = guest.getGuestID();
        }

        do {
            System.out.println("Enter Your Service Hiring Date yyyy-MM-dd : ");
            String date = in.next();

            System.out.println("Enter Your Service Hiring Time HH:mm : ");
            String time = in.next();

            bookingDateAsString = date+" "+time;
            SimpleDateFormat pattern = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            try
            {
                System.out.println(bookingDateAsString);
                dateOfBooking = pattern.parse(bookingDateAsString);
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }

        }while(!validateBooking(consumerID,partnerID,dateOfRequest,dateOfBooking));

        saveRequestToDB();
    }

    public void saveRequestToDB()
    {
        SimpleDateFormat pattern = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String sql = "INSERT INTO Bookings(RequestID,ConsumerID,PartnerID,DateOfRequest,DateOfBooking,Status) VALUES(?,?,?,?,?,?)";

            try
            {
                Connection connection = SQLiteConnection.connectDB();

                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, requestID);
                pstmt.setString(2, consumerID);
                pstmt.setString(3, partnerID);
                pstmt.setString(4, pattern.format(dateOfRequest));
                pstmt.setString(5, pattern.format(dateOfBooking));
                pstmt.setString(6, status );

                pstmt.executeUpdate();

                System.out.println("Request Successful !");

                connection.close();
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
    }

    private int generateRequestID(String passQuery)
    {
        int getValue = 0;

        try
        {
            Connection connection = SQLiteConnection.connectDB();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(passQuery);

            if(resultSet.next())
            {
                getValue = Integer.parseInt(resultSet.getString(1));
            }
            connection.close();
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }

        return getValue;
    }

    private String getRequestID()
    {
        String newID = "RQ-" + generateRequestID("select count(RequestID)+1 from Bookings");

        return newID;
    }

    public boolean getOngoingRequests(Consumer consumer)
    {
        boolean resultSetExist=true;
        String parseQuery = null;

        try
        {
            String userID = null;

            parseQuery="select Bookings.RequestID,Bookings.PartnerID,Partners.Name,Partners.Profession,Partners.ContactNo,Bookings.DateOfRequest,Bookings.DateOfBooking from Bookings INNER JOIN Partners ON Bookings.PartnerID=Partners.PartnerID AND Bookings.Status='Unprocessed' AND Bookings.ConsumerID=?";

            if(consumer instanceof Client)
            {

                Client client = (Client)consumer;
                userID = client.getClientID();
                System.out.println("Ongoing Requests for ClientID = "+userID);
                System.out.println("Client Name : "+client.getClientName());

                System.out.println();
            }

            else if(consumer instanceof Guest)
            {
                Scanner in = new Scanner(System.in);

                System.out.println("Enter Your Mail ID : ");
                String mail = in.next();

                userID = getGuestDetailsFromMail(mail);

                if(userID != null)
                    System.out.println("Ongoing Request for GuestID = "+userID);
                else
                    System.out.println("Invalid Guest !");

            }

            Connection connection = SQLiteConnection.connectDB();
            PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1,userID);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()==false)
                resultSetExist=false;
            else
            {
                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------");
                System.out.println("RequestID     PartnerID                 PartnerName                     Profession               ContactNo           DateOfRequest         DateOfBooking ");
                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------");
                do{
                    System.out.printf("%8s    %10s    %25s     %30s          %10s      %18s    %18s \n",resultSet.getString("RequestID"),resultSet.getString("PartnerID"),resultSet.getString("Name"),resultSet.getString("Profession"),resultSet.getString("ContactNo"),resultSet.getString("DateOfRequest"),resultSet.getString("DateOfBooking"));
                }while(resultSet.next());

                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------");

                System.out.println();

            }

            connection.close();
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
            return resultSetExist;
        }


    public void updateBookingRequest(int mapRequest,String requestID) {
        String parseQuery = "update Bookings set Status=? where RequestID=?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try
        {
            connection = SQLiteConnection.connectDB();

            preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1, keyRequestMap(mapRequest));
            preparedStatement.setString(2, requestID);

            preparedStatement.executeUpdate();
            System.out.println("Update Successful !");

            connection.close();

        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String keyRequestMap(int key)
    {
        HashMap<Integer,String> serviceMap = new HashMap<Integer,String>();

        serviceMap.put(1,"Processed");
        serviceMap.put(2,"Cancelled");

        return serviceMap.get(key);
    }

    public void updateRatings(int rating,String requestID)
    {
        String parseQuery = "update Bookings set Rating=? where RequestID=?";

        try {
            Connection connection = SQLiteConnection.connectDB();

            PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setInt(1, rating);
            preparedStatement.setString(2, requestID);

            preparedStatement.executeUpdate();
            System.out.println("Thanks for Your Rating !");

            connection.close();

        }

        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        updatePartnerRating(rating, requestID);
    }

    private void updatePartnerRating(int newRating,String requestID)
    {
        String partnerID = getPartnerIDFromRequest(requestID);
        String parseQuery = "select ExperienceServing,AverageRating from Partners where PartnerID=?";

        int experienceServing = 0;
        double newAverage = 0;

        try {
            Connection connection = SQLiteConnection.connectDB();

            PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1, partnerID);
            ResultSet resultSet = preparedStatement.executeQuery();

            experienceServing = resultSet.getInt("ExperienceServing");
            float overallAverage = (experienceServing) * resultSet.getFloat("AverageRating");

            experienceServing+=1;
            newAverage = (overallAverage + newRating)/(experienceServing);

            //truncate new average
            int truncNewAvg = (int)(newAverage*10);
            newAverage = truncNewAvg/10d;

            connection.close();

        }

        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        update(experienceServing,newAverage,partnerID);
    }

    private void update(int experienceServing,double newAverage,String partnerID)
    {
        String parseQuery = "update Partners SET ExperienceServing=?,AverageRating=? WHERE PartnerID=?";

        try
        {
            Connection connection = SQLiteConnection.connectDB();

            PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setInt(1, experienceServing);
            preparedStatement.setDouble( 2, newAverage);
            preparedStatement.setString(3, partnerID);

            preparedStatement.executeUpdate();

            connection.close();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String getPartnerIDFromRequest(String requestID)
    {
        String parseQuery = "select PartnerID from Bookings where RequestID = ?";
        String partnerID = null;

        try
        {
            Connection connection = SQLiteConnection.connectDB();

            PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1, requestID);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next())
                partnerID = resultSet.getString("PartnerID");

            connection.close();

        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return partnerID;
    }

    public boolean getBookingHistory(String clientID)
    {
        String parseQuery="select Bookings.RequestID,Bookings.PartnerID,Partners.Name,Partners.Profession,Partners.ContactNo,Bookings.DateOfRequest,Bookings.DateOfBooking,Bookings.Status,Bookings.Rating from Bookings INNER JOIN Partners ON Bookings.PartnerID=Partners.PartnerID where Bookings.ConsumerID=?";
        boolean resultSetExist=true;

        try
        {
            Connection connection = SQLiteConnection.connectDB();
            PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1,clientID);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()==false)
                resultSetExist=false;
            else
            {
                System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
                System.out.println("RequestID       PartnerID               PartnerName                     Profession              ContactNo          DateOfRequest      DateOfBooking             Status              Rating");
                System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

                do {
                    //System.out.println(resultSet.getString("RequestID") +"              "+ resultSet.getString("PartnerID")+"                     "+resultSet.getString("Name")+"                   "+resultSet.getString("Profession")+"           "+resultSet.getString("ContactNo")+"           "+resultSet.getString("DateOfRequest")+"            "+resultSet.getString("DateOfBooking")+"            "+resultSet.getString("Status")+"           "+resultSet.getInt("Rating"));
                    System.out.printf("%8s    %10s    %25s         %30s     %10s      %18s    %18s   %12s                %d \n",resultSet.getString("RequestID"),resultSet.getString("PartnerID"),resultSet.getString("Name"),resultSet.getString("Profession"),resultSet.getString("ContactNo"),resultSet.getString("DateOfRequest"),resultSet.getString("DateOfBooking"),resultSet.getString("Status"),resultSet.getInt("Rating"));


                }while(resultSet.next());

                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

            }
            connection.close();
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }

        return resultSetExist;
    }

    private String getGuestDetailsFromMail(String mail)
    {
        String parseQuery="select ConsumerID from Consumers where eMail=?";
        String guestID = null;

        try
        {
            Connection connection = SQLiteConnection.connectDB();
            PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1,mail);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()!=false)
                guestID=resultSet.getString("ConsumerID");

            connection.close();
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }

        return guestID;
    }

    public boolean validateBooking(String clientID,String partnerID,Date dateOfRequest,Date dateOfBooking) {

        // check if booking date and time are valid
        if(!new DateUtils().validateHireDateAndTime(dateOfRequest,dateOfBooking))
            return false;

        String parseQuery = "select *from Bookings where PartnerID=? AND Status='Unprocessed'";

        boolean alreadyBooked = false;
        boolean slotAvailable = true;

        Date bookingDateAsDate = null;
        Date slotStartTime = null;
        Date slotEndTime = null;

        try {
            Connection connection = SQLiteConnection.connectDB();
            PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1, partnerID);

            ResultSet resultSet = preparedStatement.executeQuery();
            SimpleDateFormat pattern = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            while (resultSet.next()) {

                String consumerIDfromDB = resultSet.getString("ConsumerID");
                String bookingDateinDB = resultSet.getString("DateOfBooking");
                bookingDateAsDate = pattern.parse(bookingDateinDB);

                // Assuming time slot for service be 90 minutes
                long millis = 90 * 60 * 1000; // 90 minutes in milliseconds

                slotStartTime = bookingDateAsDate;
                slotEndTime = new Date(slotStartTime.getTime()+millis);

                // check for booking in the time slot
                //if(slotStartTime.compareTo(dateOfBooking)>=0 && slotEndTime.compareTo(dateOfBooking)<=0)
                if(!dateOfBooking.before(slotStartTime) && !dateOfBooking.after(slotEndTime))
                {
                    // Already booked by the hiring client
                    if (consumerIDfromDB.equals(clientID))
                    {
                        alreadyBooked = true;
                        break;
                    }
                    // Already hired for the same time slot by someone else
                    else {
                        {
                            alreadyBooked = true;
                            slotAvailable = false;
                            break;
                        }
                    }
                }
            }
            connection.close();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return bookingStatus(alreadyBooked,slotAvailable,bookingDateAsDate,slotStartTime,slotEndTime);
    }

    private Boolean bookingStatus(Boolean alreadyBooked,Boolean slotAvailable,Date bookingDateAsDate,Date slotStartTime,Date slotEndTime)
    {
        // check if already booked by someone else
        if (alreadyBooked && !slotAvailable) {

            SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat time = new SimpleDateFormat("HH:mm");

            System.out.println("The Partner is booked by another client for the time slot !");
            System.out.println("Date: "+date.format(bookingDateAsDate)+"      From: " +time.format(slotStartTime) + "    To: "+time.format(slotEndTime) );

            System.out.printf("\nPlease try booking with another time slot!\n\n");
            return false;
        }

        // check if already booked by the client
        if (alreadyBooked) {
            System.out.printf("\nYou have already hired the Partner, Multiple booking for a partner is not possible on the same Date and Time !\n\n");
            return false;
        }

        System.out.println("\nBooking Validated ! Our UC Partner will reach out to you soon .\n\n");
        return true;
    }
}

