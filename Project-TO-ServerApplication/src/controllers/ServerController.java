package controllers;

import application.components.SciConServer;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ServerController {

	private Thread serverThread;
	@FXML private TextArea textField;

	@FXML private void startServer() {
		SciConServer s = new SciConServer(9090);
		serverThread = new Thread(s);
		serverThread.start();

		textField.appendText("Server started.\n");
	}

	@FXML private void stopServer() {
		// serverThread.interrupt();
		textField.appendText("Server stopped.\n");
	}
}
