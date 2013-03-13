package br.com.oncast.ontrack.client.services.serverPush;

import br.com.oncast.ontrack.client.ui.places.loading.ServerPushConnectionCallback;
import br.com.oncast.ontrack.shared.services.serverPush.ServerPushEvent;

import com.google.gwt.event.shared.HandlerRegistration;

public interface ServerPushClientService {

	public <T extends ServerPushEvent> HandlerRegistration registerServerEventHandler(final Class<T> eventClass,
			final ServerPushEventHandler<T> serverPushEventHandler);

	public String getConnectionID();

	public boolean isConnected();

	public void onConnected(ServerPushConnectionCallback serverPushConnectionCallback);

	public void reconnect();

}