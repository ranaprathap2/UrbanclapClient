import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class Guest extends Consumer {

    private String guestID;
    private String guestName;
    private String contactNo;
    private String eMailID;

    public void setGuestID(String guestID)
    {
        this.guestID = guestID;
    }

    public String getGuestID()
    {
        return guestID;
    }

    public String getGuestName()
    {
        return guestName;
    }

    public String getContactNo()
    {
        return contactNo;
    }

    public String geteMailID()
    {
        return eMailID;
    }

    public boolean addGuestToDB() {

        Scanner in = new Scanner(System.in);

        System.out.println("Logged in as Guest :");

        System.out.println("Enter Your Name : ");
        this.guestName = in.nextLine();

        System.out.println("Enter Your Contact No : ");
        this.contactNo = in.next();

        System.out.println("Enter Your eMail ID : ");
        this.eMailID = in.next();

        try
        {
            saveUserToDB();
            System.out.println("Guest Details added successfully");
            return true;
        }
        catch(Exception e)
        {
           System.out.println(e.getMessage());
        }

        return false;
    }

    @Override
    public void viewBookingInfo()
    {
        Scanner in = new Scanner(System.in);
        boolean requestEnded = false;

        System.out.println("Enter your Guest eMail ID: ");
        String mail = in.next();

        try
        {
            Connection connection = SQLiteConnection.connectDB();

            String parseQuery="select Bookings.RequestID,Bookings.PartnerID,Partners.Name,Partners.Profession,Partners.ContactNo,Bookings.DateOfRequest,Bookings.DateOfBooking,Bookings.Status,Bookings.Rating from Bookings INNER JOIN Partners ON Bookings.PartnerID=Partners.PartnerID where Bookings.ConsumerID=?";
            PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1,getGuestDetailsFromMail(mail));

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next())
                if(resultSet.getString("Status").equals("Processed") || resultSet.getString("Status").equals("Cancelled"))
                    requestEnded=true;

                System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
                System.out.println("RequestID       PartnerID               PartnerName                     Profession              ContactNo          DateOfRequest      DateOfBooking             Status              Rating");
                System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");


                    //System.out.println(resultSet.getString("RequestID") +"              "+ resultSet.getString("PartnerID")+"                     "+resultSet.getString("Name")+"                   "+resultSet.getString("Profession")+"           "+resultSet.getString("ContactNo")+"           "+resultSet.getString("DateOfRequest")+"            "+resultSet.getString("DateOfBooking")+"            "+resultSet.getString("Status")+"           "+resultSet.getInt("Rating"));
                System.out.printf("%8s    %10s    %25s         %30s     %10s      %18s    %18s   %12s                %d \n",resultSet.getString("RequestID"),resultSet.getString("PartnerID"),resultSet.getString("Name"),resultSet.getString("Profession"),resultSet.getString("ContactNo"),resultSet.getString("DateOfRequest"),resultSet.getString("DateOfBooking"),resultSet.getString("Status"),resultSet.getInt("Rating"));

                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");


            connection.close();
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }

        if(!requestEnded && readyToUpdateRequest())
            updateRequest();
    }

    public String getGuestDetailsFromMail(String mail)
    {
        String parseQuery="select *from Consumers where eMail=?";
        String guestID = null;

        try
        {
            Connection connection = SQLiteConnection.connectDB();
            PreparedStatement preparedStatement = connection.prepareStatement(parseQuery);
            preparedStatement.setString(1,mail);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()!=false)
            {
                guestID=resultSet.getString("ConsumerID");

                // Load the Guest details from the Mail
                this.guestID = guestID;
                this.guestName = resultSet.getString("Name");
                this.contactNo = resultSet.getString("ContactNo");
                this.eMailID = resultSet.getString("eMail");
            }

            connection.close();
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }

        return guestID;
    }
}
