package br.com.oncast.ontrack.server.services.exportImport.xml.transformations;

import br.com.oncast.ontrack.shared.services.notification.NotificationType;

import org.simpleframework.xml.transform.Transform;

public class NotificationTypeTransform implements Transform<NotificationType> {

	@Override
	public NotificationType read(final String value) throws Exception {
		return NotificationType.valueOf(value);
	}

	@Override
	public String write(final NotificationType type) throws Exception {
		return type.name();
	}

}
