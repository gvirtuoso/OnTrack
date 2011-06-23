package br.com.oncast.ontrack.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class ApplicationUIPanel extends Composite implements AcceptsOneWidget {

	private static ApplicationUIPanelUiBinder uiBinder = GWT.create(ApplicationUIPanelUiBinder.class);

	interface ApplicationUIPanelUiBinder extends UiBinder<Widget, ApplicationUIPanel> {}

	@UiField
	SimplePanel panel;

	public ApplicationUIPanel() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setWidget(final IsWidget w) {
		panel.setWidget(w);
	}
}