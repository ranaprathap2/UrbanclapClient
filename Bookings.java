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
    private String clientID;
    private String partnerID;
    private Date  dateOfRequest;
    private Date dateOfBooking;
    private String status;
    private int rating;

    Bookings()
    {
        requestID="";
        clientID="";
        partnerID="";
        dateOfRequest = new Date();
        dateOfBooking = new Date();
        status = "Unprocessed";
        rating = 0;
    }

    public void makeRequest(Client client)
    {
        String bookingDateAsString=null;

        Scanner in = new Scanner(System.in);

        requestID = getRequestID();

        System.out.println("Enter Partner ID : ");
        partnerID = in.next();

        clientID = client.getLoggedInClientID();

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
        }while(!new DateUtils().validateHireDateAndTime(dateOfRequest,dateOfBooking));

        saveRequestToDB();
    }

        public void saveRequestToDB()
    {
        Connection connection = null;
        PreparedStatement pstmt = null;

        SimpleDateFormat pattern = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String sql = "INSERT INTO Bookings(RequestID,ClientID,PartnerID,DateOfRequest,DateOfBooking,Status) VALUES(?,?,?,?,?,?)";

        try
        {
            connection = SQLiteConnection.connectDB();

            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, requestID);
            pstmt.setString(2, clientID);
            pstmt.setString(3, partnerID);
            pstmt.setString(4, pattern.format(dateOfRequest));
            pstmt.setString(5, pattern.format(dateOfBooking));
            pstmt.setString(6, status );

            pstmt.executeUpdate();

            System.out.println("Request Successful !");
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        finally {
            try
            {
                pstmt.close();
                connection.close();
            }
            catch (Exception e)
            {
                System.out.println(e);
            }
        }
    }

    private int generateRequestID(String passQuery)
    {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        int getValue = 0;

        try
        {
            connection = SQLiteConnection.connectDB();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(passQuery);

            if(resultSet.next())
            {
                getValue = Integer.parseInt(resultSet.getString(1));
            }
        }

        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        finally {
            try{
                connection.close();
            }
            catch (Exception e)
            {
                System.out.println(e);
            }
        }
        return getValue;
    }

    private String getRequestID()
    {
        String newID = "RQ-" + generateRequestID("select count(RequestID)+1 from Bookings");

        return newID;
    }

    public boolean getOngoingRequests(String clientID)
    {
        //String parseQuery="select *from Bookings where ClientID = ? and Status = 'Unprocessed'";
        String parseQuery="select Bookings.RequestID,Bookings.PartnerID,Partners.Name,Partners.Profession,Partners.ContactNo,Bookings.DateOfRequest,Bookings.DateOfBooking from Bookings INNER JOIN Partners ON Bookings.PartnerID=Partners.PartnerID AND Bookings.Status='Unprocessed' AND Bookings.ClientID=?";
        boolean resultSetExist=true;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try
        {
            connection = SQLiteConnection.connectDB();
            preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1,clientID);

            resultSet = preparedStatement.executeQuery();

            if(resultSet.next()==false)
                resultSetExist=false;
            else
            {
                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------");
                System.out.println("RequestID     PartnerID                 PartnerName                     Profession             ContactNo           DateOfRequest         DateOfBooking ");
                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------");
                do{
                    //System.out.println(resultSet.getString("RequestID") +"          "+resultSet.getString("PartnerID")+"            "+resultSet.getString("Name")+"             "+resultSet.getString("Profession")+"           "+resultSet.getString("ContactNo")+"           "+resultSet.getString("DateOfRequest")+"          "+resultSet.getString("DateOfBooking"));
                    System.out.printf("%8s    %10s    %25s         %30s     %10s      %18s    %18s \n",resultSet.getString("RequestID"),resultSet.getString("PartnerID"),resultSet.getString("Name"),resultSet.getString("Profession"),resultSet.getString("ContactNo"),resultSet.getString("DateOfRequest"),resultSet.getString("DateOfBooking"));

                }while(resultSet.next());

                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------");

                System.out.println();
            }
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        finally {
            try
            {
                preparedStatement.close();
                resultSet.close();
                connection.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }

            return resultSetExist;
        }
    }

    public void updateBookingRequest(int mapRequest,String requestID) {
        String parseQuery = "update Bookings set Status=? where RequestID=?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = SQLiteConnection.connectDB();

            preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1, keyRequestMap(mapRequest));
            preparedStatement.setString(2, requestID);

            preparedStatement.executeUpdate();
            System.out.println("Update Successful !");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                preparedStatement.close();
                connection.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
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

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = SQLiteConnection.connectDB();

            preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setInt(1, rating);
            preparedStatement.setString(2, requestID);

            preparedStatement.executeUpdate();
            System.out.println("Thanks for Your Rating !");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                preparedStatement.close();
                connection.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        updatePartnerRating(rating, requestID);
    }

    private void updatePartnerRating(int newRating,String requestID)
    {
        String partnerID = getPartnerIDFromRequest(requestID);

        String parseQuery = "select ExperienceServing,AverageRating from Partners where PartnerID=?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        int experienceServing = 0;
        double newAverage = 0;

        try {
            connection = SQLiteConnection.connectDB();

            preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1, partnerID);
            resultSet = preparedStatement.executeQuery();

            experienceServing = resultSet.getInt("ExperienceServing");
            float overallAverage = (experienceServing) * resultSet.getFloat("AverageRating");

            experienceServing+=1;
            newAverage = (overallAverage + newRating)/(experienceServing);

            //truncate new average
            int truncNewAvg = (int)(newAverage*10);
            newAverage = truncNewAvg/10d;

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                preparedStatement.close();
                connection.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        update(experienceServing,newAverage,partnerID);
    }

    private void update(int experienceServing,double newAverage,String partnerID)
    {
        String parseQuery = "update Partners SET ExperienceServing=?,AverageRating=? WHERE PartnerID=?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = SQLiteConnection.connectDB();

            preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setInt(1, experienceServing);
            preparedStatement.setDouble( 2, newAverage);
            preparedStatement.setString(3, partnerID);

            preparedStatement.executeUpdate();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                preparedStatement.close();
                connection.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

    }

    private String getPartnerIDFromRequest(String requestID)
    {
        String parseQuery = "select PartnerID from Bookings where RequestID = ?";
        String partnerID = null;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = SQLiteConnection.connectDB();

            preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1, requestID);

            resultSet = preparedStatement.executeQuery();

            if(resultSet.next())
                partnerID = resultSet.getString("PartnerID");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                preparedStatement.close();
                connection.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        return partnerID;
    }

    public boolean getBookingHistory(String clientID)
    {
        String parseQuery="select Bookings.RequestID,Bookings.PartnerID,Partners.Name,Partners.Profession,Partners.ContactNo,Bookings.DateOfRequest,Bookings.DateOfBooking,Bookings.Status,Bookings.Rating from Bookings INNER JOIN Partners ON Bookings.PartnerID=Partners.PartnerID where Bookings.ClientID=?";
        boolean resultSetExist=true;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try
        {
            connection = SQLiteConnection.connectDB();
            preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1,clientID);

            resultSet = preparedStatement.executeQuery();

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
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        finally {
            try
            {
                preparedStatement.close();
                resultSet.close();
                connection.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
        return resultSetExist;
    }
}

