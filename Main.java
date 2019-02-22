import java.util.Scanner;

public class Main {

    public static void main(String[] args)
    {
        loadMainMenu();
    }

    public static void loadMainMenu()
    {
        int choice=0;
        Scanner in = new Scanner(System.in);

        do
        {
            System.out.println("-------------------------------------------");
            System.out.println("          URBANCLAP CLIENT APP");
            System.out.println("-------------------------------------------");
            System.out.println("1 -> LOGIN");
            System.out.println("2 -> REGISTER");
            System.out.println("3 -> EXIT");
            System.out.println("-------------------------------------------");

            System.out.println();

            System.out.print("Enter Your Choice : ");
            choice = in.nextInt();

            if(choice == 1)
            {
                Client client = new Client();

                if(client.loginClient())
                    client.loadDashboard();
                else
                    System.out.println("Login failed, Try Again !");
            }

            if(choice == 2)
                new Client().registerClient();

            if(choice == 3)
                break;

        }while(choice != 3);

    }

}
