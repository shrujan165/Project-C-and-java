import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;

//Helper class//
class Helper {
    private PrintStream out;
    private HashMap <String , Pair<String,String>> credentials;
    private HashMap <String , Pair<String,String>> security_credentials;

    private Map<String, UserSession> userSessions; //Has all active users//
    private Map<String, SecuritySession> SecuritySessions; //Has all active security//
    private UserManagement user ;//UserManagement class has native methods as its methods//
    public Helper(PrintStream out, HashMap <String,Pair<String,String>> credentials,HashMap <String , Pair<String,String>> security_credentials) 
      { this.out = out;
        this.credentials = credentials;
        this.security_credentials=security_credentials;
        this.userSessions = new HashMap<>();
        this.SecuritySessions = new HashMap<>();
        user = new UserManagement();
        user.loadOrdersFromFile();
    }

    public void processCommands(Scanner scanner, String filePath) {
        ExecutorService executor = Executors.newCachedThreadPool();
        String command;
        String[] inputs;
        String password;
        String phNo;
        String securityId;
        UserSession userSession;
        int otp;
        SecuritySession securitySession;
        String service_name;
        
        while (scanner.hasNextLine()) {
            command = scanner.nextLine().trim(); 
            if (command.isEmpty()) {
                continue;  
            }

            inputs = parseCommands(command);

            if (inputs.length == 0) {
                System.out.println("Invalid command format: " + command); 
                continue;
            }

            //Switch case to process the commands//
            switch (inputs[0]) {
                case "ADD_USER":
                    if (inputs.length < 4) {
                        System.out.println("Invalid ADD_USER command");
                        break;
                    }
                    String newPhNo = inputs[1];
                    String newPassword = inputs[2];
                    String newName = inputs[3];
                    if (credentials.containsKey(newPhNo)) {
                        System.out.println("User already exists");
                    } else {
                        credentials.put(newPhNo, new Pair<>(newPassword,newName));
                        System.out.println("User added successfully");
                    }
                    break;

                case "LOGIN_USER":
                    if (inputs.length < 3) {
                        System.out.println("Invalid LOGIN_USER command");
                        break;
                    }
                    phNo = inputs[1];
                    password = inputs[2];
                    if (password.equals(credentials.get(phNo).getKey())) {
                        if(userSessions.containsKey(phNo))
                        {
                            System.out.println(phNo + " Already logged in");
                            continue;
                        }
                        
                        System.out.println("Successfully logged in: " + phNo);
                        userSession = new UserSession(phNo,user);
                        userSessions.put(phNo, userSession);
                        executor.submit(userSession); 
                    } else if (!credentials.containsKey(phNo)) {
                        System.out.println("No phNo found");
                    } else {
                        System.out.println("Wrong password");
                    }
                    break;
                case "LOGIN_SECURITY":
                    if (inputs.length < 3) {
                        System.out.println("Invalid LOGIN_SECURITY command");
                        break;
                    }
                    securityId = inputs[1];
                    password = inputs[2];
                    if (password.equals(security_credentials.get(securityId).getKey())) {
                        if(SecuritySessions.containsKey(securityId))
                        {
                            System.out.println(securityId + " Already logged in");
                            continue;
                        }
                        
                        System.out.println("Successfully logged in: " + securityId);
                        securitySession = new SecuritySession(securityId,user);
                        SecuritySessions.put(securityId, securitySession);
                        executor.submit(securitySession); 
                    } else if (!security_credentials.containsKey(securityId)) {
                        System.out.println("No Security Id found");
                    } else {
                        System.out.println("Wrong password");
                    }
                    break;
                case "LOGOUT_SECURITY" :
                    if (inputs.length < 2) {
                        System.out.println("Invalid LOGOUT_SECURITY command");
                        break;
                    }
                    String LogoutUser = inputs[1];
                    SecuritySession LogoutSession = SecuritySessions.get(LogoutUser);
                    if (LogoutSession != null) {
                        LogoutSession.logout();  
                        SecuritySessions.remove(LogoutUser); 
                    } else {
                        System.out.println("User not found or not logged in");
                    }
                    break;

                case "REMOVE_USER":
                    if (inputs.length < 2) {
                        System.out.println("Invalid REMOVE_USER command");
                        break;
                    }
                    String userToRemove = inputs[1];
                    if (credentials.remove(userToRemove) != null) {
                        System.out.println("User removed successfully");
                    } else {
                        System.out.println("User not found");
                    }
                    break;

                case "LOGOUT_USER":
                    if (inputs.length < 2) {
                        System.out.println("Invalid LOGOUT_USER command");
                        break;
                    }
                    String logoutUser = inputs[1];
                    UserSession logoutSession = userSessions.get(logoutUser);
                    if (logoutSession != null) {
                        logoutSession.logout();  
                        userSessions.remove(logoutUser); 
                    } else {
                        System.out.println("User not found or not logged in");
                    }
                    break;

                case "EXIT":
                    saveToFile(filePath);
                    executor.shutdownNow();
                    System.out.println("Exiting program and saving data");
                    return;
                case "ADD_ORDER" :
                    phNo = inputs[1];
                    otp = Integer.parseInt(inputs[2]);
                    service_name = inputs[3];
                    userSession = userSessions.get(phNo);
                    userSession.ADD_ORDER(otp,service_name);
                    break;
                case "MODIFY_ORDER" :
                    phNo = inputs[1];
                    otp = Integer.parseInt(inputs[2]);
                    service_name = inputs[3];
                    userSession = userSessions.get(phNo);
                    break;
                case "SHOW_ORDERS" :
                    phNo = inputs[1];
                    userSession = userSessions.get(phNo);
                    userSession.SHOW_ORDERS();
                    break;
                case "SEARCH_ORDERS" :
                    break;
                case "SHOW_USERS":
                    user.displayUsers();
                    break;
                case "REMOVE_ORDERS" :
                    phNo = inputs[1];
                    userSession = userSessions.get(phNo);
                    userSession.REMOVE_ORDERS();
                    break;
                case "MODIFY_OTP":
                    phNo = inputs[1];
                    userSession = userSessions.get(phNo);
                    userSession.MODIFY_OTP();
                    break;
                case "MODIFY_SERVICE":
                    phNo = inputs[1];
                    userSession = userSessions.get(phNo);
                    userSession.MODIFY_SERVICE();
                    break;
                case "ALL_ORDERS" :
                if (inputs.length < 2) {
                    System.out.println("Invalid LOGIN_USER command");
                    break;
                }
                phNo = inputs[1];
                if (SecuritySessions.containsKey(phNo)) {
                    securitySession = SecuritySessions.get(phNo);
                    securitySession.ALL_ORDERS();
                } else  {
                    System.out.println("LOGIN TO USE");
                } 
                    break;

                case "SEARCH":
                if (inputs.length < 2) {
                    System.out.println("Invalid LOGIN_USER command");
                    break;
                }
                phNo = inputs[1];
                if (SecuritySessions.containsKey(phNo)) {
                    securitySession = SecuritySessions.get(phNo);
                    securitySession.SEARCH();
                } else  {
                    System.out.println("LOGIN TO USE");
                } 
                    break;

                
                default:
                    System.out.println("INVALID COMMAND");
            }
        }
    }

    class UserSession implements Runnable {
        private String phNo;
        private BlockingQueue<String> commandQueue;
        private boolean loggedIn;
        private UserManagement user;
        Scanner scanner = new Scanner(System.in);
        public UserSession(String phNo,UserManagement user) {
            this.phNo = phNo;
            this.commandQueue = new LinkedBlockingQueue<>();
            this.loggedIn = true;
            String name = credentials.get(phNo).getValue();
            this.user =  user;
            addUSER();
        }
        public void addUSER()
        {
            user.addUser(credentials.get(phNo).getValue(),phNo);
        }
        public void logout() {
            this.loggedIn = false;
            commandQueue.offer("LOGOUT_USER"); 
        }
        public void ADD_ORDER(int otp,String ServiceName)
        {
            user.addOrderForUser(phNo,otp,ServiceName);
        }
        public void SHOW_ORDERS()
        {
            user.displayOrdersForUser(phNo);
        }
        public void REMOVE_ORDERS()
        {
            System.out.println("Choose the orderID of the order:");
            user.displayOrdersForUser(phNo);
            int orderID;
            orderID = scanner.nextInt();
            user.removeOrderFromUser(phNo,orderID-1);
        }
        public void MODIFY_OTP()
        {
            System.out.println("Choose the orderID of the order:");
            user.displayOrdersForUser(phNo);
            int orderID;
            int newOtp;
            orderID = scanner.nextInt();
            newOtp = scanner.nextInt();
            user.modifyOrderOtp(phNo,orderID-1,newOtp);
        }
        public void MODIFY_SERVICE()
        {
            System.out.println("Choose the orderID of the order:");
            user.displayOrdersForUser(phNo);
            int orderID;
            String newService;
            orderID = scanner.nextInt();
            newService = scanner.next();
            user.modifyOrderServiceName(phNo,orderID-1,newService);
        }




        @Override
        public void run() {
            while (loggedIn) {
                try {
                    String command = commandQueue.take(); 
                    if (command.equals("LOGOUT_USER"))
                    {
                        break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); 
                    break;
                }
            }
        }
    }

    class SecuritySession implements Runnable {
        private String securityId;
        private BlockingQueue<String> commandQueue;
        private boolean loggedIn;
        private UserManagement user;
        Scanner scanner = new Scanner(System.in);
        public SecuritySession(String securityId,UserManagement user) {
            this.securityId = securityId;
            this.commandQueue = new LinkedBlockingQueue<>();
            this.loggedIn = true;
            String name = security_credentials.get(securityId).getValue();
            this.user =  user;
        }

        public void logout() {
            this.loggedIn = false;
            commandQueue.offer("LOGOUT_USER"); 
        }
        public void ALL_ORDERS()
        {
            user.displayAllOrders();
        }
        public void SEARCH()
        {   
            System.out.println("Enter the phone number:");
            String number = scanner.next();
            user.searchByPhNo(number);
        }


        @Override
        public void run() {
            while (loggedIn) {
                try {
                    String command = commandQueue.take(); 
                    if (command.equals("LOGOUT_USER"))
                    {
                        break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); 
                    break;
                }
            }
        }
    }


    private void saveToFile(String filePath) {
        try {
            List<String> lines = new ArrayList<>();
            for (Map.Entry<String, Pair<String, String>> entry : credentials.entrySet()) {
                String phNo = entry.getKey();
                Pair<String, String> userDetails = entry.getValue();
                String password = userDetails.getKey();
                String name = userDetails.getValue();
                lines.add(phNo + " " + password + " " + name);
            }
            Files.write(Paths.get(filePath), lines);
        } catch (IOException e) {
            System.out.println("Error saving data to file: " + e.getMessage());
        }
    }
    

    public String[] parseCommands(String command) {
        ArrayList<String> tokens = new ArrayList<>();
        Matcher matcher = Pattern.compile("\"([^\"]*)\"|(\\S+)").matcher(command);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
               
                tokens.add(matcher.group(1));
            } else {
               
                tokens.add(matcher.group(2));
            }
        }

        return tokens.toArray(new String[0]);
    }
}