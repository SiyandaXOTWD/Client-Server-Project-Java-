package ClientServer.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * @author Mr Mbokodo SB
 * @version Client-Server 
 */
public class ServerHandler implements Runnable {
    private BufferedReader readin = null;
    private PrintWriter writer = null;
    private DataOutputStream dout = null;
    private InputStream is = null;
    private OutputStream os = null;
    private boolean processing;
    private boolean Auth;
    private Socket connection = null;

    public ServerHandler(Socket newConnectionToClient) {
        this.connection = newConnectionToClient;
        try {
            is = connection.getInputStream();
            os = connection.getOutputStream();

            readin = new BufferedReader(new InputStreamReader(is));
            writer = new PrintWriter(os);
            dout = new DataOutputStream(os);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        processing = true;
        try {
            while (processing) {
                String message = readin.readLine();
                if (message == null) {
                    break; // Client disconnected abruptly
                }
                
                System.out.println("Message received: " + message);
                StringTokenizer st = new StringTokenizer(message);
                if (!st.hasMoreTokens()) continue;
                
                String command = st.nextToken();

                if (command.equalsIgnoreCase("LOGIN")) {
                    String username = st.hasMoreTokens() ? st.nextToken() : "";
                    String password = st.hasMoreTokens() ? st.nextToken() : "";

                    if (matchUser(username, password)) {
                        Auth = true;
                        writer.println("<200> LOGGED IN. YOU MAY PROCEED");
                        writer.flush();
                    } else {
                        Auth = false;
                        writer.println("500 INVALID USERNAME OR PASSWORD. PLEASE TRY AGAIN");
                        writer.flush();
                    }

                } else if (command.equalsIgnoreCase("LIST")) {
                    if (Auth) {
                        String list = "";
                        ArrayList<String> arrList = getFileList();
                        for (String l : arrList) {
                            list += l + "#";
                        }
                        writer.println(list);
                        writer.flush();
                    } else {
                        writer.println("500 Not Authenticated");
                        writer.flush();
                    }

                } else if (command.equalsIgnoreCase("PDFRET")) {
                    String fID = st.nextToken();
                    System.out.println("ID requested: " + fID);
                    
                    String fileName = idToFile(fID);
                    File returnFile = new File("data/server/" + fileName);
                    System.out.println("Target file: " + returnFile.getAbsolutePath());
                    
                    if (returnFile.exists() && !returnFile.isDirectory() && !fileName.isEmpty()) {
                        writer.println(returnFile.length());
                        writer.flush();

                        FileInputStream fin = new FileInputStream(returnFile);
                        byte[] buffer = new byte[2048];
                        int n = 0;
                        while ((n = fin.read(buffer)) > 0) {
                            dout.write(buffer, 0, n);
                        }
                        dout.flush(); // Flush the binary stream after completing write operations
                        fin.close();
                        System.out.println("200 SENT: " + fileName);
                    } else {
                        writer.println("0"); // Tell client file size is 0 (not found)
                        writer.flush();
                        System.out.println("404 FILE NOT FOUND.");
                    }

                } else if (command.equalsIgnoreCase("LOGOUT")) {
                    Auth = false;
                    writer.println("200 LOGGED OUT");
                    writer.flush();
                    
                    processing = false;
                    closeConnections();
                    System.out.println("200 LOGGED OUT");
                }
            }
        } catch (IOException e) {
            System.err.println("Connection aborted.");
        } finally {
            closeConnections();
        }
    }

    private void closeConnections() {
        try {
            if (dout != null) dout.close();
            if (readin != null) readin.close();
            if (writer != null) writer.close();
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean matchUser(String username, String password) {
        boolean found = false;
        File userFile = new File("data/server/users.txt");
        try (Scanner scan = new Scanner(userFile)) {
            while (scan.hasNextLine() && !found) {
                String line = scan.nextLine();
                String[] lineSec = line.split("\\s+");
                if (lineSec.length >= 2 && lineSec[0].equals(username) && lineSec[1].equals(password)) {
                    found = true;
                }
            }
        } catch (IOException ex) {
            System.err.println("users.txt file not found");
        }
        return found;
    }

    private ArrayList<String> getFileList() {
        ArrayList<String> result = new ArrayList<>();
        File lstFile = new File("data/server/PdfList.txt");
        try (Scanner scan = new Scanner(lstFile)) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine().trim();
                if (!line.isEmpty()) {
                    result.add(line);
                }
            }
        } catch (IOException ex) {
            System.err.println("PdfList.txt not found");
        }
        return result;
    }

    private String idToFile(String ID) {
        String result = "";
        File lstFile = new File("data/server/PdfList.txt");
        try (Scanner scan = new Scanner(lstFile)) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine().trim();
                if (line.isEmpty()) continue;
                
                StringTokenizer token = new StringTokenizer(line);
                if (token.hasMoreTokens()) {
                    String id = token.nextToken();
                    if (id.equals(ID) && token.hasMoreTokens()) {
                        result = token.nextToken();
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            System.err.println("PdfList.txt could not be scanned for ID resolution.");
        }
        return result;
    }
}