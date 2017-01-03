package controllers;

import application.components.ControllerFunctionality;
import application.components.NetworkConnection;
import application.components.SocketEvent;
import application.components.User;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


public class LoginRegisterController implements ControllerFunctionality {

	@FXML private Parent loginWindow;
	@FXML private Parent registrationWindow;
	
	@FXML private TextField loginField;
	@FXML private TextField nameField;
	@FXML private TextField surnameField;
	@FXML private TextField passwordField;
	@FXML private TextField passwordRepeatField;
	//private boolean flag;

	private String message;
	

	public static void ConnectToServer() {
		System.out.println(" -> Trying to connect to the server: ");
		NetworkConnection.connect("localhost", 9090);
	}

	@FXML 
	public void initialize() {
		System.out.println(" -> initialize() - LoginRegisterController");
		// get a method to call it using reflection
		new Thread(() -> ConnectToServer()).start();
	}

	public void reqLogin() {

		String login = loginField.getText();
		String password = passwordField.getText();
		//flag = false;

		User u = new User(login, password);
		SocketEvent se = new SocketEvent("reqLogin", u);

		try {
			NetworkConnection.sendSocketEvent(se);
			SocketEvent res = NetworkConnection.rcvSocketEvent();
			String eventName = res.getName();
			
			if (eventName.equals("loginFailed")) {
				message = "Niepoprawny login lub hasło.";
			} else if (eventName.equals("loginSucceeded")) {
				//flag = true;
				//TODO
				// run in JavaFX after background thread finishes work
				MainController.setCurrentUser(res.getObject(User.class));
			}
		} catch (NullPointerException e) {
			message = "Nie można ustanowić połączenia z serwerem.";
		}
		
		Platform.runLater(new Runnable() {
			@Override public void run() {
				if (MainController.getCurrentUser() != null) {
					loadScene(loginWindow, "../fxml/mainLayout.fxml", 1200, 700, true, 1200, 700);
				} else {
					openDialogBox(loginWindow, message);
				}
			}
		});
	}

	public void reqRegister() {
		String login = loginField.getText();
		String password = passwordField.getText();
		String rePassword = passwordRepeatField.getText();
		String name = nameField.getText();
		String surname = surnameField.getText();

		if (password.equals(rePassword)) {
			User u = new User(login, password, name, surname);
			SocketEvent e = new SocketEvent("reqRegister", u);

			NetworkConnection.sendSocketEvent(e);
			SocketEvent res = NetworkConnection.rcvSocketEvent();

			message = res.getObject(String.class);
		} else {
			message = "Podane hasła nie są identyczne.";
		}

		// run in JavaFX after background thread finishes work
		Platform.runLater(new Runnable() {
			@Override public void run() {
				openDialogBox(registrationWindow, message);
			}
		});
	}

	@FXML private void registerBtn() {
		new Thread(() -> reqRegister()).start();
	}

	@FXML private void loginBtn() { // handler
		new Thread(() -> reqLogin()).start();
	}

	@FXML private void loginBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			new Thread(() -> reqLogin()).start();
		}
	}

	@FXML private void registerBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			new Thread(() -> reqRegister()).start();
		}
	}

	@FXML private void goToRegistrationKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			loadScene(loginWindow, "../fxml/registerLayout.fxml", 320, 300, false, 0, 0);
		}
	}

	@FXML private void cancelBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			loadScene(registrationWindow, "../fxml/loginLayout.fxml", 320, 250, false, 0, 0);
		}
	}

	@FXML private void goToLogin(Event event) {
		loadScene(registrationWindow, "../fxml/loginLayout.fxml", 320, 250, false, 0, 0);
	}

	@FXML private void goToRegistration(Event event) {
		loadScene(loginWindow, "../fxml/registerLayout.fxml", 320, 300, false, 0, 0);
	}
}
