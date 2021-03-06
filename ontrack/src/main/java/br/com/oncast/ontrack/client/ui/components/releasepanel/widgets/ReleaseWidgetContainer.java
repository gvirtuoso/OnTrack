package br.com.oncast.ontrack.client.ui.components.releasepanel.widgets;

import br.com.oncast.ontrack.client.ui.generalwidgets.ModelWidgetContainer;
import br.com.oncast.ontrack.client.ui.generalwidgets.ModelWidgetFactory;
import br.com.oncast.ontrack.shared.model.release.Release;

// IMPORTANT This class should only be used to bypass the GWT limitation in which a class cannot have more that one 'UiFactory' for the same type.
class ReleaseWidgetContainer extends ModelWidgetContainer<Release, ReleaseWidget> {

	public ReleaseWidgetContainer(final ModelWidgetFactory<Release, ReleaseWidget> modelWidgetFactory) {
		super(modelWidgetFactory);
	}
}
