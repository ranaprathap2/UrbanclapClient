interface EndUser
{
    // These methods are common to both the Client and the Guest
    void searchForServices();
    boolean userReadyToHire();
    void viewBookingInfo();
    void updateRequest();
    void loadDashboard();
}
