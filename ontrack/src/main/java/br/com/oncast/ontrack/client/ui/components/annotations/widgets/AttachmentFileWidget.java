package br.com.oncast.ontrack.client.ui.components.annotations.widgets;

import br.com.oncast.ontrack.client.utils.ui.ElementUtils;
import br.com.oncast.ontrack.shared.model.file.FileRepresentation;
import br.com.oncast.ontrack.shared.services.storage.FileUploadFieldNames;

import com.google.common.base.Joiner;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ObjectElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class AttachmentFileWidget extends Composite {

	private static AttachmentFileWidgetUiBinder uiBinder = GWT.create(AttachmentFileWidgetUiBinder.class);

	interface AttachmentFileWidgetUiBinder extends UiBinder<Widget, AttachmentFileWidget> {}

	@UiField
	Image previewImage;

	@UiField
	ObjectElement previewObject;

	@UiField
	Anchor downloadLink;

	private String downloadUrl;

	public AttachmentFileWidget() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public AttachmentFileWidget(final FileRepresentation fileRepresentation) {
		this();
		ElementUtils.setVisible(previewObject, false);
		downloadLink.setTarget("_blank");
		downloadLink.setText(fileRepresentation.getFileName());

		downloadUrl = GWT.getModuleBaseURL() + "file/download?" + FileUploadFieldNames.FILE_ID + "=" + fileRepresentation.getId().toString();

		previewImage.setUrl(getDownloadUrlWithAcceptedMimetypes("image"));
		downloadLink.setHref(downloadUrl);
	}

	public String getDownloadUrlWithAcceptedMimetypes(final String... acceptedMimetypes) {
		if (acceptedMimetypes.length < 1) return downloadUrl;
		return downloadUrl + (downloadUrl.contains("?") ? "&" : "?") + FileUploadFieldNames.ACCEPT + "=" + Joiner.on(',').join(acceptedMimetypes);
	}

	@UiHandler("previewImage")
	void onImageLoadError(final ErrorEvent e) {
		previewImage.setVisible(false);
		previewObject.setData(getDownloadUrlWithAcceptedMimetypes("video", "audio", "pdf", "text"));
		ElementUtils.setVisible(previewObject, true);
	}

	@UiHandler("previewImage")
	void onImagePreviewClick(final ClickEvent e) {
		ElementUtils.click(downloadLink.getElement());
	}

}
