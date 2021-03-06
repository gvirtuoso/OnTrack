package br.com.oncast.ontrack.server.services.exportImport.xml.abstractions;

import br.com.oncast.ontrack.shared.services.notification.Notification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class OntrackXML {

	@Attribute
	private String version;

	@Attribute(required = false)
	private Date timestamp;

	@ElementList
	private List<UserXMLNode> users;

	@ElementList
	private List<ProjectXMLNode> projects;

	@ElementList
	private List<ProjectAuthorizationXMLNode> projectAuthorizations;

	@ElementList(required = false)
	private List<Notification> notifications = new ArrayList<Notification>();

	public void setUsers(final List<UserXMLNode> users) {
		this.users = users;
	}

	public List<UserXMLNode> getUsers() {
		return users;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	public List<ProjectXMLNode> getProjects() {
		return projects;
	}

	public void setProjects(final List<ProjectXMLNode> projects) {
		this.projects = projects;
	}

	public void setProjectAuthorizations(final List<ProjectAuthorizationXMLNode> projectAuthorizations) {
		this.projectAuthorizations = projectAuthorizations;
	}

	public List<ProjectAuthorizationXMLNode> getProjectAuthorizations() {
		return projectAuthorizations;
	}

	public void setNotifications(final List<Notification> notifications) {
		this.notifications = notifications;
	}

	public List<Notification> getNotifications() {
		return notifications;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(final Date timestamp) {
		this.timestamp = timestamp;
	}
}
