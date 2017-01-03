package controllers.panels;

import application.components.Conference;
import application.components.ControllerFunctionality;
import application.components.NetworkConnection;
import application.components.SocketEvent;
import application.enums.ConferencePeriodFilter;
import controllers.MainController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;


public class FeedPanelController implements ControllerFunctionality {
	
	private MainController main;
	
	@FXML private ComboBox<String> periodFilterComboBox;
	@FXML private ComboBox<String> numberFilterComboBox;
	@FXML private TextField searchTextField;
	@FXML private ListView<Label> feedListView;
	
	private ConferencePeriodFilter periodFilter;
	private static ArrayList<Conference> feed = new ArrayList<Conference>();
	
	
	public static ArrayList<Conference> getFeed() {
		return feed;
	}
	
	public static Conference getConferenceByID(Integer givenConferenceID) throws NoSuchElementException {
		if (givenConferenceID != null) {
			return feed.stream().filter(c -> c.getId().equals(givenConferenceID)).findFirst().get();
		} else {
			return null;
		}
	}


	public void init(MainController mainController) throws NullPointerException {
		if(mainController == null) throw new NullPointerException("FeedPanelController: MainController instance is null");
		else main = mainController;
		
		reqConferenceFeed();
	}
	
	
	@FXML public void initialize() {
		System.out.println(" -> initialize() - FeedPanelController");
		
		ObservableList<String> periodFilterOptions = FXCollections.observableArrayList("Nadchodzące konferencje", "Wszystkie konferencje", "Zakończone konferencje");
		periodFilterComboBox.getItems().addAll(periodFilterOptions);
		periodFilterComboBox.setValue("Nadchodzące konferencje");

		ObservableList<String> numberFilterOptions = FXCollections.observableArrayList("20", "50", "...");
		numberFilterComboBox.getItems().addAll(numberFilterOptions);
		numberFilterComboBox.setValue("50");

		searchTextField.textProperty().addListener(obs -> {
			refreshConferencesListView(searchTextField.getText());
		});
	}
	
	
	private void refreshConferencesListView(String searchBoxContent) {
		updateFilterValueFromGUI();

		FilteredList<Conference> searchBarFilteredData = new FilteredList<>(
				FXCollections.observableArrayList(filterConferencesByPeriod(feed, periodFilter)),
				s -> s.getName().toLowerCase().contains(searchBoxContent.toLowerCase()));

		ArrayList<Conference> searchBarFilteredData_ArrayList = new ArrayList<Conference>();
		for (Conference con : searchBarFilteredData) searchBarFilteredData_ArrayList.add(con);

		Platform.runLater(new Runnable() {
			@Override public void run() {
				fillListWithLabels(feedListView, searchBarFilteredData_ArrayList, periodFilter, 35, true);
			}
		});
	}
	
	
	public void fillListWithLabels(ListView<Label> targetListView, ArrayList<Conference> conferences, ConferencePeriodFilter cf, int charLimit, boolean showDate) {
		ArrayList<Conference> filtered = filterConferencesByPeriod(conferences, cf);
		Collections.sort(filtered, Conference.confDateComparator);
		ObservableList<Label> conferenceTitleLabels = FXCollections.observableArrayList();
		targetListView.getItems().clear();
		Label label = null;

		for (Conference c : filtered) {
			String title = c.getName();
			if (showDate) {
				title += " (" + c.getStartTimeAsLocalDate() + ")";
			}
			Integer currId = c.getId();
			label = new Label(addNLsIfTooLong(title, charLimit));
			label.setFont(Font.font("Inconsolata", 13));

			label.setId(currId.toString());
			label.setPrefWidth(targetListView.getWidth());
			label.setOnMouseClicked(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent t) {
					TabsPanelController.setSelectedConferenceID(currId);
					main.getTabsPanelController().openConferenceTab(conferences);
				}
			});
			conferenceTitleLabels.add(label);
		}
		targetListView.setItems(conferenceTitleLabels);
	}
	
	
	public static String addNLsIfTooLong(String givenString, int limit) {
		String[] separateWords = givenString.split("\\s+");
		String result = new String();
		int howMuchCharsSoFar = 0;

		for (int i = 0; i < separateWords.length; ++i) {
			howMuchCharsSoFar += separateWords[i].length() + 1; // +1 because
			// we assume that every word has a space at the end

			if (howMuchCharsSoFar > limit) {
				result += "\n";
				howMuchCharsSoFar = 0;
			}
			result += separateWords[i] + " ";
		}

		return result.substring(0, result.length() - 1);
	}
	
	
	public ArrayList<Conference> filterConferencesByPeriod(ArrayList<Conference> conferences, ConferencePeriodFilter givenFilter) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		
		LocalDateTime now = LocalDateTime.now();
		now.format(formatter);
		
		ArrayList<Conference> filtered = new ArrayList<Conference>();
		
		switch (givenFilter) {
			case PAST: {
				filtered = (ArrayList<Conference>) conferences.stream().filter(c -> c.getEndTime().isBefore(now)).collect(Collectors.toList());
				break;
			}
			case FUTURE: {
				filtered = (ArrayList<Conference>) conferences.stream().filter(c -> c.getStartTime().isAfter(now)).collect(Collectors.toList());
				break;
			}
			case ONGOING: {
				filtered = (ArrayList<Conference>) conferences.stream().filter(c -> c.getStartTime().isAfter(now) && c.getEndTime().isBefore(now)).collect(Collectors.toList());
				break;
			}
			case ALL: {
				filtered.addAll(conferences);
				break;
			}

			default:
				break;
		}
		return filtered;
	}
	
	
	@SuppressWarnings("unchecked") @FXML public void reqConferenceFeed() {
		SocketEvent e = new SocketEvent("reqConferenceFeed");
		NetworkConnection.sendSocketEvent(e);
		SocketEvent res = NetworkConnection.rcvSocketEvent();

		String eventName = res.getName();
		ArrayList<Conference> receivedFeed = null;

		if (eventName.equals("updateConferenceFeed")) {
			// get temp feed to compare it with current one
			receivedFeed = res.getObject(ArrayList.class);
			// fc.setFeed(tempFeed);

			// run in JavaFX after background thread finishes work
			// compare if feeds match, if so, don't fill vbox with new content
			if (receivedFeed != null && !receivedFeed.toString().equals(feed.toString())) {
				feed = receivedFeed;

				Platform.runLater(new Runnable() {
					@Override public void run() {
						main.getTabsPanelController().refreshConferenceTabs(feed);
						// fill FeedBox and Calendar in JavaFX UI Thread
						main.getTabsPanelController().checkUsersParticipation();
						filterFeed();
						
						main.getCalendarPanelController().refreshCalendarTable(feed);
						refreshConferencesListView(searchTextField.getText());
						
						main.getCalendarPanelController().fillListViewWithSelectedDaysConferences(main.getCalendarPanelController().getSelectedDate(), feed, false);
					}
				});
			}
		}
		else System.out.println("FeedPanelController.reqConferenceFeed(): eventName=" + eventName);
	}
	
	
	private void updateFilterValueFromGUI() {
		String chosenPeriodFilter = periodFilterComboBox.getValue();
		periodFilter = ConferencePeriodFilter.ALL;
		
		if (chosenPeriodFilter.equals("Zakończone konferencje")) {
			periodFilter = ConferencePeriodFilter.PAST;
		} else if (chosenPeriodFilter.equals("Nadchodzące konferencje")) {
			periodFilter = ConferencePeriodFilter.FUTURE;
		}
	}
	
	
	// filters feed depending on conferenceCB's value - future/all/past
	// conferences
	@FXML private void filterFeed() {
		updateFilterValueFromGUI();
		
		ArrayList<Conference> filtered = filterConferencesByPeriod(feed, periodFilter);
		
		Platform.runLater(new Runnable() {
			@Override public void run() {
				fillListWithLabels(feedListView, filtered, periodFilter, 35, true);
				refreshConferencesListView(searchTextField.getText());
			}
		});
	}
}
