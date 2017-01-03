package controllers;

import application.components.ControllerFunctionality;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class DialogController implements ControllerFunctionality {

	@FXML Parent dialogBoxWindow;
	@FXML private TextArea dialogMessage;


	@FXML public void initialize() {
		System.out.println(" -> initialize() - DialogController");
	}
	
	public void setDialogMessage(String text) {
		dialogMessage.setText(text);
	}

	@FXML private void closeBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			closeWindow(dialogBoxWindow);
		}
	}

	@FXML public void closeWindowBtn(ActionEvent event) {
		closeWindow(dialogBoxWindow);
	}
}
