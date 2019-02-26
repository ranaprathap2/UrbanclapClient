import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    public boolean validateHireDateAndTime(Date dateOfRequest,Date dateOfBooking)
    {
        // booking on previous date and time from the current time is invalid
        // booking is possible for date and time after an hour from current request time

        SimpleDateFormat pattern = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        // advance date to an hour
        final long reqHoursInMillis = 1*60*60*1000; //one hour in millis
        Date validFromDate = new Date(dateOfRequest.getTime() + reqHoursInMillis);

        if(dateOfBooking.compareTo(validFromDate)>=0)
            return true;

        else if(dateOfBooking.compareTo(dateOfRequest)<0)
            System.out.println("Sorry, Booking on previous Date and Time is Invalid ! Retry");
        else
        {
            long diffInMillies = Math.abs(validFromDate.getTime()-dateOfBooking.getTime());
            long diff = TimeUnit.MINUTES.convert(diffInMillies,TimeUnit.MILLISECONDS);

            if(diff>=60)
                return true;
            else
                System.out.println("Sorry, Booking is possible only after an hour from now ! Retry");
        }
        return false;
    }


}
