import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    public static boolean validateHireDateAndTime(LocalDateTime dateOfRequest,LocalDateTime dateOfBooking)
    {
        // booking on previous date and time from the current time is invalid
        // booking is possible for date and time after an hour from current request time

        // advance date to an hour
        LocalDateTime validFromDate = dateOfRequest.plusHours(1);

        if(dateOfBooking.compareTo(validFromDate)>=0)
            return true;

        else if(dateOfBooking.compareTo(dateOfRequest)<0)
            System.out.println("Sorry, Booking on previous Date and Time is Invalid ! Retry");
        else
        {
            long diffInMillies =  Duration.between(validFromDate,dateOfBooking).toMillis();
            long diff = TimeUnit.MILLISECONDS.toMinutes(diffInMillies);

            if(diff>=60)
                return true;
            else
                System.out.println("Sorry, Booking is possible only after an hour from now ! Retry");
        }
        return false;
    }
}
