package controllers.panels;

import application.components.ControllerFunctionality;
import application.enums.MultipurposeButtonMode;
import application.enums.RequestType;
import controllers.MainController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;


public class ManagementPanelController implements ControllerFunctionality {
	
	private MainController main;
	
	@FXML private Button multipurposeButton; // "join in", "renounce/leave" and "manage" button
	@FXML private Button newConferenceButton;
	@FXML private Button deleteConferenceButton;
	
	private MultipurposeButtonMode currentMultipurposeButtonMode;
	
	public void setConferenceDeletePossible(boolean value) {
		deleteConferenceButton.setDisable(!value);
	}
	
	
	public void setMultipurposeButtonMode(MultipurposeButtonMode mode) {
		currentMultipurposeButtonMode = mode;
		switch(mode) {
			case JOIN_IN: {
				multipurposeButton.setText("Weź udział");
				multipurposeButton.setOnAction((event) -> {
					new Thread(() -> joinConference()).start();
				});
				break;
			}
			case RENOUNCE: {
				multipurposeButton.setText("Wycofaj się");
				multipurposeButton.setOnAction((event) -> { 
					new Thread(() -> leaveConference()).start(); 
				});
				break;
			}
			case MANAGE: {
				multipurposeButton.setText("Zarządzaj");
				multipurposeButton.setOnAction((event) -> manageConference());
				break;
			}
		}
	}
	
	
	public void manageConference() {
		if (TabsPanelController.getSelectedConferenceID() != null) {
			String selectedConfName = FeedPanelController.getConferenceByID(TabsPanelController.getSelectedConferenceID()).getName();
			openNewWindow(main.applicationWindow, "../fxml/conferenceManagerLayout.fxml", 
					650, 600, false, "Zarządzaj konferencją \"" + selectedConfName + "\"");
		}
	}
	
	
	public void leaveConference() {
		if (TabsPanelController.getSelectedConferenceID() != null) {
			String conferenceName = FeedPanelController.getConferenceByID(TabsPanelController.getSelectedConferenceID()).getName();
			String message = "Czy na pewno chcesz zrezygnować z udziału w konferencji \"" + conferenceName + "\"?";
			
			Platform.runLater(new Runnable() {
				@Override public void run() {
					openConfirmationWindow(main.applicationWindow, message, RequestType.REQUEST_LEAVING_CONFERENCE);
				}
			});
		}
	}
	
	
	public void joinConference() {
		if (TabsPanelController.getSelectedConferenceID() != null) {
			String conferenceName = FeedPanelController.getConferenceByID(TabsPanelController.getSelectedConferenceID()).getName();
			String message = "Czy na pewno chcesz wziąć udział w konferencji \"" + conferenceName + "\"?";
			
			Platform.runLater(new Runnable() {
				@Override public void run() {
					openConfirmationWindow(main.applicationWindow, message, RequestType.REQUEST_JOINING_CONFERENCE);
				}
			});
		}
	}
	
	
	@FXML public void removeConference() {
		if (TabsPanelController.getSelectedConferenceID() != null) {
			String conferenceName = FeedPanelController.getConferenceByID(TabsPanelController.getSelectedConferenceID()).getName();
			String message = "Czy na pewno chcesz usunąć konferencję \"" + conferenceName + "\"?";
			
			Platform.runLater(new Runnable() {
				@Override public void run() {
					openConfirmationWindow(main.applicationWindow, message, RequestType.REQUEST_REMOVING_CONFERENCE);
				}
			});
		}
	}
	
	
	@FXML public void addConference() {
		openNewWindow(main.applicationWindow, "../fxml/conferenceCreatorLayout.fxml", 600, 650, false, "Dodaj konferencję");
	}
	
	
	public void init(MainController mainController) throws NullPointerException {
		if(mainController == null) throw new NullPointerException("ManagementPanelController: MainController instance is null");
		else main = mainController;
	}
	
	
	@FXML public void initialize() {
		System.out.println(" -> initialize() - ManagementPanelController");
	}
}
