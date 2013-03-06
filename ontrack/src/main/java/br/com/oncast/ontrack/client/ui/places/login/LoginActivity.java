package br.com.oncast.ontrack.client.ui.places.login;

import br.com.oncast.ontrack.client.services.ClientServiceProvider;
import br.com.oncast.ontrack.client.services.authentication.UserAuthenticationCallback;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class LoginActivity extends AbstractActivity implements LoginView.Presenter {

	private static final ClientServiceProvider SERVICE_PROVIDER = ClientServiceProvider.getInstance();
	private final UserAuthenticationCallback authenticationCallback;
	private final LoginView view;

	public LoginActivity(final Place destinationPlace) {
		this.view = new LoginPanel(this);

		this.authenticationCallback = new UserAuthenticationCallback() {

			@Override
			public void onUserAuthenticatedSuccessfully(final String username, final UUID userId) {
				view.enable();
				SERVICE_PROVIDER.getClientStorageService().storeLastUserEmail(username);
				SERVICE_PROVIDER.getApplicationPlaceController().goTo(destinationPlace);
			}

			@Override
			public void onUnexpectedFailure(final Throwable caught) {
				view.enable();
				SERVICE_PROVIDER.getClientAlertingService().showError(ClientServiceProvider.getInstance().getClientErrorMessages().unexpectedError());
			}

			@Override
			public void onIncorrectCredentialsFailure() {
				view.enable();
				view.onIncorrectCredentials();
				SERVICE_PROVIDER.getClientAlertingService().showError(ClientServiceProvider.getInstance().getClientErrorMessages().incorrectUserOrPassword());
			}

		};
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		panel.setWidget(view.asWidget());
		SERVICE_PROVIDER.getClientAlertingService().setAlertingParentWidget(view.asWidget());
		view.setUsername(SERVICE_PROVIDER.getClientStorageService().loadLastUserEmail(""));
	}

	@Override
	public void onStop() {
		SERVICE_PROVIDER.getClientAlertingService().clearAlertingParentWidget();
	}

	@Override
	public void onAuthenticationRequest(final String username, final String password) {
		view.disable();
		SERVICE_PROVIDER.getAuthenticationService().authenticate(username, password, authenticationCallback);
	}

	@Override
	public void onResetPasswordRequest(final String username) {
		view.disable();
		// FIXME LOBO i18n
		SERVICE_PROVIDER.getClientAlertingService().showInfo("Request new password for '" + username + "'.");
		SERVICE_PROVIDER.getAuthenticationService().resetPasswordFor(username, new ResetPasswordCallback() {
			@Override
			public void onUserPasswordResetSuccessfully() {
				view.enable();
				SERVICE_PROVIDER.getClientStorageService().storeLastUserEmail(username);
				// FIXME LOBO i18n
				SERVICE_PROVIDER.getClientAlertingService().showSuccess("An e-mail with a new passowrd was sent.");
			}

			@Override
			public void onUnexpectedFailure(final Throwable caught) {
				view.enable();
				SERVICE_PROVIDER.getClientAlertingService().showError(ClientServiceProvider.getInstance().getClientErrorMessages().unexpectedError());
			}

			@Override
			public void onIncorrectCredentialsFailure() {
				view.enable();
				view.onIncorrectUsername();
				// FIXME LOBO i18n
				SERVICE_PROVIDER.getClientAlertingService().showError("Invalid user... Nothing was done.");
			}
		});
	}
}
