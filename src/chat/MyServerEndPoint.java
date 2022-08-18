package chat;

import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

class MyServerEndPoint{
    public static void main(String[] args) {
        new MyServer(77_77);
    }
}

class MyServer{
    private ServerSocket serverSocket;
    static Map<String, ClientModel> usersOnline = new HashMap<>();

    static int noOfConnections = 0;
    MyServer(int port){
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server Established at "+ port + "\nAwaiting Connections");
            while(true){
                new ClientModel(serverSocket.accept()).start();                                          // awaits fa connection
            }
        }catch (IOException exception){
            System.out.println("IOException Occurred " + exception);
        }
    }
    static class ClientModel extends Thread{
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String userName, password;
        PrintWriter printWriter;
        ClientModel(Socket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
            out = new PrintWriter(this.clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
        }
        @Override
        public void run() {
            try {
                switch (in.readLine()) {
                    case "Code-Login" -> login();
                    case "Code-CreateAccount" -> createAccount();
                }
            }catch (IOException e){
                System.out.println("Something went wrong, IOException");
            }
        }
        void createAccount(){
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter("C:\\Users\\mulla\\OneDrive\\Documents\\GitHub\\JavaDemo\\src\\com\\argent\\Data\\UserData.txt", true);
                fileWriter.append(in.readLine()).append("\n").append(in.readLine()).append("\n");
            }catch (IOException exception){
                System.out.println("error opening the file writer");
            }finally {
                try {
                    assert fileWriter != null;
                    fileWriter.close();
                }catch (IOException e){
                    System.out.println("Error in Closing the File Writter");
                }
            }
        }
        void login(){
            try{
                File userData = new File("C:\\Users\\mulla\\OneDrive\\Documents\\GitHub\\JavaDemo\\src\\com\\argent\\Data\\UserData.txt");
                Scanner userDataScanner = new Scanner(userData);
                boolean isNotUserExist = true;
                for(int i = 0; i < 2; i++) {
                    userName = in.readLine();
                    password = in.readLine();
                    while (userDataScanner.hasNextLine()){
                        if (userDataScanner.nextLine().equals(userName)) {
                            isNotUserExist = false;
                            if (userDataScanner.nextLine().equals(password)) {
                                out.println("Code-Verified");
                                MyServer.usersOnline.put(userName, this);
                                System.out.println("Connection NO." + usersOnline.size());
                                System.out.println(usersOnline);
                                supply();
                                return;
                            } else {
                                out.println("Code-InvalidPassword");
                            }
                        }
                    }
                    if (isNotUserExist){
                        out.println("Code-UserDoesn'tExist");
                    }
                }
                out.println("Code-AttemptExpired");
            } catch (IOException e) {
                System.out.println(userName + " has left the Chat.");
                usersOnline.remove(userName);
                printWriter.flush();
            }
        }
        void supply() throws IOException {
            String data, friendId = "";
            Map map = null;
            while (true) {
                data = in.readLine();
                JSONParser jsonParser = new JSONParser();
                try{
                    map = (Map) jsonParser.parse(data, new ContainerFactory() {
                        @Override
                        public Map createObjectContainer() {
                            return new LinkedHashMap();
                        }

                        @Override
                        public List creatArrayContainer() {
                            return new LinkedList();
                        }
                    });
                    friendId = (String) map.get("Receiver");
                }catch (ParseException parseException){
                    System.out.println("Parsing Exception");
                }
                try{
                    usersOnline.get(friendId).out.println("From, "+ userName + " : " + map.get("Message"));
                }catch (NullPointerException nullPointerException){
                    // The USER IS OFFLINE
                }finally {
                    printWriter = new PrintWriter(new FileWriter("Data\\ChatData.txt"));
                    printWriter.write(data);
                }
            }
        }
        @Override
        public String toString() {
            return userName;
        }
    }
}
