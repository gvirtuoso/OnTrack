package br.com.oncast.ontrack.client.ui.components.progresspanel.widgets;

import br.com.oncast.ontrack.client.ui.components.progresspanel.interaction.ProgressPanelWidgetInteractionHandler;
import br.com.oncast.ontrack.client.ui.components.progresspanel.widgets.TextInputPopup.EditionHandler;
import br.com.oncast.ontrack.client.ui.generalwidgets.AlignmentReference;
import br.com.oncast.ontrack.client.ui.generalwidgets.AlignmentReference.HorizontalAlignment;
import br.com.oncast.ontrack.client.ui.generalwidgets.AlignmentReference.VerticalAlignment;
import br.com.oncast.ontrack.client.ui.generalwidgets.PopupConfig;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

public class KanbanColumnCreateWidget extends Composite {

	private static KanbanColumnCreateWidgetUiBinder uiBinder = GWT.create(KanbanColumnCreateWidgetUiBinder.class);

	private static KanbanColumnCreateWidgetMessages messages = GWT.create(KanbanColumnCreateWidgetMessages.class);

	interface KanbanColumnCreateWidgetUiBinder extends UiBinder<Widget, KanbanColumnCreateWidget> {}

	private final ProgressPanelWidgetInteractionHandler interactionHandler;

	private final String previousColumnDescription;

	@UiField
	FocusPanel rootPanel;

	@UiField
	FocusPanel create;

	public KanbanColumnCreateWidget(final ProgressPanelWidgetInteractionHandler interactionHandler, final String preciousColumnDescription) {
		this.interactionHandler = interactionHandler;
		this.previousColumnDescription = preciousColumnDescription;
		initWidget(uiBinder.createAndBindUi(this));
		create.setTitle(messages.addNewColumn());
	}

	@UiHandler("create")
	protected void onClick(final ClickEvent event) {
		PopupConfig.configPopup().popup(new TextInputPopup(messages.columnDescription(), messages.newColumn(), new EditionHandler() {
			@Override
			public boolean onEdition(final String text) {
				final String trimmedText = text.trim();
				if (trimmedText.isEmpty()) return false;
				interactionHandler.onKanbanColumnCreate(text, previousColumnDescription);
				return true;
			}
		})).alignVertical(VerticalAlignment.TOP, new AlignmentReference(create, VerticalAlignment.BOTTOM, 0))
				.alignHorizontal(HorizontalAlignment.CENTER, new AlignmentReference(create, HorizontalAlignment.CENTER))
				.pop();
	}
}
