package br.com.oncast.ontrack.client.ui.components.annotations.widgets;

import br.com.oncast.ontrack.client.services.ClientServiceProvider;
import br.com.oncast.ontrack.client.services.user.PortableContactJsonObject;
import br.com.oncast.ontrack.client.services.user.UserDataService.LoadProfileCallback;
import br.com.oncast.ontrack.client.ui.components.annotations.widgets.menu.AnnotationMenuWidget;
import br.com.oncast.ontrack.client.ui.components.annotations.widgets.menu.DeprecateAnnotationMenuItem;
import br.com.oncast.ontrack.client.ui.components.annotations.widgets.menu.LikeAnnotationMenuItem;
import br.com.oncast.ontrack.client.ui.components.annotations.widgets.menu.SinceAnnotationMenuItem;
import br.com.oncast.ontrack.client.ui.generalwidgets.ModelWidget;
import br.com.oncast.ontrack.client.utils.date.HumanDateFormatter;
import br.com.oncast.ontrack.shared.model.annotation.Annotation;
import br.com.oncast.ontrack.shared.model.annotation.DeprecationState;
import br.com.oncast.ontrack.shared.model.file.FileRepresentation;
import br.com.oncast.ontrack.shared.model.user.User;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class AnnotationComment extends Composite implements ModelWidget<Annotation> {

	private static AnnotationCommentUiBinder uiBinder = GWT.create(AnnotationCommentUiBinder.class);

	interface AnnotationCommentUiBinder extends UiBinder<Widget, AnnotationComment> {}

	@UiField
	Image author;

	@UiField
	Label deprecatedLabel;

	@UiField
	Label closedDeprecatedLabel;

	@UiField
	DeckPanel deckPanel;

	@UiField
	HTMLPanel messageBody;

	@UiField
	AnnotationMenuWidget menu;

	private Annotation annotation;

	private UUID subjectId;

	public AnnotationComment() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public AnnotationComment(final UUID subjectId, final Annotation annotation) {
		this.subjectId = subjectId;
		this.annotation = annotation;

		initWidget(uiBinder.createAndBindUi(this));

		deckPanel.showWidget(annotation.isDeprecated() ? 1 : 0);

		updateAuthorImage();
		updateMessageBody();
		updateMenu();

		update();
	}

	@UiHandler("closedDeprecatedLabel")
	protected void onClosedDeprecatedLabelClick(final ClickEvent e) {
		deckPanel.showWidget(0);
	}

	@UiHandler("deprecatedLabel")
	protected void ondeprecatedLabelClick(final ClickEvent e) {
		deckPanel.showWidget(1);
	}

	private void updateMessageBody() {
		this.messageBody.clear();

		final FileRepresentation attachmentFile = annotation.getAttachmentFile();
		if (attachmentFile != null) {
			final AttachmentFileWidget attachedFileWidget = new AttachmentFileWidget(attachmentFile);
			messageBody.add(attachedFileWidget);
		}

		for (final String line : this.annotation.getMessage().split("\\n")) {
			messageBody.add(new HTMLPanel(SimpleHtmlSanitizer.sanitizeHtml(line)));
		}
	}

	private void updateAuthorImage() {
		final String email = this.annotation.getAuthor().getEmail();
		this.author.setUrl(ClientServiceProvider.getInstance().getUserDataService().getAvatarUrl(email));

		ClientServiceProvider.getInstance().getUserDataService().loadProfile(email, new LoadProfileCallback() {
			@Override
			public void onProfileLoaded(final PortableContactJsonObject profile) {
				author.setTitle(profile.getPreferedUsername());
			}

			@Override
			public void onProfileUnavailable(final Throwable cause) {
				author.setTitle(email);
			}
		});
	}

	@Override
	public boolean update() {
		updateDeprecation();

		menu.update();
		return false;
	}

	private void updateMenu() {
		menu.clear();

		menu.add(new DeprecateAnnotationMenuItem(subjectId, annotation));
		menu.add(new LikeAnnotationMenuItem(subjectId, annotation));
		menu.add(new SinceAnnotationMenuItem(annotation));
	}

	private void updateDeprecation() {
		final boolean isDeprecated = annotation.isDeprecated();

		if (isDeprecated) updateDeprecatedLabels();

		deprecatedLabel.setVisible(isDeprecated);

	}

	private void updateDeprecatedLabels() {
		final String absoluteDate = HumanDateFormatter.getAbsoluteText(annotation.getDeprecationTimestamp(DeprecationState.DEPRECATED));

		deprecatedLabel.setText(getDeprecationText());
		deprecatedLabel.setTitle(absoluteDate);

		closedDeprecatedLabel.setText("[Deprecated] " + annotation.getMessage());
		closedDeprecatedLabel.setTitle(absoluteDate);
	}

	private String getDeprecationText() {
		final String deprecationText = "[Deprecated by " + removeEmailDomain(annotation.getDeprecationAuthor(DeprecationState.DEPRECATED)) + " since "
				+ HumanDateFormatter.getRelativeDate(annotation.getDeprecationTimestamp(DeprecationState.DEPRECATED)) + "]";
		return deprecationText;
	}

	private String removeEmailDomain(final User user) {
		return user.getEmail().replaceAll("@.*$", "");
	}

	@Override
	public Annotation getModelObject() {
		return annotation;
	}

	public void setReadOnly(final boolean b) {
		menu.setReadOnly(b);
	}

}