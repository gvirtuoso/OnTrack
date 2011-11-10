package br.com.oncast.ontrack.server.services.exportImport.xml.abstractions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import br.com.oncast.ontrack.server.business.UserAction;
import br.com.oncast.ontrack.server.model.Password;
import br.com.oncast.ontrack.server.services.ServerServiceProvider;
import br.com.oncast.ontrack.server.services.persistence.PersistenceService;
import br.com.oncast.ontrack.server.services.persistence.exceptions.PersistenceException;
import br.com.oncast.ontrack.shared.model.actions.ModelAction;
import br.com.oncast.ontrack.shared.model.user.User;

public class XMLImporter {

	private OntrackXML ontrackXML;
	private final PersistenceService persistanceService;

	public XMLImporter() {
		this.persistanceService = ServerServiceProvider.getInstance().getPersistenceService();
	}

	public XMLImporter loadXML(final File file) {
		final Serializer serializer = new Persister();

		try {
			ontrackXML = serializer.read(OntrackXML.class, file);
		}
		catch (final Exception e) {
			throw new RuntimeException("Unable to deserialize xml file.", e);
		}
		return this;
	}

	// TODO Verify error treatment
	public void persistObjects() throws PersistenceException {
		if (ontrackXML == null) throw new RuntimeException("You must use loadXML method to load xml before use this method.");

		persistActions(ontrackXML.getUserActions());
		persistUser(ontrackXML.getUsers());
		persistPasswords(ontrackXML.getPasswords());
	}

	private void persistActions(final List<UserAction> userActions) throws PersistenceException {
		for (final UserAction userAction : userActions) {
			final ArrayList<ModelAction> actions = new ArrayList<ModelAction>();
			actions.add(userAction.getModelAction());
			persistanceService.persistActions(actions, userAction.getTimestamp());
		}
	}

	private void persistUser(final List<User> userList) throws PersistenceException {
		for (final User user : userList) {
			persistanceService.persistOrUpdateUser(user);
		}
	}

	private void persistPasswords(final List<Password> passwordList) throws PersistenceException {
		for (final Password pass : passwordList) {
			persistanceService.persistOrUpdatePassword(pass);
		}
	}
}