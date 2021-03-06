package br.com.oncast.ontrack.server.services.exportImport.xml;

import br.com.oncast.ontrack.server.business.BusinessLogic;
import br.com.oncast.ontrack.server.services.exportImport.xml.abstractions.OntrackXML;
import br.com.oncast.ontrack.server.services.exportImport.xml.abstractions.ProjectAuthorizationXMLNode;
import br.com.oncast.ontrack.server.services.exportImport.xml.abstractions.ProjectXMLNode;
import br.com.oncast.ontrack.server.services.exportImport.xml.abstractions.UserXMLNode;
import br.com.oncast.ontrack.server.services.exportImport.xml.exceptions.UnableToImportXMLException;
import br.com.oncast.ontrack.server.services.exportImport.xml.transform.CustomMatcher;
import br.com.oncast.ontrack.server.services.metrics.ServerAnalytics;
import br.com.oncast.ontrack.server.services.persistence.PersistenceService;
import br.com.oncast.ontrack.server.services.persistence.exceptions.NoResultFoundException;
import br.com.oncast.ontrack.server.services.persistence.exceptions.PersistenceException;
import br.com.oncast.ontrack.shared.model.action.UserAction;
import br.com.oncast.ontrack.shared.model.project.ProjectRepresentation;
import br.com.oncast.ontrack.shared.model.user.User;
import br.com.oncast.ontrack.shared.services.notification.Notification;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

public class XMLImporter {

	private final PersistenceService persistenceService;
	private OntrackXML ontrackXML;
	private static final Logger LOGGER = Logger.getLogger(XMLImporter.class);
	private final BusinessLogic businessLogic;
	private boolean persisted;
	private final ServerAnalytics serverAnalytics;

	public XMLImporter(final PersistenceService persistenceService, final BusinessLogic businessLogic, final ServerAnalytics serverAnalytics) {
		this.persistenceService = persistenceService;
		this.businessLogic = businessLogic;
		this.serverAnalytics = serverAnalytics;
		this.persisted = false;
	}

	public XMLImporter loadXML(final File file) {
		final long initialTime = getCurrentTime();
		final Serializer serializer = new Persister(new CustomMatcher());

		try {
			ontrackXML = serializer.read(OntrackXML.class, file);
			persisted = false;
			LOGGER.debug("Finished XML Serialization in " + getTimeSpent(initialTime) + " ms");
			serverAnalytics.onImportXmlLoad(file.length(), getTimeSpent(initialTime));
		} catch (final Exception e) {
			throw new UnableToImportXMLException("Unable to deserialize xml file.", e);
		}
		return this;
	}

	public XMLImporter persistObjects() {
		if (ontrackXML == null) throw new RuntimeException("You must use loadXML method to load xml before use this method.");

		try {
			persistUsers(ontrackXML.getUsers());
			persistProjects(ontrackXML.getProjects());
			persistAuthorizations(ontrackXML.getProjectAuthorizations());
			persistNotifications(ontrackXML.getNotifications());

			this.persisted = true;
			return this;
		} catch (final Exception e) {
			throw new UnableToImportXMLException("The xml import was not concluded. Some operations may be changed the database, but was not rolledback. ", e);
		}
	}

	private void persistNotifications(final List<Notification> notifications) throws PersistenceException {
		final long initialTime = getCurrentTime();
		for (final Notification notification : notifications) {
			persistenceService.persistOrUpdateNotification(notification);
		}
		serverAnalytics.onNotificationsImported(notifications.size(), getTimeSpent(initialTime));
		LOGGER.debug("Persisted " + notifications.size() + " notifications in " + getTimeSpent(initialTime) + " ms.");
	}

	private void persistUsers(final List<UserXMLNode> userNodes) throws PersistenceException {
		final long initialTime = getCurrentTime();
		int newUsersCount = 0;
		for (final UserXMLNode userNode : userNodes) {
			final User user = userNode.getUser();

			persistenceService.persistOrUpdateUser(user);
			if (userNode.hasPassword()) persistenceService.persistOrUpdatePassword(userNode.getPassword());
			newUsersCount++;
		}
		LOGGER.debug("Persisted " + newUsersCount + " new users in " + getTimeSpent(initialTime) + " ms.");
		serverAnalytics.onUsersImported(newUsersCount, getTimeSpent(initialTime));
	}

	private void persistProjects(final List<ProjectXMLNode> projectNodes) throws PersistenceException {
		final long startTime = getCurrentTime();
		for (final ProjectXMLNode projectNode : projectNodes) {
			final long initialTime = getCurrentTime();
			final ProjectRepresentation representation = projectNode.getProjectRepresentation();
			persistenceService.persistOrUpdateProjectRepresentation(representation);
			final List<UserAction> actions = projectNode.getActions();
			for (final UserAction userAction : actions) {
				userAction.setProjectId(representation.getId());
			}
			persistActions(actions);
			LOGGER.info("Persisted project " + representation + " and it's " + actions.size() + " actions in " + getTimeSpent(initialTime) + " ms.");
			serverAnalytics.onProjectPersisted(representation, actions.size(), getTimeSpent(initialTime));
		}
		LOGGER.debug("Persisted " + projectNodes.size() + " projects in " + getTimeSpent(startTime) + " ms");
	}

	private void persistActions(final List<UserAction> userActions) throws PersistenceException {
		persistenceService.persistActions(userActions);
	}

	private void persistAuthorizations(final List<ProjectAuthorizationXMLNode> projectAuthorizationNodes) throws PersistenceException, NoResultFoundException {
		final long initialTime = getCurrentTime();
		for (final ProjectAuthorizationXMLNode authNode : projectAuthorizationNodes) {
			persistenceService.authorize(authNode.getUserId(), authNode.getProjectId());
		}
		LOGGER.debug("Persisted " + projectAuthorizationNodes.size() + " ProjectAuthorizations in " + getTimeSpent(initialTime) + " ms.");
		serverAnalytics.onProjectAuthorizationsImported(projectAuthorizationNodes.size(), getTimeSpent(initialTime));

	}

	public void loadProjects() {
		if (ontrackXML == null) throw new RuntimeException("You must use loadXML method to load xml before this method.");
		if (!persisted) throw new RuntimeException("You must use persistObjects method to persist actions before this method.");

		for (final ProjectXMLNode node : ontrackXML.getProjects()) {
			final long initialTime = getCurrentTime();
			final ProjectRepresentation representation = node.getProjectRepresentation();
			try {
				businessLogic.loadProjectForMigration(representation.getId());
				LOGGER.info("Loaded project " + representation + " in " + getTimeSpent(initialTime) + " ms.");
				serverAnalytics.onProjectLoadedForMigration(representation, node.getActions().size(), getTimeSpent(initialTime));
			} catch (final Exception e) {
				final String message = "Unable to load project '" + representation + "' after import.";
				LOGGER.error(message, e);
				serverAnalytics.onProjectLoadForMigrationError(representation, e);
				throw new UnableToImportXMLException("The xml import was not concluded. Some operations may be changed the database, but was not rolled back. Reason: " + message, e);
			}
		}
	}

	private long getCurrentTime() {
		return new Date().getTime();
	}

	private long getTimeSpent(final long initialTime) {
		return getCurrentTime() - initialTime;
	}

}