package br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.scope;

import javax.persistence.Entity;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.model.ModelActionEntity;
import br.com.oncast.ontrack.server.util.converter.annotations.Convert;
import br.com.oncast.ontrack.shared.model.scope.actions.ScopeUpdateAction;

@Entity
@Convert(ScopeUpdateAction.class)
public class ScopeUpdateActionEntity extends ModelActionEntity {

	private String referenceId;
	private String newPattern;

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(final String referenceId) {
		this.referenceId = referenceId;
	}

	public String getNewPattern() {
		return newPattern;
	}

	public void setNewPattern(final String newPattern) {
		this.newPattern = newPattern;
	}
}
