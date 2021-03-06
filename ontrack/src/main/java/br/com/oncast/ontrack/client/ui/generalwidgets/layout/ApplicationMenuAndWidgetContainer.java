package br.com.oncast.ontrack.client.ui.generalwidgets.layout;

import br.com.oncast.ontrack.client.ui.components.appmenu.ApplicationMenu;

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class ApplicationMenuAndWidgetContainer extends Composite implements HasWidgets {

	private static ApplicationMenuAndWidgetContainerUiBinder uiBinder = GWT.create(ApplicationMenuAndWidgetContainerUiBinder.class);

	interface ApplicationMenuAndWidgetContainerUiBinder extends UiBinder<Widget, ApplicationMenuAndWidgetContainer> {}

	@UiField
	protected FlowPanel widgetContainer;

	@UiField(provided = true)
	protected ApplicationMenu applicationMenu;

	public ApplicationMenuAndWidgetContainer() {
		this(true);
	}

	public ApplicationMenuAndWidgetContainer(final boolean enableProjectDependantMenus) {
		this.applicationMenu = new ApplicationMenu(enableProjectDependantMenus);
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void add(final Widget w) {
		widgetContainer.add(w);
	}

	@Override
	public void clear() {
		widgetContainer.clear();
	}

	@Override
	public Iterator<Widget> iterator() {
		return widgetContainer.iterator();
	}

	@Override
	public boolean remove(final Widget w) {
		return widgetContainer.remove(w);
	}

	public ApplicationMenu getMenu() {
		return applicationMenu;
	}

	public HasWidgets getContentPanel() {
		return widgetContainer;
	}

	public FlowPanel getContentPanelWidget() {
		return widgetContainer;
	}
}
