package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class MyClientEndPoint {
    public static void main(String[] args) throws IOException{
        Scanner scanner = new Scanner(System.in);
        MyClient myClient = new MyClient("127.0.0.1", 77_77);
        System.out.print("Press 1 tp SignIn\nPress 2 to SignUp : ");
        switch (scanner.nextInt()){
            case 1 -> myClient.pingServerLogin();
            case 2 -> myClient.pingServerCreateAccount();
        }
        myClient.close();
    }
}

record Chat(String friendId,    // Since Java SE 14
            String userId,
            String message,
            String time){
    String asJsonPacket(){
        StringBuilder jsonObject = new StringBuilder();
        jsonObject.append("{\"Sender\":\"");
        jsonObject.append(userId);
        jsonObject.append("\",\"Receiver\":\"");
        jsonObject.append(friendId);
        jsonObject.append("\",\"Time\":\"e\"");
        jsonObject.append(",\"Message\":\"");
        jsonObject.append(message);
        jsonObject.append("\"}");

        return jsonObject.toString();
    }
}

class LocalValidation{
    protected boolean isValidUserID(String userID){
        if (userID.length() > 15) return false;
        if (userID.length() < 8) return false;
        for(int i = 0; i < userID.length(); i++){
            if (('a' <= userID.charAt(i) && userID.charAt(i) <= 'z') || userID.charAt(i) == '_' || userID.charAt(i) == '.'){
            }else{
                return false;
            }
        }
        return true;
    }
    protected boolean isValidPassword(String passWord){
        if ((8 < passWord.length()) && (15 > passWord.length())){
            boolean chr = true, nbr = true;
            for(int i = 0; i < passWord.length(); i++){
                chr = (passWord.charAt(i) < 'a' || passWord.charAt(i) > 'z') || (passWord.charAt(i) < 'A' || passWord.charAt(i) > 'z');
                nbr = passWord.charAt(i) < '9';
                System.out.println(passWord.charAt(i) + " " + chr + " " + nbr);
                if (chr == nbr == true){    // to reduce the itra
                    return true;
                }
            }
        }
        return false;
    }
}
class MyClient extends LocalValidation{
    private String userID;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    MyClient(String ipAddress, int port){
        try {
            clientSocket = new Socket(ipAddress, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            Scanner scanner = new Scanner(System.in);
        }catch (IOException e){
            System.out.println("IOException Occurred");
        }
    }
    void pingServerCreateAccount() {
        String userName, passWord;
        Scanner scanner = new Scanner(System.in);
        out.println("Code-CreateAccount");
        while(true) {
            System.out.println("Note : The UserName can have only small letters and .(dot), _(underscore), length should be between 8 to 15");
            System.out.print("Enter the UserName : ");
            userName = scanner.next();
            if (isValidUserID(userName)) {
                out.println(userName);
                break;
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
                System.out.println("Invalid Username !");
            }
        }
        while(true) {
            System.out.println("Note : The Password must be greater than 8, must have atleast one symbol and a number");
            System.out.print("Enter the Password : ");
            passWord = scanner.next();
            if (isValidPassword(passWord)) {
                out.println(passWord);
                break;
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
                System.out.println("Invalid Password !");
            }
        }
        System.out.println("Home Screen");
        homeScreen();
    }
    void pingServerLogin() throws IOException {
        out.println("Code-Login");
        String passWord, serverResponds;
        Scanner scanner = new Scanner(System.in);
        do {
            System.out.println("Enter your UserName : ");
            userID = scanner.next();
            System.out.println("Enter your PassWord : ");
            passWord = scanner.next();

            out.println(userID);
            out.println(passWord);
            serverResponds = in.readLine();
            System.out.println("Responds : " + serverResponds);
            switch (serverResponds){
                case "Code-InvalidPassword" -> System.out.println("Invalid Password");
                case "Code-UserDoesn'tExist" -> System.out.println("User Id Doesn't Exist");
                case "Code-AttemptExpired" -> {
                    System.out.println("Attempt Expired");
                    return;
                }
            }
        }while (!serverResponds.equals("Code-Verified"));
        homeScreen();      // this call is only reachable when the server responds Verfied.
    }
    void homeScreen(){
        // TODO list out the friendList
        String friendID;
        Scanner scanner = new Scanner(System.in);
        //
                    System.out.println("Enter the UserName to chat with : ");
                    friendID = scanner.nextLine();
                    //out.println(friendID);
        // informing the server, will have to be removed.
        doChat(friendID);
    }
    void doChat(String friendId) {
        Scanner scanner = new Scanner(System.in);
                // friends user name to the server

        new Thread(){
            @Override
            public void run() {
                while (true){
                    out.println(new Chat(friendId, userID, scanner.nextLine(), "").asJsonPacket());
                    // TODO TERMINATE THE CHAT on the code
                }
            }
        }.start();

        while (true){
            try {
                System.out.println(in.readLine());
            } catch (IOException e) {
                System.out.println("Error in reading, thread");
            }
        }
    }
    void close() throws IOException{
        clientSocket.close();
        in.close();
        out.close();
    }
}