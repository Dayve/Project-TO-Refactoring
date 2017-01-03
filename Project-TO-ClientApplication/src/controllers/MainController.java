package controllers;

import application.Main;
import application.components.Conference;
import application.components.ControllerFunctionality;
import application.components.NetworkConnection;
import application.components.SocketEvent;
import application.components.User;
import application.enums.RequestType;
import controllers.panels.CalendarPanelController;
import controllers.panels.FeedPanelController;
import controllers.panels.LogOffPanelController;
import controllers.panels.ManagementPanelController;
import controllers.panels.TabsPanelController;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;


public class MainController implements ControllerFunctionality {
	
	@FXML public Parent applicationWindow;

	@FXML private CalendarPanelController calendarPanelController;
	@FXML private FeedPanelController feedPanelController;
	@FXML private LogOffPanelController logOffPanelController;
	@FXML private ManagementPanelController managementPanelController;
	@FXML private TabsPanelController tabsPanelController;
	
	public static User currentUser;
	private static LinkedBlockingQueue<RequestType> requestQueue = new LinkedBlockingQueue<RequestType>();
	
	private int checkedRequestsWithoutUpdate = 0;
	
	
	public static void makeRequest(RequestType newRequest) {
		requestQueue.add(newRequest);
	}
	
	
	public TabsPanelController getTabsPanelController() {
		return tabsPanelController;
	}
	
	
	public FeedPanelController getFeedPanelController() {
		return feedPanelController;
	}
	
	
	public ManagementPanelController getManagementPanelController() {
		return managementPanelController;
	}
	
	
	public CalendarPanelController getCalendarPanelController() {
		return calendarPanelController;
	}
	

	@FXML public void initialize() {
		System.out.println(" -> initialize() - MainController");
		System.out.print(" -> Setting MainController instance reference in panel controllers: ");
				
		try {
			calendarPanelController.init(this);
			feedPanelController.init(this);
			logOffPanelController.init(this);
			managementPanelController.init(this);
			tabsPanelController.init(this);
			
			System.out.println("panel controllers initialized");
			
			feedPanelController.reqConferenceFeed();
			setupTimer();
			calendarPanelController.refreshCalendarTable(FeedPanelController.getFeed());
		}
		catch (NullPointerException nullPointerException) {
			System.out.println("exception was thrown while initializing panel controllers");
			nullPointerException.printStackTrace();
		}
	}
	
	
	// Sets the timer up - every second timer checks requestsQueue, which contains
	// tasks from other controllers for ApplicationController to perform:
	private void setupTimer() {
		Main.timer = new Timer();
		
		Main.timer.scheduleAtFixedRate(new TimerTask() {
			@Override public void run() {
				Platform.runLater(new Runnable() {
					@Override public void run() {
						if (requestQueue.contains(RequestType.UPDATE_CONFERENCE_FEED) || checkedRequestsWithoutUpdate > 10) {
							feedPanelController.reqConferenceFeed();
							checkedRequestsWithoutUpdate = 0;
							requestQueue.remove(RequestType.UPDATE_CONFERENCE_FEED);
						} else checkedRequestsWithoutUpdate++;

						if (requestQueue.contains(RequestType.REQUEST_JOINING_CONFERENCE)) {
							reqJoinConference();
							requestQueue.remove(RequestType.REQUEST_JOINING_CONFERENCE);
						}

						if (requestQueue.contains(RequestType.REQUEST_LEAVING_CONFERENCE)) {
							reqLeaveConference();
							requestQueue.remove(RequestType.REQUEST_LEAVING_CONFERENCE);
						}

						if (requestQueue.contains(RequestType.REQUEST_REMOVING_CONFERENCE)) {
							reqRemoveConference();
							requestQueue.remove(RequestType.REQUEST_REMOVING_CONFERENCE);
						}
					}
				});
			}
		}, 0, 500);
	}
	
	
	// actual request for joining a conference
	private void reqJoinConference() {
		ArrayList<Integer> userIdConferenceId = new ArrayList<Integer>();
		userIdConferenceId.add(currentUser.getId());
		userIdConferenceId.add(TabsPanelController.getSelectedConferenceID());

		SocketEvent se = new SocketEvent("reqJoinConference", userIdConferenceId);
		NetworkConnection.sendSocketEvent(se);

		SocketEvent receivedSocketEvent = NetworkConnection.rcvSocketEvent();
		String eventName = receivedSocketEvent.getName();
		
		final String message;
		
		if (eventName.equals("joinConferenceSucceeded")) {
			feedPanelController.reqConferenceFeed();
			message = "Wysłano prośbę o udział w konferencji do jej organizatora.";
		} else {
			System.out.println("MainController.reqJoinConference(): eventName=" + eventName);
			message = "Nie udało się dołączyć do konferencji.";
		}

		Platform.runLater(new Runnable() {
			@Override public void run() {
				openDialogBox(applicationWindow, message);
			}
		});
	}
	
	
	// actual request for leaving a conference
	private void reqLeaveConference() {
		ArrayList<Integer> userIdConferenceId = new ArrayList<Integer>();
		userIdConferenceId.add(currentUser.getId());
		userIdConferenceId.add(tabsPanelController.getSelectedConferenceID());

		SocketEvent se = new SocketEvent("reqLeaveConference", userIdConferenceId);
		NetworkConnection.sendSocketEvent(se);

		SocketEvent res = NetworkConnection.rcvSocketEvent();
		String eventName = res.getName();
		
		final String message;
		
		if (eventName.equals("leaveConferenceSucceeded")) {
			feedPanelController.reqConferenceFeed();
			message = "Zrezygnowałeś z udziału w konferencji.";
		} else {
			System.out.println("MainController.reqLeaveConference(): eventName=" + eventName);
			message = "Nie udało się zrezygnować z udziału w konferencji.";
		}

		Platform.runLater(new Runnable() {
			@Override public void run() {
				openDialogBox(applicationWindow, message);
			}
		});
	}
	
	
	// actual request for leaving a conference
	private void reqRemoveConference() {
		SocketEvent se = new SocketEvent("reqRemoveConference", tabsPanelController.getSelectedConferenceID());
		NetworkConnection.sendSocketEvent(se);

		SocketEvent res = NetworkConnection.rcvSocketEvent();
		String eventName = res.getName();
		
		final String message;
		
		if (eventName.equals("removeConferenceSucceeded")) {
			feedPanelController.reqConferenceFeed();
			message = "Udało się usunąć konferencję.";
		} else {
			System.out.println("MainController.reqRemoveConference(): eventName=" + eventName);
			message = "Nie udało się usunąć konferencji.";
		}

		Platform.runLater(new Runnable() {
			@Override public void run() {
				openDialogBox(applicationWindow, message);
			}
		});
	}
	
	
	public static User getCurrentUser() {
		return currentUser;
	}

	
	public static void setCurrentUser(User givenUser) {
		currentUser = givenUser;
	}
}
