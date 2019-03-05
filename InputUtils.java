import java.util.Scanner;

public class InputUtils {

    public static boolean checkIntegerMismatch(Scanner in)
    {
        if(in.hasNextInt())
            return true;
        else
        {
            System.out.println("Invalid input, Please Try Again !");
            in.next();
            System.out.println();
            return false;
        }
    }
}
