package application;
	
import java.util.Timer;

import application.components.ControllerFunctionality;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;


public class Main extends Application implements ControllerFunctionality {

	private Stage primaryStage;
	public static Timer timer;
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Project-TO-ClientApplication");
		
		loadScene(primaryStage, "../fxml/loginLayout.fxml", 320, 250, false, 0, 0);
	}
	
	@Override public void stop() {
		if (timer != null) {
			timer.cancel();
		}
		Platform.exit();
	}
	
	public static void main(String[] args) {
		System.out.println("> Project-TO-ClientApplication launched:");
		launch(args);
	}
}
