package controllers.panels;

import application.components.ControllerFunctionality;
import application.components.NetworkConnection;
import controllers.MainController;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;


public class LogOffPanelController implements ControllerFunctionality {
	
	private MainController main;
	
	@FXML private Button logOffButton;
	@FXML private Label usernameLabel;
	
	public void init(MainController mainController) throws NullPointerException {
		if(mainController == null) throw new NullPointerException("LogOffPanelController: MainController instance is null");
		else main = mainController;
	}
	
	@FXML public void initialize() {
		System.out.println(" -> initialize() - LogOffPanelController");
	}
	
	@FXML private void logoutButtonAction() {
		// here check if login is valid
		new Thread(() -> logout()).start();
	}

	public void logout() {
		NetworkConnection.disconnect();
		
		Platform.runLater(new Runnable() {
			@Override public void run() {
				loadScene(main.applicationWindow, "../fxml/loginLayout.fxml", 320, 250, false, 0, 0);
			}
		});
	}
}
