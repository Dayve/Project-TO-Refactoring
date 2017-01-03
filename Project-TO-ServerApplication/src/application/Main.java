package application;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	private Stage primaryStage;

	@Override public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("sciCon server");
		initServerLayout();
	}

	private void initServerLayout() {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("../fxml/ServerLayout.fxml"));
		try {
			Parent layout;
			layout = (Parent) loader.load();
			Scene scene = new Scene(layout);
			primaryStage.setResizable(false);
			primaryStage.setScene(scene);
			primaryStage.setMinWidth(320);
			primaryStage.setMinHeight(200);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
