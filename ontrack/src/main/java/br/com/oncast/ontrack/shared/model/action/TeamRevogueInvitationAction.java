package br.com.oncast.ontrack.shared.model.action;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.team.TeamRevogueInvitationActionEntity;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConvertTo;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.action.helper.ActionHelper;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.user.User;
import br.com.oncast.ontrack.shared.model.user.UserRepresentation;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

import org.simpleframework.xml.Element;

@ConvertTo(TeamRevogueInvitationActionEntity.class)
public class TeamRevogueInvitationAction implements TeamAction {

	private static final long serialVersionUID = 1L;

	@Element
	private UUID userId;

	protected TeamRevogueInvitationAction() {}

	public TeamRevogueInvitationAction(final User user) {
		this(user.getId());
	}

	public TeamRevogueInvitationAction(final UUID userId) {
		this.userId = userId;
	}

	@Override
	public ModelAction execute(final ProjectContext context, final ActionContext actionContext) throws UnableToCompleteActionException {
		final UserRepresentation user = ActionHelper.findUser(userId, context, this);
		user.setValid(false);

		return new TeamInviteAction(user);
	}

	@Override
	public UUID getReferenceId() {
		return userId;
	}

}
