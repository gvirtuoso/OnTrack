package br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.impediments;

import br.com.oncast.ontrack.server.services.persistence.jpa.ActionTableColumns;
import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.model.ModelActionEntity;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConvertTo;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConvertUsing;
import br.com.oncast.ontrack.server.utils.typeConverter.custom.StringToUuidConverter;
import br.com.oncast.ontrack.shared.model.action.ImpedimentRemoveAction;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "ImpedimentRemove")
@ConvertTo(ImpedimentRemoveAction.class)
public class ImpedimentRemoveActionEntity extends ModelActionEntity {

	@ConvertUsing(StringToUuidConverter.class)
	@Column(name = ActionTableColumns.STRING_1)
	private String annotationId;

	@ConvertUsing(StringToUuidConverter.class)
	@Column(name = ActionTableColumns.STRING_2)
	private String subjectId;

	@Column(name = ActionTableColumns.STRING_3)
	private String previousType;

	public String getAnnotationId() {
		return annotationId;
	}

	public void setAnnotationId(final String annotationId) {
		this.annotationId = annotationId;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(final String subjectId) {
		this.subjectId = subjectId;
	}

	public String getPreviousType() {
		return previousType;
	}

	public void setPreviousType(final String previousType) {
		this.previousType = previousType;
	}
}
