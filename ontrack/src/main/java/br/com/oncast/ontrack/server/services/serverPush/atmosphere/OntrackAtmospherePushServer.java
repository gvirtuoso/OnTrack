package br.com.oncast.ontrack.server.services.serverPush.atmosphere;

import br.com.oncast.ontrack.server.services.serverPush.CometClientConnection;
import br.com.oncast.ontrack.server.services.serverPush.InternalConnectionListener;
import br.com.oncast.ontrack.server.services.serverPush.ServerPushApi;
import br.com.oncast.ontrack.server.services.serverPush.ServerPushConnection;
import br.com.oncast.ontrack.shared.services.serverPush.ServerPushEvent;

import java.util.HashMap;
import java.util.Map;

import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.gwt.server.GwtAtmosphereResource;

public class OntrackAtmospherePushServer implements ServerPushApi {

	private final Map<ServerPushConnection, GwtAtmosphereResource> cometSessionMap = new HashMap<ServerPushConnection, GwtAtmosphereResource>();

	@Override
	public void pushEvent(final ServerPushEvent serverPushEvent, final ServerPushConnection client) {
		final GwtAtmosphereResource resource = cometSessionMap.get(client);
		if (resource != null) resource.getBroadcaster().broadcast(serverPushEvent);
	}

	@Override
	public void setConnectionListener(final InternalConnectionListener connectionListener) {
		OntrackAtmosphereHandler.setConnectionListener(new AtmosphereConnectionListener() {

			@Override
			public void disconnected(final GwtAtmosphereResource resource) {
				final ServerPushConnection identifier = toResourceIdentifier(resource);
				cometSessionMap.remove(identifier);
				connectionListener.onClientDisconnected(identifier);
			}

			@Override
			public void connected(final GwtAtmosphereResource resource) {

				final ServerPushConnection resourceIdentifier = toResourceIdentifier(resource);
				setupBroadcaster(resource, resourceIdentifier);
				cometSessionMap.put(resourceIdentifier, resource);
				connectionListener.onClientConnected(resourceIdentifier);
			}

		});
	}

	private void setupBroadcaster(final GwtAtmosphereResource resource, final ServerPushConnection resourceIdentifier) {
		resource.getAtmosphereResource().setBroadcaster(BroadcasterFactory.getDefault().get(resourceIdentifier));
	}

	private ServerPushConnection toResourceIdentifier(final GwtAtmosphereResource resource) {
		return new CometClientConnection(resource.getConnectionUUID(), resource.getSession().getId());
	}
}
