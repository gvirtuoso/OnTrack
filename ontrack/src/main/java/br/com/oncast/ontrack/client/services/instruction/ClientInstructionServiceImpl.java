package br.com.oncast.ontrack.client.services.instruction;

import java.util.HashSet;
import java.util.Set;

import br.com.oncast.ontrack.client.ui.generalwidgets.AlignmentReference;
import br.com.oncast.ontrack.client.ui.generalwidgets.AlignmentReference.HorizontalAlignment;
import br.com.oncast.ontrack.client.ui.generalwidgets.AlignmentReference.VerticalAlignment;
import br.com.oncast.ontrack.client.ui.generalwidgets.PopupConfig;
import br.com.oncast.ontrack.client.ui.generalwidgets.instructions.InstructionWidget;
import br.com.oncast.ontrack.client.ui.generalwidgets.instructions.InstructionWidget.DismissListener;

import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.IsWidget;

public class ClientInstructionServiceImpl implements ClientInstructionService {

	@Override
	public void addWarningTip(final IsWidget widget, final String title, final String tips) {
		if (!(widget instanceof HasMouseOverHandlers) || !(widget instanceof HasMouseOutHandlers)) throw new IllegalArgumentException(
				"The widget should implement HasMouseOverHandlers");

		final Set<HandlerRegistration> registrations = new HashSet<HandlerRegistration>();

		widget.asWidget().addStyleName("hasWarningInstructions");
		final InstructionWidget instructionWidget = new InstructionWidget(title, tips, new DismissListener() {
			@Override
			public void onDismissRequested() {
				widget.asWidget().removeStyleName("hasWarningInstructions");
				for (final HandlerRegistration reg : registrations) {
					reg.removeHandler();
				}
			}
		});
		final PopupConfig config = PopupConfig.configPopup()
				.popup(instructionWidget)
				.alignHorizontal(HorizontalAlignment.LEFT, new AlignmentReference(widget.asWidget(), HorizontalAlignment.RIGHT, -100))
				.alignVertical(VerticalAlignment.BOTTOM, new AlignmentReference(widget.asWidget(), VerticalAlignment.TOP, 5));

		registrations.add(((HasMouseOverHandlers) widget).addMouseOverHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(final MouseOverEvent event) {
				config.pop();
			}
		}));

		registrations.add(((HasMouseOutHandlers) widget).addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(final MouseOutEvent event) {
				instructionWidget.scheduleFadeAnimation();
			}
		}));

		registrations.add(widget.asWidget().addAttachHandler(new Handler() {
			@Override
			public void onAttachOrDetach(final AttachEvent event) {
				if (event.isAttached()) return;
				for (final HandlerRegistration reg : registrations) {
					reg.removeHandler();
				}
			}
		}));
	}
}
