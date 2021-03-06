package br.com.oncast.ontrack.client.services.user;

import br.com.drycode.api.web.gwt.dispatchService.client.DispatchCallback;
import br.com.drycode.api.web.gwt.dispatchService.client.DispatchService;

import br.com.oncast.ontrack.client.services.context.ContextChangeListener;
import br.com.oncast.ontrack.client.services.context.ContextProviderService;
import br.com.oncast.ontrack.client.services.serverPush.ServerPushClientService;
import br.com.oncast.ontrack.client.services.serverPush.ServerPushEventHandler;
import br.com.oncast.ontrack.shared.model.user.User;
import br.com.oncast.ontrack.shared.model.user.UserRepresentation;
import br.com.oncast.ontrack.shared.model.uuid.UUID;
import br.com.oncast.ontrack.shared.services.requestDispatch.UserDataRequest;
import br.com.oncast.ontrack.shared.services.requestDispatch.UserDataRequestResponse;
import br.com.oncast.ontrack.shared.services.requestDispatch.UserDataUpdateRequest;
import br.com.oncast.ontrack.shared.services.requestDispatch.UserDataUpdateRequestResponse;
import br.com.oncast.ontrack.shared.services.user.UserInformationUpdateEvent;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserDataServiceImpl implements UserDataService {

	private static final String GRAVATAR_BASE_URL = "https://secure.gravatar.com/";

	private static final int GRAVATAR_SUCCESS_CODE = 200;

	private final DispatchService dispatchService;

	private final Set<User> cachedUsers;

	private final SetMultimap<UUID, UserSpecificInformationChangeListener> userSpecificListeners;

	private final ContextProviderService contextProvider;

	public UserDataServiceImpl(final DispatchService dispatchService, final ContextProviderService contextProvider, final ServerPushClientService serverPushClientService) {

		this.dispatchService = dispatchService;
		this.contextProvider = contextProvider;

		userSpecificListeners = HashMultimap.create();
		cachedUsers = new HashSet<User>();

		contextProvider.addContextLoadListener(new ContextChangeListener() {
			@Override
			public void onProjectChanged(final UUID projectId, final Long leadedProjectRevision) {
				if (projectId == null) clearCachedUsers();
				else updateUserDataFor(projectId);
			}
		});

		serverPushClientService.registerServerEventHandler(UserInformationUpdateEvent.class, new ServerPushEventHandler<UserInformationUpdateEvent>() {
			@Override
			public void onEvent(final UserInformationUpdateEvent event) {
				final User user = event.getUser();
				cachedUsers.add(user);
				notifyUserDataUpdate(user);
			}
		});
	}

	@Override
	public void hasAvatarInGravatar(final User user, final UserHasGravatarCallback callback) {
		final String urlAddress = GRAVATAR_BASE_URL + "avatar/" + getMd5Hex(user.getEmail()) + "?s=1&d=404";

		try {
			new RequestBuilder(RequestBuilder.GET, urlAddress).sendRequest(null, new RequestCallback() {
				@Override
				public void onResponseReceived(final Request request, final Response response) {
					callback.onResponseReceived(response.getStatusCode() == GRAVATAR_SUCCESS_CODE);
				}

				@Override
				public void onError(final Request request, final Throwable exception) {
					callback.onResponseReceived(false);
				}
			});
		} catch (final RequestException e) {
			callback.onResponseReceived(false);
		}
	}

	@Override
	public SafeUri getAvatarUrl(final User user) {
		return new SafeUri() {
			@Override
			public String asString() {
				return new String(GRAVATAR_BASE_URL + "avatar/" + getMd5Hex(user.getEmail()) + "?s=40");
			}

		};
	}

	@Override
	public SafeUri getAvatarUrl(final User user, final int imageSize) {
		return new SafeUri() {
			@Override
			public String asString() {
				return new String(GRAVATAR_BASE_URL + "avatar/" + getMd5Hex(user.getEmail()) + "?s=" + imageSize);
			}

		};
	}

	// TODO+++++ find a good way of generating MD5
	private String getMd5Hex(final String email) {
		try {
			final String normalizedEmail = email.trim().toLowerCase();
			final byte[] digest = MessageDigest.getInstance("MD5").digest(normalizedEmail.getBytes());

			final BigInteger bigInteger = new BigInteger(1, digest); // This can remove heading 0s;
			return toStringWithTraillingZeros(bigInteger, 32); // Add possibly removed heading 0s
		} catch (final NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	private String toStringWithTraillingZeros(final BigInteger integer, final int length) {
		String md5 = integer.toString(16);
		for (int i = md5.length(); i < length; i++) {
			md5 = "0" + md5;
		}
		return md5;
	}

	@Override
	public void onUserDataUpdate(final User user, final AsyncCallback<User> callback) {
		dispatchService.dispatch(new UserDataUpdateRequest(user), new DispatchCallback<UserDataUpdateRequestResponse>() {
			@Override
			public void onSuccess(final UserDataUpdateRequestResponse result) {
				callback.onSuccess(result.getUser());
			}

			@Override
			public void onTreatedFailure(final Throwable caught) {
				callback.onFailure(caught);
			}

			@Override
			public void onUntreatedFailure(final Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	private void updateUserDataFor(final UUID projectId) {
		clearCachedUsers();
		dispatchService.dispatch(new UserDataRequest(contextProvider.getCurrent().getUsers()), new DispatchCallback<UserDataRequestResponse>() {

			@Override
			public void onSuccess(final UserDataRequestResponse result) {
				final List<User> users = result.getUsers();
				cachedUsers.addAll(users);
				notifyAllListeners(users);
			}

			@Override
			public void onTreatedFailure(final Throwable caught) {
				caught.printStackTrace();
			}

			@Override
			public void onUntreatedFailure(final Throwable caught) {
				caught.printStackTrace();
			}

		});
	}

	private void clearCachedUsers() {
		cachedUsers.clear();
	}

	private void notifyUserDataUpdate(final User user) {
		for (final UserSpecificInformationChangeListener listener : userSpecificListeners.get(user.getId()))
			listener.onInformationChange(user);
	}

	private void notifyAllListeners(final List<User> users) {
		for (final User user : users)
			notifyUserDataUpdate(user);
	}

	@Override
	public void loadRealUser(final UUID userId, final AsyncCallback<User> callback) {
		if (cachedUsers.contains(userId)) {
			callback.onSuccess(getRealUser(userId));
			return;
		}

		dispatchService.dispatch(new UserDataRequest(userId), new DispatchCallback<UserDataRequestResponse>() {

			@Override
			public void onSuccess(final UserDataRequestResponse result) {
				final User user = result.getUsers().get(0);
				cachedUsers.add(user);
				notifyUserDataUpdate(user);
				callback.onSuccess(user);
			}

			@Override
			public void onTreatedFailure(final Throwable caught) {
				caught.printStackTrace();
				callback.onFailure(caught);
			}

			@Override
			public void onUntreatedFailure(final Throwable caught) {
				caught.printStackTrace();
				callback.onFailure(caught);
			}

		});
	}

	@Override
	public User getRealUser(final UserRepresentation userRepresentation) {
		return getRealUser(userRepresentation.getId());
	}

	@Override
	public User getRealUser(final UUID userId) {
		for (final User user : cachedUsers) {
			if (user.getId().equals(userId)) return user;
		}
		throw new IllegalStateException("User information unavailable");
	}

	@Override
	public HandlerRegistration registerListenerForSpecificUser(final UserRepresentation user, final UserSpecificInformationChangeListener listener) {
		return registerListenerForSpecificUser(user.getId(), listener);
	}

	@Override
	public HandlerRegistration registerListenerForSpecificUser(final UUID userId, final UserSpecificInformationChangeListener listener) {
		userSpecificListeners.put(userId, listener);
		if (cachedUsers.contains(userId)) listener.onInformationChange(getRealUser(userId));

		return new HandlerRegistration() {
			@Override
			public void removeHandler() {
				userSpecificListeners.remove(userId, listener);
			}
		};
	}

	public interface UserSpecificInformationChangeListener {

		void onInformationChange(User user);

	}

	@Override
	public List<User> getUsersList() {
		return new ArrayList<User>(cachedUsers);
	}

}
