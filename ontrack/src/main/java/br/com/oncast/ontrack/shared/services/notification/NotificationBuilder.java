package br.com.oncast.ontrack.shared.services.notification;

import br.com.oncast.ontrack.shared.model.project.ProjectRepresentation;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

import java.util.Date;

public class NotificationBuilder {

	private final Notification notification;

	public NotificationBuilder(final NotificationType type, final ProjectRepresentation projectRepresentation, final UUID authorId) {
		notification = new Notification();
		notification.setId(new UUID());
		notification.setTimestamp(new Date());
		notification.setType(type);
		notification.setProjectRepresentation(projectRepresentation);
		notification.setAuthorId(authorId);
		notification.setDescription("");
		notification.setReferenceDescription("");
	}

	public NotificationBuilder addReceipient(final UUID userId) {
		notification.addRecipient(new NotificationRecipient(userId));
		return this;
	}

	public NotificationBuilder setTimestamp(final Date timestamp) {
		notification.setTimestamp(timestamp);
		return this;
	}

	public NotificationBuilder setReferenceId(final UUID id) {
		notification.setReferenceId(id);
		return this;
	}

	public NotificationBuilder setDescription(final String description) {
		notification.setDescription(description);
		return this;
	}

	public NotificationBuilder setReferenceDescription(final String referenceDescription) {
		notification.setReferenceDescription(referenceDescription);
		return this;
	}

	public NotificationBuilder setProjectRepresentation(final ProjectRepresentation projectRepresentation) {
		notification.setProjectRepresentation(projectRepresentation);
		return this;
	}

	public Notification getNotification() {
		return notification;
	}
}