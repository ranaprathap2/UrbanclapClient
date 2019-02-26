import java.util.Scanner;
public class Main {

    public static void main(String[] args)
    {
        loadMainMenu();
    }

    public static void loadMainMenu()
    {
        int choice; 
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

            System.out.printf("Enter Your Choice : \n");
            choice = in.nextInt();

            if(choice == 1)
            {
                int choose;
                Scanner input = new Scanner(System.in);

                System.out.println("-------------------------------------------");
                System.out.printf("Choose Your Type of Login : \n\n1 -> CLIENT\n2 -> GUEST\n");
                System.out.printf("------------------------------------------\n");

                System.out.print("Input Login Type : ");
                choose = input.nextInt();

                System.out.println();

                if(choose == 1)
                {
                    Client client = new Client();

                    // If Client Login is successful load the dashboard, Else display Login error
                    if(client.loginUser())
                        client.loadDashboard();
                    else
                        System.out.println("Login failed, Try Again !");
                }

                if(choose == 2)
                {
                    // Load the dashboard directly for the guest
                    // No need for Registration
                    Guest guest = new Guest();
                    guest.loadDashboard();
                }
            }

            // Load Client Registration Wizard
            if(choice == 2)
                new Client().registerClient();

        }while(choice != 3);
    }
}
