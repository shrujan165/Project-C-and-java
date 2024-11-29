import java.io.PrintStream;

//Class to load the library and declare the native methods//
public class UserManagement {
    private static PrintStream out = System.out;

    public static void print(String k) {
        out.println(k);
    }

    static {
        System.loadLibrary("UserManagement");
    }

    public native void addUser(String phNo, String name);
    public native void displayUsers();
    public native void removeUser(String phNo);
    public native void addOrderForUser(String phNo, int otp, String serviceName);
    public native void displayOrdersForUser(String phNo);
    public native void removeOrderFromUser(String phNo, int index);
    public native void modifyOrderOtp(String phNo, int orderID, int newOTP);
    public native void modifyOrderServiceName(String phNo, int orderID, String serviceName);
    public native void displayAllOrders();
    public native void searchByPhNo(String phNo);
    public native void loadOrdersFromFile();
}
