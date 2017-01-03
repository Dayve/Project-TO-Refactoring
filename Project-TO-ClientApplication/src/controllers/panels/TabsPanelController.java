package controllers.panels;

import application.components.Conference;
import application.components.User;
import application.enums.MultipurposeButtonMode;
import controllers.MainController;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;


public class TabsPanelController {
	
	private MainController main;
	
	@FXML private TabPane conferenceInfoTabPane;
	private HashMap<Integer, Tab> openedTabsConferencesIds = new HashMap<Integer, Tab>();
	
	
	public void init(MainController mainController) throws NullPointerException {
		if(mainController == null) throw new NullPointerException("TabsPanelController: MainController instance is null");
		else main = mainController;
		
		conferenceInfoTabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
			@Override public void changed(ObservableValue<? extends Tab> ov, Tab from, Tab to) {
				if (to != null) {
					selectedConferenceID = Integer.parseInt(to.getId());
					checkUsersParticipation();
				} else {
					selectedConferenceID = null;
				}
			}
		});
		
		Platform.runLater(new Runnable() {
			@Override public void run() {
				setupTabResizeEvent(main.applicationWindow);
			}
		});
	}
	
	private static Integer selectedConferenceID;
	
	
	public static Integer getSelectedConferenceID() {
		return selectedConferenceID;
	}
	
	
	public static void setSelectedConferenceID(Integer newID) {
		selectedConferenceID = newID;
	}
	
	
	public TabPane getConferenceInfoTabPane() {
		return conferenceInfoTabPane;
	}
	
	
	private void setupTabResizeEvent(Parent applicationWindow) {
		Stage mainStage = (Stage) applicationWindow.getScene().getWindow();
		
		mainStage.heightProperty().addListener(new ChangeListener<Number>() {
			@Override public void changed(ObservableValue<? extends Number> arg0, Number prevSize, Number newSize) {
				//System.out.println("prevSize: " + prevSize + " newSize: " + newSize);
				resizeConferenceTabs(conferenceInfoTabPane, newSize.intValue() / 2);
			}
		});
	}
	
	
	public void resizeConferenceTabs(TabPane tp, Integer size) {
		for (Tab t : tp.getTabs()) {
			VBox vb = (VBox) t.getContent();
			ScrollPane confInfo = (ScrollPane) vb.getChildren().get(0);
			confInfo.setPrefHeight(size);
		}
	}
	
	
	@FXML public void initialize() {
		System.out.println(" -> initialize() - TabsPanelController");
	}
	
	
	public void checkUsersParticipation() {
		if (selectedConferenceID == null) System.out.println("main.getSelectedConferenceID() is null");
		else System.out.println("main.getSelectedConferenceID():" + selectedConferenceID);
		
		// Look for conference which id was clicked:
		if (selectedConferenceID != null) {
			try {
				Conference selectedConf = main.getFeedPanelController().getConferenceByID(selectedConferenceID);
				
				ArrayList<User> selectedConfParticipants = selectedConf.getFullParticipantsList();
				ArrayList<User> selectedConfOrganizers = selectedConf.getOrganizers();
				
				boolean currentUserTakesPart = false;
				boolean currentUserIsOrganizer = false;
				
				// Check if current user takes part in a selected conference:
				for (User u : selectedConfParticipants) {
					if (u.getId() == MainController.getCurrentUser().getId()) {
						currentUserTakesPart = true;
						break;
					}
				}
				
				// Check if current user is the organizer of a selected conference:
				for (User u : selectedConfOrganizers) {
					if (u.getId() == MainController.getCurrentUser().getId()) {
						currentUserIsOrganizer = true;
						break;
					}
				}

				if (currentUserIsOrganizer) {
					main.getManagementPanelController().setConferenceDeletePossible(true);
					main.getManagementPanelController().setMultipurposeButtonMode(MultipurposeButtonMode.MANAGE);
				}
				else {
					main.getManagementPanelController().setConferenceDeletePossible(false);
					
					if (currentUserTakesPart) {
						main.getManagementPanelController().setMultipurposeButtonMode(MultipurposeButtonMode.RENOUNCE);
					} else {
						main.getManagementPanelController().setMultipurposeButtonMode(MultipurposeButtonMode.JOIN_IN);
					}
				}
			} catch (NoSuchElementException e) {
				System.out.println("NoSuchElementException was thrown (TabsPanelController.checkUsersParticipation()");
				selectedConferenceID = null;
			}
		}
	}

	
	public void openConferenceTab(ArrayList<Conference> confPool) {
		if (!openedTabsConferencesIds.containsKey(selectedConferenceID)) {
			for (Conference c : confPool) {
				if (c.getId() == selectedConferenceID) {
					Tab tab = new Tab();
					tab.setOnClosed(new EventHandler<Event>() {
						@Override public void handle(Event event) {
							Integer id = Integer.parseInt(tab.getId());
							openedTabsConferencesIds.remove(id);
						}
					});
					tab.setText(c.getName());
					tab.setId(selectedConferenceID.toString());
					VBox vbox = new VBox();

					ScrollPane scPane = createConfDescriptionScrollPane(c, main.applicationWindow.getScene().getWindow().getHeight()/2);

					// VBOx is redundant only theoretically, the full hierarchy is:
					// Tab[ VBox[ ScrollPane[ TextFlow[ Text, Text, Text, ... ] ] ] ]
					vbox.getChildren().add(scPane);

					tab.setContent(vbox);
					conferenceInfoTabPane.getTabs().add(tab);
					openedTabsConferencesIds.put(selectedConferenceID, tab);
					conferenceInfoTabPane.getSelectionModel().select(tab);
					break;
				}
			}
		} else {
			conferenceInfoTabPane.getSelectionModel().select(openedTabsConferencesIds.get(selectedConferenceID));
		}
	}
	
	
	public void refreshConferenceTabs(ArrayList<Conference> confPool) {
		try {
			for (Iterator<Tab> iterator = conferenceInfoTabPane.getTabs().iterator(); iterator.hasNext();) {
				Tab t = iterator.next();
				try {
					Conference c = confPool.stream().filter(conf -> conf.getId() == Integer.parseInt(t.getId())).findFirst().get();
					VBox vbox = new VBox();
					ScrollPane scPane = createConfDescriptionScrollPane(c, main.applicationWindow.getScene().getWindow().getHeight()/2);
					Platform.runLater(new Runnable() {
						@Override public void run() {
							vbox.getChildren().add(scPane);
							t.setContent(vbox);
						}
					});
				} catch (NoSuchElementException e) {
					// if there's no such conference found remove its tab
					openedTabsConferencesIds.remove(Integer.parseInt(t.getId()));
					iterator.remove();
				}
			}
		} catch (ConcurrentModificationException e) {
			// happens when there is only one tab opened
			// and organizer removes their conference
			// so the tab closes and leaves nothing opened
			selectedConferenceID = null;
		}
	}
	
	
	private ScrollPane createConfDescriptionScrollPane(Conference c, double prefTabHeight) {
		// TextFlow is built from many Text objects (which can have different styles)
		TextFlow flow = new TextFlow();
		flow.setPrefHeight(prefTabHeight);

		ArrayList<Text> confDescriptionSections = new ArrayList<Text>(); // e.g. "Tytuł", "Organizatorzy"

		// Styles:
		String sectionNameStyle = new String("-fx-font-weight:bold;"), // For "Tytuł", "Organizatorzy" and the rest
				
				sectionContentStyle = new String(); // For content (text of description etc.)

		String[] sectionNames = new String[] { "Temat:\n", "\n\nOrganizatorzy:\n", "\nCzas rozpoczęcia:\n",
				"\n\nCzas zakończenia:\n", "\n\nMiejsce:\n", "\n\nPlan:\n", "\n\nOpis:\n",
				"\n\nUczestnicy: (wg. roli)\n" };

		String[] sectionContents = new String[] { c.getSubject(), Conference.userListToStr(c.getOrganizers()),
				c.getStartTime().toString().replace("T", ", godz. "),
				c.getEndTime().toString().replace("T", ", godz. "), c.getPlace(), c.getAgenda(), c.getDescription(),
				c.getAllParticipantsListStr() };

		for (int i = 0; i < sectionContents.length; ++i) {
			// Label/section name:
			Text currentSectionTitle = new Text(sectionNames[i]);
			currentSectionTitle.setStyle(sectionNameStyle);
			confDescriptionSections.add(currentSectionTitle);

			// Content:
			Text currentSectionContent = new Text(sectionContents[i]);
			currentSectionContent.setStyle(sectionContentStyle);
			confDescriptionSections.add(currentSectionContent);
		}

		ScrollPane scPane = new ScrollPane(flow);
		scPane.setFitToWidth(true);

		flow.getChildren().addAll(confDescriptionSections);
		flow.setStyle("-fx-padding: 10 10 10 10;");
		
		return scPane;
	}
}
