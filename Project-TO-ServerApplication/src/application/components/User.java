package application.components;

import java.io.Serializable;

public class User implements Serializable {
	
	private static final long serialVersionUID = -7433946303557607605L;
	
	Integer id;
	String login, name, surname, password, email, organization;

	
	public Integer getId() { return id; }
	public String getName() { return name; }
	public String getLogin() { return login; }
	public String getSurname() { return surname; }
	public String getEmail() { return email; }
	public String getPassword() { return password; }
	public String getOrganization() { return organization; }
	
	
	public User(Integer id, String login, String password, String name, String surname, String organization) {
		this.id = id;
		this.login = login;
		this.password = password;
		this.name = name;
		this.surname = surname;
		this.organization = organization;
	}

	public User(String login, String password) {
		this(null, login, password, null, null, null);
	}

	public User(String login, String password, String name, String surname) {
		this(null, login, password, name, surname, null);
	}

	
	@Override public String toString() {
		String ret = name + " " + surname + "(" + login + ")";
		if(organization != null && !organization.isEmpty()) ret += ", " + organization;
		
		return ret;
	}
}
