package br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.scope;

import javax.persistence.Entity;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.model.ModelActionEntity;
import br.com.oncast.ontrack.server.util.typeConverter.annotations.ConvertTo;
import br.com.oncast.ontrack.server.util.typeConverter.annotations.ConvertUsing;
import br.com.oncast.ontrack.server.util.typeConverter.custom.StringToUuidConverter;
import br.com.oncast.ontrack.shared.model.scope.actions.ScopeMoveDownAction;

@Entity
@ConvertTo(ScopeMoveDownAction.class)
public class ScopeMoveDownActionEntity extends ModelActionEntity {

	@ConvertUsing(StringToUuidConverter.class)
	private String referenceId;

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(final String referenceId) {
		this.referenceId = referenceId;
	}
}
