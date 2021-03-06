package br.com.oncast.ontrack.client.ui.components.scopetree.events;

import br.com.oncast.ontrack.shared.model.uuid.UUID;

import com.google.gwt.event.shared.GwtEvent;

public class ScopeTreeItemDeclareValueEvent extends GwtEvent<ScopeTreeItemDeclareValueEventHandler> {

	public static Type<ScopeTreeItemDeclareValueEventHandler> TYPE;
	private final UUID scopeId;
	private final String valueDescription;

	public ScopeTreeItemDeclareValueEvent(final UUID scopeId, final String valueDescription) {
		this.scopeId = scopeId;
		this.valueDescription = valueDescription;
	}

	public static Type<ScopeTreeItemDeclareValueEventHandler> getType() {
		if (TYPE == null) {
			TYPE = new Type<ScopeTreeItemDeclareValueEventHandler>();
		}
		return TYPE;
	}

	@Override
	public Type<ScopeTreeItemDeclareValueEventHandler> getAssociatedType() {
		return ScopeTreeItemDeclareValueEvent.getType();
	}

	@Override
	protected void dispatch(final ScopeTreeItemDeclareValueEventHandler handler) {
		handler.onDeclareValueRequest(scopeId, valueDescription);
	}
}
