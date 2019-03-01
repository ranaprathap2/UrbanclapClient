import java.util.Scanner;
import java.util.InputMismatchException;

public class exceptionHandling {
    public static void main(String[] args) {

        Scanner s = new Scanner(System.in);
        try {
            System.out.print("Enter an integer: ");
            int num = s.nextInt();
            System.out.println("You entered " + num);
        } catch (InputMismatchException e) {
            System.out.println("You have entered invalid data");
        }
    }
}