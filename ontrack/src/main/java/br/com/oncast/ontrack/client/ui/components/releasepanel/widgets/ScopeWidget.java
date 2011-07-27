package br.com.oncast.ontrack.client.ui.components.releasepanel.widgets;

import br.com.oncast.ontrack.shared.model.scope.Scope;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ScopeWidget extends Composite implements ModelWidget<Scope> {

	private static ScopeWidgetUiBinder uiBinder = GWT.create(ScopeWidgetUiBinder.class);

	interface ScopeWidgetUiBinder extends UiBinder<Widget, ScopeWidget> {}

	@UiField
	Label descriptionLabel;

	@UiField
	Label progressLabel;

	private final Scope scope;

	private String currentScopeDescription;

	private String currentScopeProgress;

	public ScopeWidget(final Scope scope) {
		initWidget(uiBinder.createAndBindUi(this));

		this.scope = scope;
		updateDescription();
		updateProgress();
	}

	@Override
	public void update() {
		if (!scope.getDescription().equals(currentScopeDescription)) updateDescription();
		if (!scope.getProgress().getDescription().equals(currentScopeProgress)) updateProgress();
	}

	private void updateDescription() {
		currentScopeDescription = scope.getDescription();
		descriptionLabel.setText(currentScopeDescription);
	}

	private void updateProgress() {
		currentScopeProgress = scope.getProgress().getDescription();
		progressLabel.setText(currentScopeProgress);
		progressLabel.setVisible(!currentScopeProgress.isEmpty());
	}

	public Scope getScope() {
		return scope;
	}

	@Override
	public Scope getModelObject() {
		return getScope();
	}
}
