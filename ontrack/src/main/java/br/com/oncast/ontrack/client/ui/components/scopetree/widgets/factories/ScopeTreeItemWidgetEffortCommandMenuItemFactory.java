package br.com.oncast.ontrack.client.ui.components.scopetree.widgets.factories;

import br.com.oncast.ontrack.client.ui.components.scopetree.widgets.ScopeTreeItemWidgetEditionHandler;
import br.com.oncast.ontrack.client.ui.generalwidgets.SimpleCommandMenuItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;

public class ScopeTreeItemWidgetEffortCommandMenuItemFactory implements ScopeTreeItemWidgetCommandMenuItemFactory {

	private static final CommandMenuMessages messages = GWT.create(CommandMenuMessages.class);

	private final ScopeTreeItemWidgetEditionHandler controller;

	public ScopeTreeItemWidgetEffortCommandMenuItemFactory(final ScopeTreeItemWidgetEditionHandler controller) {
		this.controller = controller;
	}

	@Override
	public SimpleCommandMenuItem createCustomItem(final String inputText) {
		return new SimpleCommandMenuItem(messages.use(inputText), inputText, new Command() {

			@Override
			public void execute() {
				controller.declareEffort(inputText);
			}
		});
	}

	@Override
	public SimpleCommandMenuItem createItem(final String itemText, final String effortToDeclare) {
		return new SimpleCommandMenuItem(itemText, effortToDeclare, new Command() {

			@Override
			public void execute() {
				controller.declareEffort(effortToDeclare);
			}
		});
	}

	@Override
	public String getNoItemText() {
		return null;
	}

	@Override
	public boolean shouldPrioritizeCustomItem() {
		return true;
	}
}
