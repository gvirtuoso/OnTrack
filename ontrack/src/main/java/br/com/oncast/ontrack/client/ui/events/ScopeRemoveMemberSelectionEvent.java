package br.com.oncast.ontrack.client.ui.events;

import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.user.UserRepresentation;

import com.google.gwt.event.shared.GwtEvent;

public class ScopeRemoveMemberSelectionEvent extends GwtEvent<ScopeRemoveMemberSelectionEventHandler> implements ScopeMemberSelectionEvent {

	public static Type<ScopeRemoveMemberSelectionEventHandler> TYPE;
	private final Scope scope;
	private final UserRepresentation member;

	public ScopeRemoveMemberSelectionEvent(final UserRepresentation member, final Scope scope) {
		this.member = member;
		this.scope = scope;
	}

	public static Type<ScopeRemoveMemberSelectionEventHandler> getType() {
		return TYPE == null ? TYPE = new Type<ScopeRemoveMemberSelectionEventHandler>() : TYPE;
	}

	@Override
	public Type<ScopeRemoveMemberSelectionEventHandler> getAssociatedType() {
		return ScopeRemoveMemberSelectionEvent.getType();
	}

	@Override
	protected void dispatch(final ScopeRemoveMemberSelectionEventHandler handler) {
		handler.clearSelection(this);
	}

	@Override
	public Scope getTargetScope() {
		return scope;
	}

	@Override
	public UserRepresentation getMember() {
		return member;
	}

}
