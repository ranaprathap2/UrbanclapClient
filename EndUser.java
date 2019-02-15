interface EndUser
{
    // These methods are common to both the Client and the Guest
    void searchForServices(Consumer consumer);
    boolean readyToHire();
    void viewBookingInfo();
    void updateRequest();
    void loadDashboard();
}
