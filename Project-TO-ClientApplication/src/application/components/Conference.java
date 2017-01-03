package application.components;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;


public class Conference implements Serializable {

	private static final long serialVersionUID = -6259050915073534863L;
	
	public static Comparator<Conference> confDateComparator = new Comparator<Conference>() {
		public int compare(Conference c1, Conference c2) {
			return c1.getStartTime().compareTo(c2.getStartTime());
		}
	};
	
	private Integer id;
	private String name, subject, place, description, agenda;
	private LocalDateTime startTime, endTime;
	private ArrayList<User> organizers = new ArrayList<User>(),
			prelectors = new ArrayList<User>(),
			participants = new ArrayList<User>(),
			sponsors = new ArrayList<User>(),
			pending = new ArrayList<User>();

	
	public static long getSerialversionuid() { return serialVersionUID; }
	
	public Integer getId() { return id; }

	public String getName() { return name; }
	public String getSubject() { return subject; }
	public String getPlace() { return place; }
	public String getDescription() { return description; }
	public String getAgenda() { return agenda; }
	
	public LocalDateTime getStartTime() { return startTime; }
	public LocalDateTime getEndTime() { return endTime; }
	
	public LocalDate getStartTimeAsLocalDate() {
		return startTime.toLocalDate();
	}

	public ArrayList<User> getOrganizers() { return organizers; }
	public ArrayList<User> getPrelectors() { return prelectors; }
	public ArrayList<User> getParticipants() { return participants; }
	public ArrayList<User> getSponsors() { return sponsors; }
	public ArrayList<User> getPending() { return pending; }
	
	public ArrayList<User> getFullParticipantsList() {
		ArrayList<User> ret = new ArrayList<User>();
		ret.addAll(prelectors);
		ret.addAll(participants);
		ret.addAll(sponsors);
		ret.addAll(pending);
		return ret;
	}

	
	public Conference(String name, String subject, LocalDateTime startTime, LocalDateTime endTime, String place,
			String description, String agenda, User organizer) {
		this.name = name;
		this.subject = subject;
		this.startTime = startTime;
		this.endTime = endTime;
		this.place = place;
		this.description = description;
		this.agenda = agenda;
		organizers.add(organizer);
	}

	public Conference(String name, String subject, LocalDateTime startTime, LocalDateTime endTime, String place,
			String description, String agenda, ArrayList<User> organizers) {
		this.name = name;
		this.subject = subject;
		this.startTime = startTime;
		this.endTime = endTime;
		this.place = place;
		this.description = description;
		this.agenda = agenda;
		this.organizers = organizers;
	}

	public Conference(Integer id, String name, String subject, LocalDateTime startTime, LocalDateTime endTime, String place,
			String description, String agenda, ArrayList<User> organizers) {
		this(name, subject, startTime, endTime, place, description, agenda, organizers);
		this.id = id;
	}
	
	public Conference(Integer id, String name, String subject, LocalDateTime startTime, LocalDateTime endTime, String place,
			String description, String agenda, User organizer) {
		this(name, subject, startTime, endTime, place, description, agenda, organizer);
		this.id = id;
	}
	
	public Conference(String name, String subject, LocalDateTime startTime, LocalDateTime endTime, String place,
			String description, String agenda, ArrayList<User> organizers, ArrayList<User> sponsors,
			ArrayList<User> prelectors, ArrayList<User> participants, ArrayList<User> pending) {
		this.name = name;
		this.subject = subject;
		this.startTime = startTime;
		this.endTime = endTime;
		this.place = place;
		this.description = description;
		this.agenda = agenda;
		this.organizers = organizers;
		this.sponsors = sponsors;
		this.participants = participants;
		this.pending = pending;
		this.prelectors = prelectors;
	}
	
	public Conference(int id, String name, String subject, LocalDateTime startTime, LocalDateTime endTime, String place,
			String description, String agenda, ArrayList<User> organizers, ArrayList<User> sponsors,
			ArrayList<User> prelectors, ArrayList<User> participants, ArrayList<User> pending) {
		this(name, subject, startTime, endTime, place, description, agenda, organizers, sponsors, prelectors,
				participants, pending);
		this.id = id;
	}

	
	public static String userListToStr(ArrayList<User> uL) {
		String str = new String();
		Iterator<User> it = uL.iterator();
		
		while (it.hasNext()) {
			User user = it.next();
			
			str +=	user.getName() + " " +
					user.getSurname() + 
					((user.getOrganization().isEmpty() || user.getOrganization() == null) ? "" : "("+user.getOrganization()+")") + "\n";
		}
		
		return str;
	}

	
	public String getAllParticipantsListStr() {
		String sponsorsStr = userListToStr(sponsors);
		String prelectorsStr = userListToStr(prelectors);
		String participantsStr = userListToStr(participants);
		String pendingStr = userListToStr(pending);
		String str = "";

		if (prelectorsStr.length() > 0) {
			str += "Prelegenci:\n";
			str += prelectorsStr;
		}
		if (sponsorsStr.length() > 0) {
			str += "\nSponsorzy:\n";
			str += sponsorsStr;
		}
		if (participantsStr.length() > 0) {
			str += "\nUczestnicy:\n";
			str += participantsStr;
		}
		if (pendingStr.length() > 0) {
			str += "\nOczekujący na potwierdzenie:\n";
			str += pendingStr;
		}

		return str;
	}

	
	@Override public String toString() {
		String ret = 
				"Temat:\n" + subject + 
				"\n\nOrganizatorzy:\n" + userListToStr(organizers) + 
				"\nCzas rozpoczęcia:" + startTime.toString().replace("T", ", godz. ") + 
				"\n\nCzas zakończenia: "+ endTime.toString().replace("T", ", godz. ") + 
				"\n\nMiejsce:\n" + place + 
				"\n\nPlan:\n" + agenda;
		
		if(this.description != null && !this.description.isEmpty()) ret += "\n\nOpis: " + description;
		ret += "\n\nLista uczestników:\n" + getAllParticipantsListStr();
		
		return ret;
	}
}
