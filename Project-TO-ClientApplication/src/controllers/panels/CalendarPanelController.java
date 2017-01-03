package controllers.panels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import application.components.Conference;
import application.components.User;
import application.components.Week;
import application.enums.ConferencePeriodFilter;
import application.enums.ParticipantsRole;
import controllers.ConferenceCreatorController;
import controllers.MainController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;


public class CalendarPanelController {

	private MainController main;
	private LocalDate selectedDate;
	
	@FXML private Button prevMonthButton;
	@FXML private Button nextMonthButton;
	@FXML private ComboBox<String> monthSelectionComboBox;
	@FXML private ComboBox<String> yearSelectionComboBox;
	
	@FXML private TableView<Week> calendarTable;
	@FXML private ListView<Label> selectedDayConferencesList;
	
	
	public void init(MainController mainController) throws NullPointerException {
		if(mainController == null) throw new NullPointerException("CalendarPanelController: MainController instance is null");
		else main = mainController;
	}
	
	
	@FXML public void initialize() {
		System.out.println(" -> initialize() - CalendarPanelController");
		
		selectedDate = LocalDate.now();
		setupMonthsYearsCBs();
		calendarTable.getSelectionModel().setCellSelectionEnabled(true);
	}
	
	
	public LocalDate getSelectedDate() {
		return selectedDate;
	}
	
	
	public void refreshCalendarTable(ArrayList<Conference> conferencesFeed) {
		calendarTable.getItems().clear();
		calendarTable.getColumns().clear();
		fillCalendarTable(conferencesFeed);
	}
	
	
	private void setupMonthsYearsCBs() {
		ObservableList<String> monthsFeedOptions = FXCollections.observableArrayList("Styczeń", "Luty", "Marzec",
				"Kwiecień", "Maj", "Czerwiec", "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień");

		monthSelectionComboBox.getItems().addAll(monthsFeedOptions);
		monthSelectionComboBox.setValue("miesiąc");

		ObservableList<String> yearsFeedOptions = FXCollections.observableArrayList("2016", "2017", "2018", "2019", "2020");

		yearSelectionComboBox.getItems().addAll(yearsFeedOptions);
		yearSelectionComboBox.setValue("rok");
		
		// Set initial ComboBox values
		String currentDateInPolish = localDateToPolishDateString(selectedDate);
		monthSelectionComboBox.setValue(currentDateInPolish.substring(0, currentDateInPolish.indexOf(" ")));
		yearSelectionComboBox.setValue(currentDateInPolish.substring(currentDateInPolish.indexOf(" ")+1));
	}
	
	
	public void fillCalendarTable(ArrayList<Conference> conferencesFeed) {
		// ColumnTitle are used only while displaying the content,
		// PropertyValue however must be the same as variable names in the Week class:
		String[] daysOfTheWeekColumnTitles = { "Pn", "Wt", "Śr", "Czw", "Pt", "Sb", "Nd" };
		String[] daysOfTheWeekPropertyValues = { "pn", "wt", "sr", "cz", "pt", "sb", "nd" };

		// List of columns:
		List<TableColumn<Week, String>> dayOfTheWeekColumns = new ArrayList<TableColumn<Week, String>>();

		// Setting the attributes for every column:
		for (int i = 0; i < daysOfTheWeekColumnTitles.length; ++i) {
			TableColumn<Week, String> col = new TableColumn<>(daysOfTheWeekColumnTitles[i]);

			col.setMinWidth(10);
			col.setPrefWidth(55);
			col.setMaxWidth(200);
			col.setResizable(false);
			col.setSortable(false);

			String	defaultCellSettings = "-fx-alignment: CENTER; -fx-font-size: 14pt;",
					participantMarker = "-fx-background-color: blue;",
					organizerMarker = "-fx-background-color: yellow;",
					prelectorMarker = "-fx-background-color: gray;",
					sponsorMarker = "-fx-background-color: pink;",
					pendingMarker = "-fx-background-color: red;",
					noneYetMarker = "-fx-background-color: green;";

			// Column-wise cell factory:
			col.setCellFactory(tableColumn -> {
				TableCell<Week, String> cell = new TableCell<Week, String>() {
					@Override protected void updateItem(String item, boolean emptyCell) {
						super.updateItem(item, emptyCell);
						this.setText(emptyCell ? null : item);

						setStyle(defaultCellSettings);

						if (item != null && !emptyCell && !item.isEmpty()) {

							ArrayList<Conference> thisDayConferences = getConferencesAtDate(selectedDate.withDayOfMonth(Integer.parseInt(item)), conferencesFeed);
							
							if (!thisDayConferences.isEmpty()) {
								for (Conference c : thisDayConferences) {
									// If you have some role in more than one conference in that day, style
									// will be overwritten (in thisDayConferences array order)

									switch (usersRoleOnConference(MainController.getCurrentUser(), c)) {
										// If you have two roles, style will be overwritten in this order:
										case PARTICIPANT:
											setStyle(defaultCellSettings + " " + participantMarker);
											break;

										case ORGANIZER:
											setStyle(defaultCellSettings + " " + organizerMarker);
											break;

										case PRELECTOR:
											setStyle(defaultCellSettings + " " + prelectorMarker);
											break;

										case SPONSOR:
											setStyle(defaultCellSettings + " " + sponsorMarker);
											break;

										case PENDING:
											setStyle(defaultCellSettings + " " + pendingMarker);
											break;

										case NONE:
											setStyle(defaultCellSettings + " " + noneYetMarker);
											break;
									}
								}
							}
						}
					}
				};

				cell.setPrefHeight(col.getWidth());

				// Handle action: left mouse button pressed:
				cell.setOnMouseClicked(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent t) {
						if (!cell.isEmpty()) {
							String cellsContent = cell.getItem();

							if (!cellsContent.isEmpty()) {
								selectedDate = selectedDate.withDayOfMonth(Integer.parseInt(cellsContent));

								if (isAnyConferenceAtDate(selectedDate, conferencesFeed)) {
									// Perform an action after a day with assigned conference was clicked:
									fillListViewWithSelectedDaysConferences(selectedDate, conferencesFeed, false);
								} else {
									selectedDayConferencesList.getItems().clear();
								}
								ConferenceCreatorController.setChosenDay(selectedDate);
							}
						}
					}
				});

				return cell;
			});

			col.setCellValueFactory(new PropertyValueFactory<>(daysOfTheWeekPropertyValues[i]));
			dayOfTheWeekColumns.add(col);
		}

		// Filling the actual table:
		calendarTable.setItems(createThisMonthsWeeksRows(selectedDate));
		calendarTable.getColumns().addAll(dayOfTheWeekColumns);
	}
	
	
	public void fillListViewWithSelectedDaysConferences(LocalDate selectedDate, ArrayList<Conference> feed, boolean showDate) {
		ArrayList<Conference> selectedDayConferences = new ArrayList<Conference>();
		
		selectedDayConferencesList.getItems().clear();
		
		for (Conference c : feed) {
			if (c.getStartTime().toLocalDate().equals(selectedDate)) {
				selectedDayConferences.add(c);
			}
		}
		
		if (selectedDayConferences != null) {
			main.getFeedPanelController().fillListWithLabels(selectedDayConferencesList, selectedDayConferences, ConferencePeriodFilter.ALL, 35, showDate);
		}
	}
	
	
	private static ArrayList<Conference> getConferencesAtDate(LocalDate givenDate, ArrayList<Conference> conferencesFeed) {
		ArrayList<Conference> results = new ArrayList<Conference>();

		for (Conference d : conferencesFeed) {
			if (d.getStartTime().toLocalDate().equals(givenDate)) {
				results.add(d);
			}
		}
		return results;
	}
	
	
	@FXML public void changeMonthToChosen() {
		String polishMonth = monthSelectionComboBox.getValue();
		String engShortMonth = PolishDateStringToEngDateString(polishMonth);
		
		try {
			Date date = new SimpleDateFormat("MMM", Locale.ENGLISH).parse(engShortMonth);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int month = cal.get(Calendar.MONTH) + 1; // months begin with 0
			selectedDate = selectedDate.withMonth(month);
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		refreshCalendarTable(main.getFeedPanelController().getFeed());
	}
	

	@FXML public void changeYearToChosen() {
		int year = Integer.parseInt(yearSelectionComboBox.getValue());
		selectedDate = selectedDate.withYear(year);
		refreshCalendarTable(main.getFeedPanelController().getFeed());
	}
	
	
	// Returns String containing: "<polish name of a month> <year>" for a given LocalDate:
	private static String localDateToPolishDateString(LocalDate givenDate) {
		String result = new String();

		switch (givenDate.getMonth()) {
			case JANUARY:
				result += "Styczeń ";
				break;
			case FEBRUARY:
				result += "Luty ";
				break;
			case MARCH:
				result += "Marzec ";
				break;
			case APRIL:
				result += "Kwiecień ";
				break;
			case MAY:
				result += "Maj ";
				break;
			case JUNE:
				result += "Czerwiec ";
				break;
			case JULY:
				result += "Lipiec ";
				break;
			case AUGUST:
				result += "Sierpień ";
				break;
			case SEPTEMBER:
				result += "Wrzesień ";
				break;
			case OCTOBER:
				result += "Październik ";
				break;
			case NOVEMBER:
				result += "Listopad ";
				break;
			case DECEMBER:
				result += "Grudzień ";
				break;
			default:
				result += "[Invalid month] ";
				break;
		}

		result += givenDate.getYear();

		return result;
	}
	
	
	public String PolishDateStringToEngDateString(String givenDate) {
		String result = new String();

		switch (givenDate) {
			case "Styczeń":
				result += "Jan";
				break;
			case "Luty":
				result += "Feb";
				break;
			case "Marzec":
				result += "Mar";
				break;
			case "Kwiecień":
				result += "Apr";
				break;
			case "Maj":
				result += "May";
				break;
			case "Czerwiec":
				result += "Jun";
				break;
			case "Lipiec":
				result += "Jul";
				break;
			case "Sierpień":
				result += "Aug";
				break;
			case "Wrzesień":
				result += "Sep";
				break;
			case "Październik":
				result += "Oct";
				break;
			case "Listopad":
				result += "Nov";
				break;
			case "Grudzień":
				result += "Dec";
				break;
		}
		return result;
	}
	
	
	// Returns true if there is a conference (one or more) assigned to a given date:
	private static boolean isAnyConferenceAtDate(LocalDate givenDate, ArrayList<Conference> conferencesFeed) {
		for (Conference d : conferencesFeed) {
			if (d.getStartTime().toLocalDate().equals(givenDate)) {
				return true;
			}
		}
		return false;
	}
	
	
	public static ParticipantsRole usersRoleOnConference(User user, Conference conference) {
		if(user == null) System.out.println("CalendarPanelController.usersRoleOnConference: user is null.");
		if(conference == null) System.out.println("CalendarPanelController.usersRoleOnConference: conference is null.");
		
		for(User u : conference.getParticipants()) {
			if(u.getId().equals(user.getId())) return ParticipantsRole.PARTICIPANT;
		}
		for(User u : conference.getOrganizers()) {
			if(u.getId().equals(user.getId())) return ParticipantsRole.ORGANIZER;
		}
		for(User u : conference.getPrelectors()) {
			if(u.getId().equals(user.getId())) return ParticipantsRole.PRELECTOR;
		}
		for(User u : conference.getSponsors()) {
			if(u.getId().equals(user.getId())) return ParticipantsRole.SPONSOR;
		}
		for(User u : conference.getPending()) {
			if(u.getId().equals(user.getId())) return ParticipantsRole.PENDING;
		}
		return ParticipantsRole.NONE;
	}
	
	
	// Generates day numbers (calendarTable rows - weeks) for the year and month
	// currently stored in calendarsDate:
	private static ObservableList<Week> createThisMonthsWeeksRows(LocalDate calendarsDate) {
		ObservableList<Week> weeksInAMonth = FXCollections.observableArrayList();

		int nthDayOfWeekMonthStartsAt = calendarsDate.withDayOfMonth(1).getDayOfWeek().getValue();
		List<String> currentInitializers = new ArrayList<String>();
		Integer dayOfTheMonth = 1;
		boolean firstWeekIteration = true;

		while (dayOfTheMonth <= calendarsDate.lengthOfMonth()) {
			currentInitializers.clear();

			for (int d = 1; d <= 7; ++d) {
				if ((d < nthDayOfWeekMonthStartsAt && firstWeekIteration)
						|| dayOfTheMonth > calendarsDate.lengthOfMonth()) {
					// Empty labels in days of the week before current month's
					// first day and after the last one:
					currentInitializers.add("");
				} else {
					currentInitializers.add(dayOfTheMonth.toString());
					dayOfTheMonth++;
				}
			}

			weeksInAMonth.add(new Week(currentInitializers));
			if (firstWeekIteration)
				firstWeekIteration = false;
		}

		return weeksInAMonth;
	}
	
	
	@FXML public void changeMonthToNext() {
		selectedDate = selectedDate.plusMonths(1);
		refreshCalendarTable(main.getFeedPanelController().getFeed());
		
		updateComboBoxesAccordingToDate(selectedDate);
	}
	

	@FXML public void changeMonthToPrevious() {
		selectedDate = selectedDate.minusMonths(1);
		refreshCalendarTable(main.getFeedPanelController().getFeed());
		
		updateComboBoxesAccordingToDate(selectedDate);
	}
	
	
	private void updateComboBoxesAccordingToDate(LocalDate givenDate) {
		// Set new ComboBox values:
		String currentDateInPolish = localDateToPolishDateString(givenDate);
		monthSelectionComboBox.setValue(currentDateInPolish.substring(0, currentDateInPolish.indexOf(" ")));
		yearSelectionComboBox.setValue(currentDateInPolish.substring(currentDateInPolish.indexOf(" ") + 1));
	}
}

