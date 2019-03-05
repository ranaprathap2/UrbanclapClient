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
            System.out.println("---------------------------------------------------------------");
            System.out.println("      URBANCLAP CLIENT APP - FIND ANYONE YOU NEED");
            System.out.println("---------------------------------------------------------------");
            System.out.println("1 -> LOGIN");
            System.out.println("2 -> REGISTER");
            System.out.println("3 -> EXIT");
            System.out.println("---------------------------------------------------------------");

            System.out.println();

            System.out.printf("Enter Your Choice : \n");

            // Handling mismatch without try catch alternative and simple
            if(InputUtils.checkIntegerMismatch(in))
            {
                choice = in.nextInt();

                if(choice == 1) {
                    int choose;
                    Scanner input = new Scanner(System.in);

                    do {
                        System.out.println("-------------------------------------------");
                        System.out.printf("Choose Your Type of Login : \n\n1 -> CLIENT\n2 -> GUEST\n3 -> OR TO EXIT\n");
                        System.out.printf("------------------------------------------\n");

                        System.out.print("Input Login Type : ");

                        // Handling mismatch without try catch alternative and simple
                        if (InputUtils.checkIntegerMismatch(input)) {
                            choose = input.nextInt();

                            if (choose == 1) {
                                Client client = new Client();

                                // If Client Login is successful load the dashboard, Else display Login error
                                if (client.loginUser())
                                    client.loadDashboard();
                                else
                                    System.out.println("Login failed, Try Again !");
                            } else if (choose == 2) {
                                // Load the dashboard directly for the guest
                                // No need for Registration
                                Guest guest = new Guest();
                                guest.loadDashboard();
                            }

                            else if(choose ==3)
                                break;

                            else
                                System.out.printf("\n\nInvalid Choice !\n\n");
                        }
                    } while (true);
                }

                // Load Client Registration Wizard
                else if(choice == 2)
                    new Client().registerClient();

                else if(choice == 3)
                    break;

                else
                    System.out.printf("\n\nInvalid Choice !\n\n");

                System.out.println();
            }
        }while(true);
    }
}
