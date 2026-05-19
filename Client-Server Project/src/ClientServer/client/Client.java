package ClientServer.client;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 
 * @author Mr Mbokodo SB
 * @version Client-Server
 * 
 * 
 */

public class Client extends Application
{
    public static void main(String[] args)
    {
    	launch(args);
    }

	@Override
	public void start(Stage primaryStage) throws Exception {
		//create the ClientPane, set up the Scene and Stage
		
		primaryStage.setTitle("Client-Server Project");
		ClientPane rootPane = new ClientPane();
		Scene scene = new Scene(rootPane,700,700);
		primaryStage.setScene(scene);
		primaryStage.show();
		
	}
}
