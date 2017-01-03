package controllers;

import java.time.LocalDate;

import application.components.ControllerFunctionality;
import application.enums.RequestType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class ConfirmationController implements ControllerFunctionality {
	
	private MainController main;
	
	@FXML Parent confirmationWindow;
	@FXML private TextArea confirmationMessage;
	
	private RequestType requestType;
	
	
	@FXML public void initialize() {
		System.out.println(" -> initialize() - ConfirmationController");
	}
	
	public void init(MainController mainController) throws NullPointerException {
		if(mainController == null) throw new NullPointerException("ConfirmationController: MainController instance is null");
		else main = mainController;
	}
	
	
	public void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}

	public void setConfirmationMessage(String text) {
		confirmationMessage.setText(text);
	}

	@FXML public void yesBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			main.makeRequest(requestType);
			closeWindow(confirmationWindow);
		}
	}

	@FXML public void yesBtn() {
		main.makeRequest(requestType);
		closeWindow(confirmationWindow);
	}

	@FXML public void noBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			closeWindow(confirmationWindow);
		}
	}

	@FXML public void noBtn(ActionEvent event) {
		closeWindow(confirmationWindow);
	}
}
