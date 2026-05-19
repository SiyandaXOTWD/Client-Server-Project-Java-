package ClientServer.server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * 
 * @author Mr Mbokodo SB
 * @version Client-Server
 * 
 * 
 */

public class Server
{
	
	private ServerSocket serverSock; 
	private boolean running; 
	
	public Server(int port)
	{
		try {
			
			serverSock = new ServerSocket(port);
			System.out.println("Server connected on: " + port);
			running = true;
			
			while(running)
			{
				Thread thd = new Thread(new ServerHandler(serverSock.accept()));
				System.out.println("connected with client");
				thd.start();
			}
		
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}

    public static void main(String[] argv)
    {
    	Server s = new Server(2018);

    }
}
