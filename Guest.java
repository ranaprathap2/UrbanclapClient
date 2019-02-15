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
            saveUserToDB(this);
            System.out.println("Guest Details added successfully");
        }
        catch(Exception e)
        {
           System.out.println(e.getMessage());
        }

        return true;
    }

    @Override
    public void viewBookingInfo() {
       //invokes the listOngoingRequests in the super class common to both guest and client
        listOngoingRequests();
    }

}
