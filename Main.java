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

            System.out.printf("Enter Your Choice : \n");

            // Handling mismatch without try catch alternative and simple
            if(in.hasNextInt())
            {
                choice = in.nextInt();

                if(!(choice>0 && choice<4))
                {
                    System.out.printf("\n\nInvalid Choice !\n\n");
                    continue;
                }
            }
            else
            {
                System.out.println("Invalid input, Please Try Again !");
                in.next();
                System.out.println();
                continue;
            }

            if(choice == 1)
            {
                int choose;
                Scanner input = new Scanner(System.in);

                do {
                    System.out.println("-------------------------------------------");
                    System.out.printf("Choose Your Type of Login : \n\n1 -> CLIENT\n2 -> GUEST\n");
                    System.out.printf("------------------------------------------\n");

                    System.out.print("Input Login Type : ");

                    // Handling mismatch without try catch alternative and simple
                    if(input.hasNextInt())
                    {
                        choose = input.nextInt();

                        if((choose!=1 && choose!=2))
                        {
                            System.out.printf("\n\nInvalid Choice !\n\n");
                            continue;
                        }
                        else
                            break;
                    }
                    else
                    {
                        System.out.println("Invalid input, Please Try Again !");
                        input.next();
                        System.out.println();
                        continue;
                    }

                }while(true);

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
