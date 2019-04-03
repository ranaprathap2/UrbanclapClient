import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Bookings {

    private String requestID;
    private String consumerID;
    private String partnerID;
    LocalDateTime dateOfRequest;
    LocalDateTime dateOfBooking;
    private String status;
    private int rating;

    Bookings() {
        dateOfRequest = LocalDateTime.now();
        dateOfBooking = null;
        status = "Unprocessed";
        rating = 0;
    }

    private boolean verifyPartnerID(String partnerID, String city, String serviceCategory) {
        try {
            Connection connection = SQLiteConnection.connectDB();
            String parseQuery = "select PartnerID from Partners where City=? AND ServiceCategory=?";
            PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1, city);
            preparedStatement.setString(2, serviceCategory);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                if (resultSet.getString("PartnerID").equals(partnerID)) {
                    // If partner is verified close the connection and return true
                    connection.close();
                    return true;
                }
            }
            // Close the connection when the partner is not found and proceed to statements after catch
            connection.close();
        } catch (Exception e) {
            System.out.println(e);
        }

        System.out.println("Invalid Partner ID, Choose a PartnerID from the ResultSet !");
        return false;
    }

    public void makeRequest(Consumer consumer, String city, String serviceCategory) {
        String bookingDateAsString;
        Scanner in = new Scanner(System.in);

        requestID = getRequestID();

        do {
            System.out.println("Enter Partner ID : ");
            partnerID = in.next().toUpperCase();
            System.out.println(partnerID);

        } while (!verifyPartnerID(partnerID, city, serviceCategory));


        if (consumer instanceof Client) {
            Client client = (Client) consumer;
            consumerID = client.getClientID();
        }

        if (consumer instanceof Guest) {
            Guest guest = (Guest) consumer;
            consumerID = guest.getGuestID();
        }

        do {
            System.out.println("Enter Your Service Hiring Date & Time yyyy-MM-dd  : ");
            String date = in.next();

            System.out.println("Enter Your Service Hiring Time HH:mm : ");
            String time = in.next();

            bookingDateAsString = date + " " + time;
            System.out.println("check : " + bookingDateAsString);

            DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            try {
                dateOfBooking = LocalDateTime.parse(bookingDateAsString, pattern);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("Enter a valid Date !");
                continue;
            }

            System.out.println("Your Hiring Date & Time : " + dateOfBooking.format(pattern));

            if (validateBooking(consumerID, partnerID, dateOfRequest, dateOfBooking))
                break;

        } while (true);

        saveRequestToDB();
    }

    public void saveRequestToDB() {
        String sql = "INSERT INTO Bookings(RequestID,ConsumerID,PartnerID,DateOfRequest,DateOfBooking,Status) VALUES(?,?,?,?,?,?)";
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        try {
            Connection connection = SQLiteConnection.connectDB();

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, requestID);
            pstmt.setString(2, consumerID);
            pstmt.setString(3, partnerID);
            pstmt.setString(4, pattern.format(dateOfRequest));
            pstmt.setString(5, pattern.format(dateOfBooking));
            pstmt.setString(6, status);

            pstmt.execute();

            System.out.println("Request Successful !");

            connection.close();
        } catch (Exception e) {
            System.out.println("Check in save Request to DB :" + e);
        }
    }

    private int generateRequestID(String passQuery) {
        int getValue = 0;

        try {
            Connection connection = SQLiteConnection.connectDB();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(passQuery);

            if (resultSet.next()) {
                getValue = Integer.parseInt(resultSet.getString(1));
            }
            connection.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return getValue;
    }

    private String getRequestID() {
        String newID = "RQ-" + generateRequestID("select count(RequestID)+1 from Bookings");

        return newID;
    }

    public boolean viewOngoingRequests(String clientID, String clientName) {
        String parseQuery;

        try {
            parseQuery = "select Bookings.RequestID,Bookings.PartnerID,Partners.Name,Partners.Profession,Partners.ContactNo,Bookings.DateOfRequest,Bookings.DateOfBooking from Bookings INNER JOIN Partners ON Bookings.PartnerID=Partners.PartnerID AND Bookings.Status='Unprocessed' AND Bookings.ConsumerID=?";

            System.out.println("Ongoing Requests for ClientID = " + clientID);
            System.out.println("Client Name : " + clientName);

            System.out.println();

            Connection connection = SQLiteConnection.connectDB();
            PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1, clientID);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next() == false) // if no result set exist
            {
                connection.close();
                return false;
            } else {
                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------");
                System.out.println("RequestID     PartnerID                 PartnerName                     Profession               ContactNo           DateOfRequest         DateOfBooking ");
                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------");
                do {
                    System.out.printf("%8s    %10s    %25s     %30s          %10s      %18s    %18s \n", resultSet.getString("RequestID"), resultSet.getString("PartnerID"), resultSet.getString("Name"), resultSet.getString("Profession"), resultSet.getString("ContactNo"), resultSet.getString("DateOfRequest"), resultSet.getString("DateOfBooking"));
                } while (resultSet.next());

                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------");

                System.out.println();
            }

            connection.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return true;
    }

    public void updateBookingRequest(int mapRequest, String requestID) {
        String parseQuery = "update Bookings set Status=? where RequestID=?";

        try {
            Connection connection = SQLiteConnection.connectDB();

            PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1, keyRequestMap(mapRequest));
            preparedStatement.setString(2, requestID);

            preparedStatement.executeUpdate();
            System.out.println("Update Successful !");

            connection.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String keyRequestMap(int key) {
        // using a ternary operator with three operands
        return (key == 1) ? "Processed" : "Cancelled";
    }

    public void updateRatings(int rating, String requestID) {
        String parseQuery = "update Bookings set Rating=? where RequestID=?";

        try {
            Connection connection = SQLiteConnection.connectDB();

            PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setInt(1, rating);
            preparedStatement.setString(2, requestID);

            preparedStatement.executeUpdate();
            System.out.println("Thanks for Your Rating !");

            connection.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        updatePartnerRating(rating, requestID);
    }

    private void updatePartnerRating(int newRating, String requestID) {
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

            experienceServing += 1;
            newAverage = (overallAverage + newRating) / (experienceServing);

            //truncate new average
            int truncNewAvg = (int) (newAverage * 10);
            newAverage = truncNewAvg / 10d;

            connection.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        update(experienceServing, newAverage, partnerID);
    }

    private void update(int experienceServing, double newAverage, String partnerID) {
        String parseQuery = "update Partners SET ExperienceServing=?,AverageRating=? WHERE PartnerID=?";

        try {
            Connection connection = SQLiteConnection.connectDB();

            PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setInt(1, experienceServing);
            preparedStatement.setDouble(2, newAverage);
            preparedStatement.setString(3, partnerID);

            preparedStatement.executeUpdate();

            connection.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String getPartnerIDFromRequest(String requestID) {
        String parseQuery = "select PartnerID from Bookings where RequestID = ?";
        String partnerID = null;

        try {
            Connection connection = SQLiteConnection.connectDB();

            PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1, requestID);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next())
                partnerID = resultSet.getString("PartnerID");

            connection.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return partnerID;
    }


    public boolean viewBookingHistory(String consumerID) {
        try {
            Connection connection = SQLiteConnection.connectDB();

            String parseQuery = "select Bookings.RequestID,Bookings.PartnerID,Partners.Name,Partners.Profession,Partners.ContactNo,Bookings.DateOfRequest,Bookings.DateOfBooking,Bookings.Status,Bookings.Rating from Bookings INNER JOIN Partners ON Bookings.PartnerID=Partners.PartnerID where Bookings.ConsumerID=?";
            PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1, consumerID);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next() == false) {
                connection.close();
                return false;
            } else {
                System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
                System.out.println("RequestID       PartnerID               PartnerName                     Profession              ContactNo          DateOfRequest      DateOfBooking             Status              Rating");
                System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

                do {
                    //System.out.println(resultSet.getString("RequestID") +"              "+ resultSet.getString("PartnerID")+"                     "+resultSet.getString("Name")+"                   "+resultSet.getString("Profession")+"           "+resultSet.getString("ContactNo")+"           "+resultSet.getString("DateOfRequest")+"            "+resultSet.getString("DateOfBooking")+"            "+resultSet.getString("Status")+"           "+resultSet.getInt("Rating"));
                    System.out.printf("%8s    %10s    %25s         %30s     %10s      %18s    %18s   %12s                %d \n", resultSet.getString("RequestID"), resultSet.getString("PartnerID"), resultSet.getString("Name"), resultSet.getString("Profession"), resultSet.getString("ContactNo"), resultSet.getString("DateOfRequest"), resultSet.getString("DateOfBooking"), resultSet.getString("Status"), resultSet.getInt("Rating"));

                } while (resultSet.next());

                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

            }
            connection.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return true;
    }

    public boolean viewGuestBookingInfo(String guestID) {
        Connection connection = SQLiteConnection.connectDB();
        String parseQuery = "select Bookings.RequestID,Bookings.PartnerID,Partners.Name,Partners.Profession,Partners.ContactNo,Bookings.DateOfRequest,Bookings.DateOfBooking,Bookings.Status,Bookings.Rating from Bookings INNER JOIN Partners ON Bookings.PartnerID=Partners.PartnerID where Bookings.ConsumerID=?";

        boolean requestEnded = false;
        if (guestID != null) {
            try
            {
                PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
                preparedStatement.setString(1, guestID);


                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    if (resultSet.getString("Status").equals("Processed") || resultSet.getString("Status").equals("Cancelled"))
                        requestEnded = true;

                    System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
                    System.out.println("RequestID       PartnerID               PartnerName                     Profession              ContactNo          DateOfRequest      DateOfBooking             Status              Rating");
                    System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");


                    //System.out.println(resultSet.getString("RequestID") +"              "+ resultSet.getString("PartnerID")+"                     "+resultSet.getString("Name")+"                   "+resultSet.getString("Profession")+"           "+resultSet.getString("ContactNo")+"           "+resultSet.getString("DateOfRequest")+"            "+resultSet.getString("DateOfBooking")+"            "+resultSet.getString("Status")+"           "+resultSet.getInt("Rating"));
                    System.out.printf("%8s    %10s    %25s         %30s     %10s      %18s    %18s   %12s                %d \n", resultSet.getString("RequestID"), resultSet.getString("PartnerID"), resultSet.getString("Name"), resultSet.getString("Profession"), resultSet.getString("ContactNo"), resultSet.getString("DateOfRequest"), resultSet.getString("DateOfBooking"), resultSet.getString("Status"), resultSet.getInt("Rating"));

                    System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

                }
                connection.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
        }

        else
            System.out.println("Invalid Guest !");

        return requestEnded;

    }

    private boolean validateBooking(String clientID,String partnerID,LocalDateTime dateOfRequest,LocalDateTime dateOfBooking) {

        // check if booking date and time are on previous date and time and one an hour from now
        if(!DateUtils.validateHireDateAndTime(dateOfRequest,dateOfBooking))
            return false;

        String parseQuery = "select *from Bookings where PartnerID=? AND Status='Unprocessed'";

        boolean alreadyBooked = false;
        boolean slotAvailable = true;
        boolean afterBeforeTimeSlot = false;

        LocalDateTime bookingDateFromDB = null;
        LocalDateTime slotStartTime = null;
        LocalDateTime slotEndTime = null;

        try {
            Connection connection = SQLiteConnection.connectDB();
            PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1, partnerID);

            ResultSet resultSet = preparedStatement.executeQuery();

            DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            while (resultSet.next()) {

                String consumerIDfromDB = resultSet.getString("ConsumerID");
                String bookingDateinDB = resultSet.getString("DateOfBooking");
                bookingDateFromDB =LocalDateTime.parse(bookingDateinDB,pattern);

                // Assuming time slot for service be 90 minute
                slotStartTime = bookingDateFromDB;
                slotEndTime = slotStartTime.plusMinutes(90) ;

                // check for booking in the time slot
                if(!dateOfBooking.isBefore(slotStartTime) && !dateOfBooking.isAfter(slotEndTime))
                {
                    // Already booked by the hiring client
                    if (consumerIDfromDB.equals(clientID))
                    {
                        alreadyBooked = true;
                        break;
                    }
                    // Already hired for the same time slot by someone else
                    else {

                            alreadyBooked = true;
                            slotAvailable = false;
                            break;
                    }
                }
                // An hour before or after the time slot
                //else if(dateOfBooking.isBefore(slotStartTime.minusHours(1)) || dateOfBooking.isAfter(slotEndTime.plusHours(1)))
                else if((!dateOfBooking.isBefore(slotStartTime.minusMinutes(60)) && !dateOfBooking.isAfter(slotStartTime) || (!dateOfBooking.isBefore(slotEndTime) && !dateOfBooking.isAfter(slotEndTime.plusMinutes(60)))))
                {
                    afterBeforeTimeSlot = true;
                }
            }

            preparedStatement.close();
            connection.close();
        }
        catch (Exception e) {
            System.out.println("Check in validateBooking: "+e.getMessage());
        }

        return bookingStatus(alreadyBooked,slotAvailable,afterBeforeTimeSlot,bookingDateFromDB,slotStartTime,slotEndTime);
    }

    private boolean bookingStatus(boolean alreadyBooked,boolean slotAvailable,boolean afterBeforeTimeSlot,LocalDateTime bookingDateFromDB,LocalDateTime slotStartTime,LocalDateTime slotEndTime)
    {
        // check if already booked by someone else
        if (alreadyBooked && !slotAvailable) {

            DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter time = DateTimeFormatter.ofPattern("HH:mm");

            System.out.println("The Partner is booked by another client for the time slot !");
            System.out.println("Date: "+date.format(bookingDateFromDB)+"      From: " +time.format(slotStartTime) + "    To: "+time.format(slotEndTime) );

            System.out.printf("\nPlease try booking with another time slot!\n\n");
            return false;
        }

        // check if already booked by the client
        if (alreadyBooked) {
            System.out.printf("\nYou have already hired the Partner, Multiple booking for a partner is not possible on the same Date and Time !\n\n");
            return false;
        }

        // check an hour before or after the time slot
        if(afterBeforeTimeSlot)
        {
            System.out.println("\nSlot Unavailable due to prior time of Service, Try Another Time Slot !");
            return false;
        }

        System.out.println("\nBooking Validated ! Our UC Partner will reach out to you soon .\n\n");
        return true;
    }
}

