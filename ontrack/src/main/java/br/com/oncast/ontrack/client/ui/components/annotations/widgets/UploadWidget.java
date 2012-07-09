package br.com.oncast.ontrack.client.ui.components.annotations.widgets;

import java.util.Set;

import br.com.oncast.ontrack.client.services.ClientServiceProvider;
import br.com.oncast.ontrack.client.services.actionExecution.ActionExecutionListener;
import br.com.oncast.ontrack.client.services.actionExecution.ActionExecutionService;
import br.com.oncast.ontrack.shared.model.action.FileUploadAction;
import br.com.oncast.ontrack.shared.model.action.ModelAction;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.project.ProjectRepresentation;
import br.com.oncast.ontrack.shared.model.uuid.UUID;
import br.com.oncast.ontrack.shared.services.storage.FileUploadFieldNames;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.URL;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;

public class UploadWidget extends Composite {

	private static UploadWidgetUiBinder uiBinder = GWT.create(UploadWidgetUiBinder.class);

	interface UploadWidgetUiBinder extends UiBinder<Widget, UploadWidget> {}

	interface UploadWidgetStyle extends CssResource {
		String removeImg();
	}

	@UiField
	protected UploadWidgetStyle style;

	@UiField
	protected FocusPanel uploadIcon;

	@UiField
	protected SimplePanel formPanel;

	@UiField
	protected Label fileNameLabel;

	protected FileUpload fileUpload;

	protected TextBox fileName;

	protected TextBox projectId;

	protected FormPanel form;

	private ActionExecutionListener actionExecutionListener;

	private String actionUrl;

	private FlowPanel formContainer;

	public UploadWidget() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void setActionUrl(final String url) {
		actionUrl = URL.encode(url);
	}

	@Override
	protected void onLoad() {
		setUploadFieldVisible(false);
	}

	public void setUploadFieldVisible(final boolean b) {
		fileNameLabel.setVisible(b);
		uploadIcon.setStyleName(style.removeImg(), b);
	}

	private static native void clickOnInputFile(Element elem) /*-{
		elem.click();
	}-*/;

	@UiHandler("uploadIcon")
	protected void onUploadIconClicked(final ClickEvent e) {
		if (!isUploadWidgetVisible()) clickOnInputFile(getFileUpload().getElement());
		else setUploadFieldVisible(false);
	}

	private boolean isUploadWidgetVisible() {
		return fileNameLabel.isVisible();
	}

	public String getFilename() {
		final String name = getFileUpload().getFilename();
		return name.substring(name.lastIndexOf("\\") + 1);
	}

	public void submitForm(final UploadWidgetListener listener) {
		final String filename = getFilename();
		if (!isUploadWidgetVisible() || filename.isEmpty()) {
			listener.onUploadCompleted(null);
			return;
		}

		ClientServiceProvider.getInstance().getClientNotificationService().showInfo("Uploading your file...");
		final FormPanel form = getForm();
		getProjectId().setValue(getCurrentProject().getId().toStringRepresentation());
		getFileName().setValue(filename);
		getActionExecutionService().addActionExecutionListener(getActionExecutionListener(filename, listener));
		form.submit();
	}

	private FileUpload getFileUpload() {
		if (fileUpload == null) {
			fileUpload = new FileUpload();
			fileUpload.setName(FileUploadFieldNames.FILE);
			fileUpload.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(final ChangeEvent event) {
					final String name = getFilename();
					fileNameLabel.setText(name);
					setUploadFieldVisible(!name.isEmpty());
				}
			});
			getFormContainer().add(fileUpload);
		}
		return fileUpload;
	}

	private TextBox getProjectId() {
		if (projectId == null) {
			projectId = new TextBox();
			projectId.setName(FileUploadFieldNames.PROJECT_ID);
			getFormContainer().add(projectId);
		}
		return projectId;
	}

	private TextBox getFileName() {
		if (fileName == null) {
			fileName = new TextBox();
			fileName.setName(FileUploadFieldNames.FILE_NAME);
			getFormContainer().add(fileName);
		}
		return fileName;
	}

	private FlowPanel getFormContainer() {
		if (formContainer == null) {
			formContainer = new FlowPanel();
			getForm().setWidget(formContainer);
		}
		return formContainer;
	}

	private FormPanel getForm() {
		if (form == null) {
			form = new FormPanel();
			form.setVisible(false);

			formPanel.add(form);
			form.addSubmitHandler(new SubmitHandler() {
				@Override
				public void onSubmit(final SubmitEvent event) {
					form.setEncoding(FormPanel.ENCODING_MULTIPART);
					form.setMethod(FormPanel.METHOD_POST);
					form.setAction(actionUrl);
				}
			});
		}
		return form;
	}

	private ProjectRepresentation getCurrentProject() {
		return ClientServiceProvider.getInstance().getProjectRepresentationProvider().getCurrent();
	}

	private ActionExecutionListener getActionExecutionListener(final String filename, final UploadWidgetListener listener) {
		return actionExecutionListener = new ActionExecutionListener() {
			@Override
			public void onActionExecution(final ModelAction action, final ProjectContext context, final Set<UUID> inferenceInfluencedScopeSet,
					final boolean isUserAction) {
				if (action instanceof FileUploadAction) {
					final FileUploadAction uploadAction = (FileUploadAction) action;
					if (filename.equals(uploadAction.getFileName())) {
						getActionExecutionService().removeActionExecutionListener(actionExecutionListener);
						ClientServiceProvider.getInstance().getClientNotificationService().showSuccess("Upload Completed!");
						listener.onUploadCompleted(uploadAction.getReferenceId());
					}
				}
			}

		};
	}

	private ActionExecutionService getActionExecutionService() {
		return ClientServiceProvider.getInstance().getActionExecutionService();
	}

	public interface UploadWidgetListener {

		void onUploadCompleted(UUID referenceId);

	}

}