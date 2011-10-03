package br.com.oncast.ontrack.client.ui.components.releasepanel.widgets;

import br.com.oncast.ontrack.client.ui.components.releasepanel.DragAndDropManager;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.utils.deepEquality.IgnoredByDeepEquality;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ReleasePanelWidget extends Composite {

	interface ReleasePanelWidgetUiBinder extends UiBinder<Widget, ReleasePanelWidget> {}

	@IgnoredByDeepEquality
	private static ReleasePanelWidgetUiBinder uiBinder = GWT.create(ReleasePanelWidgetUiBinder.class);

	@UiField
	@IgnoredByDeepEquality
	protected VerticalModelWidgetContainer<Release, ReleaseWidget> releaseContainer;

	@UiField
	protected AbsolutePanel boundaryPanel;

	private Release rootRelease;

	// IMPORTANT: This field cannot be 'final' because some tests need to set it to a new value through reflection. Do not remove the 'null' attribution.
	@IgnoredByDeepEquality
	private ModelWidgetFactory<Release, ReleaseWidget> releaseWidgetFactory = null;

	public ReleasePanelWidget(final ReleasePanelWidgetInteractionHandler releasePanelInteractionHandler) {
		final DragAndDropManager dragAndDropManager = new DragAndDropManager();
		releaseWidgetFactory = new ReleaseWidgetFactory(releasePanelInteractionHandler, new ScopeWidgetFactory(releasePanelInteractionHandler,
				dragAndDropManager.getDraggableItemListener()), dragAndDropManager.getDropTargetCreationListener());

		initWidget(uiBinder.createAndBindUi(this));
		dragAndDropManager.configureBoundaryPanel(boundaryPanel);
	}

	public void setRelease(final Release rootRelease) {
		this.rootRelease = rootRelease;
		releaseContainer.clear();

		for (final Release childRelease : rootRelease.getChildren())
			releaseContainer.createChildModelWidget(childRelease);
	}

	public void update() {
		releaseContainer.update(rootRelease.getChildren());
	}

	@UiFactory
	protected VerticalModelWidgetContainer<Release, ReleaseWidget> createReleaseContainer() {
		return new VerticalModelWidgetContainer<Release, ReleaseWidget>(releaseWidgetFactory, new ModelWidgetContainerListener() {

			@Override
			public void onUpdateComplete(final boolean hasChanged, final boolean hasNewWidgets) {}
		});
	}
}
