package br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.description;

import br.com.oncast.ontrack.server.services.persistence.jpa.ActionTableColumns;
import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.model.ModelActionEntity;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConvertTo;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConvertUsing;
import br.com.oncast.ontrack.server.utils.typeConverter.custom.StringToUuidConverter;
import br.com.oncast.ontrack.shared.model.action.DescriptionRemoveAction;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "DescriptionRemove")
@ConvertTo(DescriptionRemoveAction.class)
public class DescriptionRemoveActionEntity extends ModelActionEntity {

	@Column(name = ActionTableColumns.STRING_1)
	@ConvertUsing(StringToUuidConverter.class)
	private String descriptionId;

	@Column(name = ActionTableColumns.STRING_2)
	@ConvertUsing(StringToUuidConverter.class)
	private String subjectId;

	@Column(name = ActionTableColumns.BOOLEAN_1)
	private boolean userAction;

	protected DescriptionRemoveActionEntity() {}

	public String getDescriptionId() {
		return descriptionId;
	}

	public void setDescriptionId(final String descriptionId) {
		this.descriptionId = descriptionId;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(final String subjectId) {
		this.subjectId = subjectId;
	}

	public boolean isUserAction() {
		return userAction;
	}

	public void setUserAction(final boolean userAction) {
		this.userAction = userAction;
	}

}
