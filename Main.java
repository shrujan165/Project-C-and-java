import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;

//Main class//
public class Main {
    public static void main(String[] args) {
        Matcher matcher;
        String[] filePath = {"./data/loginData","./data/SecurityLogin"};
        HashMap<String, Pair<String, String>> credentials = new HashMap<>();
        HashMap<String, Pair<String, String>> securityCredentials = new HashMap<>();
        List<HashMap<String, Pair<String, String>>> CREDENTIALS = new ArrayList<>();
        CREDENTIALS.add(credentials);
        CREDENTIALS.add(securityCredentials);

        Pattern pattern = Pattern.compile("^(.+?)\\s+(.+?)\\s+(.+)$");
        for (int i = 0; i < 2; i++) {
            try {
                List<String> lines = Files.readAllLines(Paths.get(filePath[i]));
                for (String line : lines) {
                    matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String username = matcher.group(1);
                        String password = matcher.group(2);
                        String name = matcher.group(3);
                        CREDENTIALS.get(i).put(username, new Pair<>(password, name));
                    } else {
                        System.out.println("Invalid line format: " + line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Helper helper = new Helper(System.out, credentials, securityCredentials);
        Scanner scanner = new Scanner(System.in);
        helper.processCommands(scanner, filePath[0]);
    }
}
