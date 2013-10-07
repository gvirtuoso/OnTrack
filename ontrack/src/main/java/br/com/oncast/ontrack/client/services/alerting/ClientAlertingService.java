package br.com.oncast.ontrack.client.services.alerting;

import br.com.oncast.ontrack.client.ui.generalwidgets.ErrorMaskPanel;
import br.com.oncast.ontrack.client.ui.generalwidgets.HideHandler;
import br.com.oncast.ontrack.client.ui.generalwidgets.MaskPanel;
import br.com.oncast.ontrack.client.ui.generalwidgets.alerting.Alert;
import br.com.oncast.ontrack.client.ui.generalwidgets.alerting.AlertType;
import br.com.oncast.ontrack.client.ui.generalwidgets.alerting.AlertingContainer;
import br.com.oncast.ontrack.client.ui.generalwidgets.animation.AnimationCallback;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;

// TODO Refactor this class to be more flexible or create a builder
public class ClientAlertingService {

	public static final int DURATION_SHORT = 3000;

	public static final int DURATION_LONG = 5000;

	Widget alertingParentPanel;

	AlertingContainer alertingContainer;

	public ClientAlertingService() {
		alertingContainer = new AlertingContainer();
	}

	/**
	 * Set the Alerting's ParentWidget, useful to set the alert's position or from where it will appear
	 * 
	 * @param the
	 *            new parent widget
	 * @return the previous parent widget
	 */
	public Widget setAlertingParentWidget(final Widget widget) {
		final Widget previous = clearAlertingParentWidget();
		alertingParentPanel = widget;
		alertingParentPanel.getElement().appendChild(alertingContainer.getElement());
		return previous;
	}

	public Widget clearAlertingParentWidget() {
		if (alertingParentPanel == null) return null;

		final Widget widget = alertingParentPanel;

		alertingContainer.removeFromParent();
		alertingParentPanel = null;
		return widget;
	}

	private void addAlertToAlertingContainer(final Alert alertMessage) {
		alertingContainer.add(alertMessage);
	}

	private void removeAlertFromAlertingContainer(final Alert alertingMessage) {
		alertingContainer.remove(alertingMessage);
	}

	// TODO make this message alert like modal popup
	public ConfirmationAlertRegister showErrorWithConfirmation(final String errorMessage, final AlertConfirmationListener confirmationListener) {
		return makeConfirmationAlert(errorMessage, AlertType.ERROR, confirmationListener);
	}

	public void showModalError(final String errorDescriptionMessage) {
		makeModalAutoCloseAlert(errorDescriptionMessage, AlertType.ERROR, DURATION_LONG);
	}

	public AlertRegistration showLongDurationInfo(final String message) {
		return makeAutoCloseAlert(message, AlertType.INFO, DURATION_LONG);
	}

	public AlertRegistration showInfo(final String message) {
		return makeAutoCloseAlert(message, AlertType.INFO, DURATION_SHORT);
	}

	public AlertRegistration showError(final String message) {
		return makeAutoCloseAlert(message, AlertType.ERROR, DURATION_LONG);
	}

	public AlertRegistration showWarning(final String message) {
		return makeAutoCloseAlert(message, AlertType.WARNING, DURATION_SHORT);
	}

	public AlertRegistration showWarning(final String message, final int duration) {
		return makeAutoCloseAlert(message, AlertType.WARNING, duration);
	}

	public AlertRegistration showSuccess(final String message) {
		return showSuccess(message, DURATION_SHORT);
	}

	public AlertRegistration showSuccess(final String message, final int duration) {
		return makeAutoCloseAlert(message, AlertType.SUCCESS, duration);
	}

	public AlertRegistration showBlockingError(final String message) {
		return makeBlockingAlert(message, AlertType.ERROR);
	}

	private AlertRegistration makeBlockingAlert(final String message, final AlertType type) {
		final Alert toast = new Alert();
		addAlertToAlertingContainer(toast);

		final AlertRegistration alertRegistration = new AlertRegistration() {
			@Override
			public void hide() {
				toast.hide(new AnimationCallback() {

					@Override
					public void onComplete() {
						removeAlertFromAlertingContainer(toast);
					}
				});
			}
		};
		toast.show(message, type);
		return alertRegistration;
	}

	private AlertRegistration makeAutoCloseAlert(final String message, final AlertType type, final int autoCloseTime) {
		final Alert toast = new Alert();
		addAlertToAlertingContainer(toast);

		final AlertRegistration alertRegistration = new AlertRegistration() {
			@Override
			public void hide() {
				toast.hide(new AnimationCallback() {

					@Override
					public void onComplete() {
						removeAlertFromAlertingContainer(toast);
					}
				});
			}
		};
		toast.show(message, type, new AnimationCallback() {

			@Override
			public void onComplete() {
				new Timer() {
					@Override
					public void run() {
						alertRegistration.hide();
					}
				}.schedule(autoCloseTime);
			}
		});
		return alertRegistration;
	}

	private ConfirmationAlertRegister makeConfirmationAlert(final String message, final AlertType type, final AlertConfirmationListener listener) {
		final Alert toast = new Alert();
		addAlertToAlertingContainer(toast);

		ErrorMaskPanel.show(new HideHandler() {

			@Override
			public void onWillHide() {
				toast.hide(new AnimationCallback() {

					@Override
					public void onComplete() {
						removeAlertFromAlertingContainer(toast);
						listener.onConfirmation();
					}
				});
			}
		});
		toast.show(message, type);

		return new ConfirmationAlertRegister() {
			@Override
			public void hide(final boolean confirmation) {
				ErrorMaskPanel.assureHidden();
				removeAlertFromAlertingContainer(toast);
				if (confirmation) listener.onConfirmation();
			}
		};
	}

	private void makeModalAutoCloseAlert(final String errorDescriptionMessage, final AlertType type, final int autoCloseTime) {
		final Alert toast = new Alert();
		addAlertToAlertingContainer(toast);

		ErrorMaskPanel.show(new HideHandler() {
			@Override
			public void onWillHide() {
				toast.hide(new AnimationCallback() {

					@Override
					public void onComplete() {
						removeAlertFromAlertingContainer(toast);
					}
				});
			}
		});
		toast.show(errorDescriptionMessage, type, new AnimationCallback() {

			@Override
			public void onComplete() {
				new Timer() {
					@Override
					public void run() {
						MaskPanel.assureHidden();
					}
				}.schedule(autoCloseTime);
			}
		});
	}
}
