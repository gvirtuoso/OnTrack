package br.com.oncast.ontrack.client.services.storage;

import br.com.oncast.ontrack.client.ui.settings.ViewSettings.ScopeTreeColumn;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

public interface ClientStorageService {

	UUID loadSelectedScopeId(UUID defaultValue);

	void storeSelectedScopeId(UUID scopeId);

	boolean loadScopeTreeColumnVisibility(ScopeTreeColumn column);

	void storeScopeTreeColumnVisibility(ScopeTreeColumn column, boolean isVisible);

	String loadLastUserEmail(String defaultValue);

	void storeLastUserEmail(String email);

}
