package ClientServer.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * @author Mr Mbokodo SB
 * @version Client-Server
 */
public class ClientPane extends GridPane {
    private Label lblUser;
    private TextField txtUser;
    private Label lblPass;
    private TextField txtPass;
    private Button btnLogIN;
    private Button btnList;
    private TextArea txtList;
    private Label lblDownload;
    private TextField txtDownload;
    private Button btnDownload;
    private Button btnLogOff;
    private TextArea txtResponse;
    
    private BufferedReader txtIn = null;
    private PrintWriter txtOut = null;
    private DataInputStream dIn = null;
    private InputStream is = null;
    private OutputStream os = null;

    private Socket clientSock = null;
    private String[] listData;

    public ClientPane() {
        SetUp();

        btnLogIN.setOnAction(e -> {
            try {
                
                clientSock = new Socket("localhost", 2018);
                txtResponse.appendText("CONNECTED TO SERVER \n");
                
                is = clientSock.getInputStream();
                os = clientSock.getOutputStream();

                txtIn = new BufferedReader(new InputStreamReader(is));
                txtOut = new PrintWriter(os);
                dIn = new DataInputStream(is);

                String user = txtUser.getText();
                String pass = txtPass.getText();

                txtOut.println("LOGIN " + user + " " + pass);
                txtOut.flush();

                txtResponse.appendText(txtIn.readLine() + "\n");

            } catch (IOException ex) {
                txtResponse.appendText("500 COULD NOT CONNECT TO SERVER \n");
            }
        });

        btnList.setOnAction(e1 -> {
            if (txtOut == null) {
                txtResponse.appendText("ERROR: Not connected to server.\n");
                return;
            }
            txtOut.println("LIST");
            txtOut.flush();

            try {
                String response = txtIn.readLine();
                if (response != null && !response.isEmpty()) {
                    txtList.clear(); 
                    listData = response.split("#");
                    for (int s = 0; s < listData.length; s++) {
                        txtList.appendText(listData[s] + "\r\n");
                    }
                    txtResponse.appendText("LIST RECEIVED \n");
                }
            } catch (IOException ex) {
                txtResponse.appendText("LIST NOT RECEIVED \n");
            }
        });

        btnDownload.setOnAction(e3 -> {
            if (txtOut == null || txtIn == null) {
                txtResponse.appendText("ERROR: Connect and authenticate first.\n");
                return;
            }

            String inputIdText = txtDownload.getText().trim();
            if (inputIdText.isEmpty()) {
                txtResponse.appendText("ERROR: Please enter an ID.\n");
                return;
            }

            txtOut.println("PDFRET " + inputIdText);
            txtOut.flush();
            txtResponse.appendText("RETRIEVE REQUEST SENT \n");

            try {
                
                String responseLine = txtIn.readLine();
                if (responseLine == null) {
                    txtResponse.appendText("ERROR: Server closed connection.\n");
                    return;
                }
                
                int fileSize = Integer.parseInt(responseLine.trim());
                if (fileSize <= 0) {
                    txtResponse.appendText("FILE NOT FOUND ON SERVER OR EMPTY\n");
                    return;
                }
                
                String fileName = "";
                if (listData != null) {
                    for (String s : listData) {
                        StringTokenizer tok = new StringTokenizer(s);
                        if (tok.hasMoreTokens()) {
                            String fId = tok.nextToken();
                            if (fId.equals(inputIdText) && tok.hasMoreTokens()) {
                                fileName = tok.nextToken();
                                break;
                            }
                        }
                    }
                }

                
                if (fileName.isEmpty()) {
                    fileName = "downloaded_track_" + inputIdText + ".mp3"; 
                }

                // Ensure data local container tree target points somewhere valid
                File localDir = new File("data/client");
                if (!localDir.exists()) {
                    localDir.mkdirs();
                }

                File downloadedFile = new File(localDir, fileName);
                FileOutputStream fOut = new FileOutputStream(downloadedFile);
                
                byte[] buffer = new byte[2048];
                int totalByte = 0;
                int n = 0;
                
                // Read clean sequential bytes relative to file constraints sizing block
                while (totalByte < fileSize) {
                    int remaining = fileSize - totalByte;
                    int bytesToRead = Math.min(buffer.length, remaining);
                    
                    n = dIn.read(buffer, 0, bytesToRead);
                    if (n == -1) {
                        break; // Stream ended unexpectedly
                    }
                    fOut.write(buffer, 0, n);
                    totalByte += n;
                }
                
                fOut.flush();
                fOut.close();
                System.out.println("FILE DOWNLOADED \n");
                txtResponse.appendText("FILE RECEIVED \n" + downloadedFile.getName() + "\n");
                
            } catch (IOException e2) {
                e2.printStackTrace();
                txtResponse.appendText("IO ERROR OCCURRED DURING DOWNLOADING \n");
            } catch (NumberFormatException ex) {
                txtResponse.appendText("FILE NOT RECEIVED (Parsing Error) \n");
            }
        });

        btnLogOff.setOnAction(e4 -> {
            if (txtOut == null) return;
            try {
                txtOut.println("LOGOUT");
                txtOut.flush();
              
                txtResponse.appendText("\n" + txtIn.readLine() + "\n");
            
                txtIn.close();
                txtOut.close();
                dIn.close();
                if (clientSock != null) clientSock.close();
                
                txtOut = null; // Clean active reference
            } catch (IOException e2) {
                e2.printStackTrace(); 
            }
        });
    }

    public void SetUp() {
        setHgap(10);
        setVgap(10);
        setAlignment(Pos.CENTER);

        lblUser = new Label("Username: ");
        add(lblUser, 0, 0);
        txtUser = new TextField();
        add(txtUser, 1, 0);

        lblPass = new Label("Password: ");
        add(lblPass, 0, 1);
        txtPass = new TextField();
        add(txtPass, 1, 1);

        btnLogIN = new Button("LOG IN");
        add(btnLogIN, 1, 2);

        btnList = new Button("LIST");
        add(btnList, 0, 3);

        txtList = new TextArea();
        add(txtList, 0, 4, 2, 2);

        lblDownload = new Label("File ID to download: ");
        add(lblDownload, 0, 6);
        txtDownload = new TextField();
        add(txtDownload, 1, 6);
        btnDownload = new Button("DOWNLOAD");
        add(btnDownload, 1, 7);

        btnLogOff = new Button("LOG OFF");
        add(btnLogOff, 0, 8);
        txtResponse = new TextArea();
        add(txtResponse, 0, 9, 2, 2);
    }
}