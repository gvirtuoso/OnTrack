package br.com.oncast.ontrack.shared.model.action;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.annotation.AnnotationVoteRemoveActionEntity;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConvertTo;
import br.com.oncast.ontrack.shared.exceptions.ActionExecutionErrorMessageCode;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.action.helper.ActionHelper;
import br.com.oncast.ontrack.shared.model.annotation.Annotation;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.user.UserRepresentation;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

import org.simpleframework.xml.Element;

@ConvertTo(AnnotationVoteRemoveActionEntity.class)
public class AnnotationVoteRemoveAction implements AnnotationAction {

	private static final long serialVersionUID = 1L;

	@Element
	private UUID annotationId;

	@Element
	private UUID annotatedObjectId;

	protected AnnotationVoteRemoveAction() {}

	public AnnotationVoteRemoveAction(final UUID annotationId, final UUID annotatedObjectId) {
		this.annotationId = annotationId;
		this.annotatedObjectId = annotatedObjectId;
	}

	@Override
	public ModelAction execute(final ProjectContext context, final ActionContext actionContext) throws UnableToCompleteActionException {
		final Annotation annotation = ActionHelper.findAnnotation(annotatedObjectId, annotationId, context, this);
		final UserRepresentation user = ActionHelper.findUser(actionContext.getUserId(), context, this);
		if (annotation.isDeprecated()) throw new UnableToCompleteActionException(this, ActionExecutionErrorMessageCode.VOTE_REMOVE_FROM_DEPRECATED_ANNOTATION);

		if (!annotation.hasVoted(user)) throw new UnableToCompleteActionException(this, ActionExecutionErrorMessageCode.REMOVE_UNGIVEN_VOTE);
		annotation.removeVote(user);
		return new AnnotationVoteAction(annotationId, annotatedObjectId);
	}

	@Override
	public UUID getReferenceId() {
		return annotatedObjectId;
	}

}
