package br.com.oncast.ontrack.client;

import br.com.oncast.ontrack.client.services.ClientServices;
import br.com.oncast.ontrack.client.services.globalEvent.GlobalNativeEventService;
import br.com.oncast.ontrack.client.services.places.PlaceChangeListener;
import br.com.oncast.ontrack.client.ui.ApplicationUIPanel;
import br.com.oncast.ontrack.client.ui.generalwidgets.MaskPanel;
import br.com.oncast.ontrack.client.ui.generalwidgets.PopUpPanel;
import br.com.oncast.ontrack.client.ui.nativeeventhandlers.BrowserEscapeKeyDefaultActionPreventer;
import br.com.oncast.ontrack.client.ui.places.projectSelection.ProjectSelectionPlace;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.RootPanel;

public class ApplicationEntryPoint implements EntryPoint {

	public static final Place DEFAULT_APP_PLACE = new ProjectSelectionPlace();
	private static final ClientServices SERVICE_PROVIDER = ClientServices.get();

	@Override
	public void onModuleLoad() {
		ignoreBrowserDefaultActionForEscapeKey();
		setUpClientServices();
		setUpPlaceAwarePopupCleaner();
	}

	/**
	 * Ignores the default browser action for the ESC key down event. In some cases closes all current HTTP requests (AJAX requests as well).
	 */
	private void ignoreBrowserDefaultActionForEscapeKey() {
		GlobalNativeEventService.getInstance().addKeyDownListener(new BrowserEscapeKeyDefaultActionPreventer());
	}

	private void setUpClientServices() {
		final ApplicationUIPanel applicationUIPanel = new ApplicationUIPanel();
		RootPanel.get().add(applicationUIPanel);

		SERVICE_PROVIDER.configure(applicationUIPanel, DEFAULT_APP_PLACE);
	}

	/**
	 * Cleans the screen from pop ups every time application place changes.
	 */
	private void setUpPlaceAwarePopupCleaner() {
		SERVICE_PROVIDER.placeController().addPlaceChangeListener(new PlaceChangeListener() {

			@Override
			public void onPlaceChange(final Place newPlace) {
				MaskPanel.assureHidden();
				PopUpPanel.clear();
			}
		});
	}
}