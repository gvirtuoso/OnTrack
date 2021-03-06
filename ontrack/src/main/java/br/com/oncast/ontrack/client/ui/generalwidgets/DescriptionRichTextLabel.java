package br.com.oncast.ontrack.client.ui.generalwidgets;

import br.com.oncast.ontrack.client.services.globalEvent.GlobalNativeEventService;
import br.com.oncast.ontrack.client.services.globalEvent.NativeEventListener;
import br.com.oncast.ontrack.client.ui.components.annotations.widgets.RichTextToolbar;
import br.com.oncast.ontrack.client.utils.html.HTMLTextUtils;
import br.com.oncast.ontrack.client.utils.keyboard.BrowserKeyCodes;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.Widget;

public class DescriptionRichTextLabel extends Composite implements HasText, HasKeyDownHandlers, HasBlurHandlers {

	private static DescriptionRichTextLabelUiBinder uiBinder = GWT.create(DescriptionRichTextLabelUiBinder.class);

	interface DescriptionRichTextLabelUiBinder extends UiBinder<Widget, DescriptionRichTextLabel> {}

	@UiField
	FocusPanel focusPanel;

	@UiField(provided = true)
	RichTextArea textArea;

	@UiField(provided = true)
	RichTextToolbar toolbar;

	@UiField
	DeckPanel deckPanel;

	@UiField
	DeckPanel descriptionDeckPanel;

	@UiField
	InlineHTML label;

	@UiField
	FocusPanel richTextArea;

	@UiField
	Label editButtom;

	private final NativeEventListener clickListener;

	private ExpansionListener expansionListener;

	private final EditableLabelEditionHandler editableLabelEditionHandler;

	private String currentText = "";

	public DescriptionRichTextLabel(final EditableLabelEditionHandler editableLabelEditionHandler) {
		this.editableLabelEditionHandler = editableLabelEditionHandler;
		textArea = new RichTextArea();
		textArea.ensureDebugId("cwRichText-area");
		toolbar = new RichTextToolbar(textArea);
		toolbar.ensureDebugId("cwRichText-toolbar");
		toolbar.setWidth("100%");

		textArea.setFocus(false);

		initWidget(uiBinder.createAndBindUi(this));

		descriptionDeckPanel.showWidget(0);
		deckPanel.setAnimationEnabled(true);

		shrink();

		clickListener = new NativeEventListener() {

			@Override
			public void onNativeEvent(final NativeEvent nativeEvent) {
				if (isInsideFocusPanel(Element.as(nativeEvent.getEventTarget()))) return;
				submitContent();
			}

			private boolean isInsideFocusPanel(final Element element) {
				Element parent = element;

				while ((parent = parent.getParentElement()) != null) {
					if (parent == focusPanel.getElement()) return true;
				}

				return false;

			}
		};
	}

	public HandlerRegistration registerExpansionListener(final ExpansionListener listener) {
		this.expansionListener = listener;
		return new HandlerRegistration() {
			@Override
			public void removeHandler() {
				expansionListener = null;
			}
		};
	}

	private void expand() {
		deckPanel.showWidget(0);
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				textArea.setFocus(true);
				if (expansionListener != null) expansionListener.onExpandded();
			}
		});
	}

	@UiHandler("richTextArea")
	public void onRichTextAreaMouseOut(final MouseOutEvent event) {
		GlobalNativeEventService.getInstance().addMouseUpListener(clickListener);
	}

	@UiHandler("editButtom")
	public void onEditLabelClick(final ClickEvent event) {
		expand();
	}

	@UiHandler("richTextArea")
	public void onRichTextAreaMouseOver(final MouseOverEvent event) {
		GlobalNativeEventService.getInstance().removeMouseUpListener(clickListener);
	}

	@UiHandler("createButton")
	public void onCreateButtonClicked(final ClickEvent event) {
		submitContent();
	}

	@UiHandler("cancelButton")
	public void onCancelButtonClicked(final ClickEvent event) {
		setText(currentText);
		hideRichTextArea();
	}

	@UiHandler("textArea")
	public void onTextAreaKeyDown(final KeyDownEvent event) {
		if (event.getNativeKeyCode() == BrowserKeyCodes.KEY_ESCAPE) hideRichTextArea();
		else if (event.getNativeKeyCode() == BrowserKeyCodes.KEY_ENTER && event.isControlKeyDown()) submitContent();
	}

	private void submitContent() {
		String text = textArea.getText().trim();
		if (currentText.equals(text)) {
			hideRichTextArea();
			return;
		}

		if (!text.isEmpty()) text = textArea.getHTML();
		setText(text);
		editableLabelEditionHandler.onEditionRequest(text);
		hideRichTextArea();
	}

	@Override
	public void setText(final String text) {
		currentText = text.trim();
		textArea.setHTML(currentText);
		label.setHTML(HTMLTextUtils.setTargetBlankInHyperLinks(currentText));
		updateVisibleWidget();
	}

	private void updateVisibleWidget() {
		descriptionDeckPanel.showWidget((currentText == null || currentText.isEmpty()) ? 0 : 1);
	}

	@Override
	public String getText() {
		return textArea.getHTML();
	}

	@Override
	public HandlerRegistration addKeyDownHandler(final KeyDownHandler handler) {
		return textArea.addKeyDownHandler(handler);
	}

	public void setFocus(final boolean b) {
		textArea.setFocus(true);
	}

	public void hideRichTextArea() {
		shrink();
		GlobalNativeEventService.getInstance().removeMouseUpListener(clickListener);
		focusPanel.setFocus(true);
	}

	private void shrink() {
		deckPanel.showWidget(1);
		if (expansionListener != null) expansionListener.onShrinked();

		updateVisibleWidget();
	}

	public interface ExpansionListener {
		void onExpandded();

		void onShrinked();
	}

	@Override
	public HandlerRegistration addBlurHandler(final BlurHandler handler) {
		return focusPanel.addBlurHandler(handler);
	}

}
