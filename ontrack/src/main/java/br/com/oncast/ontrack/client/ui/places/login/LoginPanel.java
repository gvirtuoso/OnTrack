package br.com.oncast.ontrack.client.ui.places.login;

import br.com.oncast.ontrack.client.services.ClientServices;
import br.com.oncast.ontrack.client.services.validation.EmailValidator;
import br.com.oncast.ontrack.client.ui.generalwidgets.layout.ApplicationWidgetContainer;
import br.com.oncast.ontrack.client.ui.generalwidgets.layout.ValidationInputContainer;
import br.com.oncast.ontrack.client.ui.generalwidgets.layout.ValidationInputContainer.ValidationHandler;
import br.com.oncast.ontrack.client.utils.keyboard.BrowserKeyCodes;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SubmitButton;
import com.google.gwt.user.client.ui.Widget;

public class LoginPanel extends Composite implements LoginView {

	private static LoginPanelUiBinder uiBinder = GWT.create(LoginPanelUiBinder.class);

	interface LoginPanelUiBinder extends UiBinder<Widget, LoginPanel> {}

	@UiField
	ApplicationWidgetContainer root;

	@UiField
	protected ValidationInputContainer emailArea;

	@UiField
	protected ValidationInputContainer passwordArea;

	@UiField
	protected SubmitButton loginButton;

	@UiField
	protected FormPanel form;

	@UiField
	protected Anchor forgotPassword;

	private boolean isEmailValid;

	private final Presenter presenter;

	public LoginPanel(final LoginView.Presenter presenter) {
		this.presenter = presenter;
		initWidget(uiBinder.createAndBindUi(this));
		emailArea.setHandler(new ValidationHandler() {
			@Override
			public boolean isValid(final String email) {
				isEmailValid = !email.trim().isEmpty() && EmailValidator.isValid(email);
				return isEmailValid;
			}

			@Override
			public void onSubmit() {}

		});

		passwordArea.setHandler(new ValidationHandler() {
			@Override
			public boolean isValid(final String value) {
				return true;
			}

			@Override
			public void onSubmit() {}
		});

		form.addSubmitHandler(new SubmitHandler() {
			@Override
			public void onSubmit(final SubmitEvent event) {
				event.cancel();
			}
		});
	}

	private void doAuthenticate() {
		if (!isEmailValid) return;

		presenter.onAuthenticationRequest(emailArea.getText(), passwordArea.getText());
	}

	private void doResetPassword() {
		if (!isEmailValid) {
			final ClientServices serviceProvider = ClientServices.get();
			serviceProvider.alerting().showError(serviceProvider.messages().passwordRequestNeedsUsernameInput());
			emailArea.setFocus(true);
			return;
		}

		presenter.onResetPasswordRequest(emailArea.getText());
	}

	@UiHandler("emailArea")
	protected void onAttach(final AttachEvent event) {
		if (!event.isAttached()) return;
		emailArea.setFocus(true);
	}

	@UiHandler("emailArea")
	protected void onEmailTab(final KeyDownEvent event) {
		if (event.getNativeKeyCode() != BrowserKeyCodes.KEY_TAB) return;

		passwordArea.setFocus(true);
		event.preventDefault();
	}

	@UiHandler("loginButton")
	protected void onLoginClick(final ClickEvent e) {
		doAuthenticate();
	}

	@UiHandler("passwordArea")
	protected void onPasswordTab(final KeyDownEvent event) {
		if (event.getNativeKeyCode() != BrowserKeyCodes.KEY_TAB) return;

		emailArea.setFocus(true);
		event.preventDefault();
	}

	@UiHandler("forgotPassword")
	protected void onForgotPasswordLinkCLick(final ClickEvent event) {
		doResetPassword();
	}

	@Override
	public void disable() {
		emailArea.setEnabled(false);
		passwordArea.setEnabled(false);
		loginButton.setEnabled(false);
	}

	@Override
	public void enable() {
		emailArea.setEnabled(true);
		passwordArea.setEnabled(true);
		loginButton.setEnabled(true);
	}

	@Override
	public void onIncorrectCredentials() {
		emailArea.update(false);
		passwordArea.update(false);
	}

	@Override
	public void setUsername(final String username) {
		if (username.trim().isEmpty()) return;

		emailArea.setText(username);
		passwordArea.setFocus(true);
	}

	@Override
	public void onIncorrectUsername() {
		emailArea.update(false);
		passwordArea.update(true);
		emailArea.setFocus(true);
	}

	@Override
	public Panel getAlertingContainer() {
		return root.getAlertingContainer();
	}
}
