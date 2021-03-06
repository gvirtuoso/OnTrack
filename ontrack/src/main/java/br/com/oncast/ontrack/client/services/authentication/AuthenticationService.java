package br.com.oncast.ontrack.client.services.authentication;

import br.com.oncast.ontrack.client.ui.places.login.ResetPasswordCallback;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

import com.google.gwt.place.shared.Place;

public interface AuthenticationService {

	void authenticate(String login, String password, UserAuthenticationCallback callback);

	void logout();

	void changePassword(String oldPassword, String newPassword, UserPasswordChangeCallback callback);

	void registerUserAuthenticationListener(UserAuthenticationListener listener);

	void unregisterUserAuthenticatedListener(UserAuthenticationListener listener);

	UUID getCurrentUserId();

	void loadCurrentUserInformation(UserInformationLoadCallback userInformationLoadCallback);

	void registerAuthenticationExceptionGlobalHandler();

	boolean isUserAvailable();

	void onUserLogout();

	void onUserLoginRequired(Place destinationPlace);

	boolean canCurrentUserManageProjects();

	void resetPasswordFor(String username, ResetPasswordCallback resetPasswordCallback);

	boolean isCurrentUserActivated();
}
