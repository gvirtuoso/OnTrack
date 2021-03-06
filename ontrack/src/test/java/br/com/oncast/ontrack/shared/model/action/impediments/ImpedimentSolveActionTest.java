package br.com.oncast.ontrack.shared.model.action.impediments;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.impediments.ImpedimentSolveActionEntity;
import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.model.ModelActionEntity;
import br.com.oncast.ontrack.shared.model.action.ImpedimentSolveAction;
import br.com.oncast.ontrack.shared.model.action.ModelAction;
import br.com.oncast.ontrack.shared.model.action.ModelActionTest;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.annotation.Annotation;
import br.com.oncast.ontrack.shared.model.annotation.AnnotationType;
import br.com.oncast.ontrack.shared.model.annotation.DeprecationState;
import br.com.oncast.ontrack.shared.model.user.UserRepresentation;
import br.com.oncast.ontrack.shared.model.uuid.UUID;
import br.com.oncast.ontrack.utils.mocks.models.UserRepresentationTestUtils;
import br.com.oncast.ontrack.utils.model.AnnotationTestUtils;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.when;

public class ImpedimentSolveActionTest extends ModelActionTest {

	private UUID subjectId;
	private UserRepresentation user;
	private Annotation annotation;
	private Date timestamp;

	@Before
	public void setup() throws Exception {
		subjectId = new UUID();
		user = UserRepresentationTestUtils.createUser();

		annotation = AnnotationTestUtils.create(user);
		timestamp = new Date();
		when(context.findAnnotation(subjectId, annotation.getId())).thenReturn(annotation);

		when(actionContext.getUserId()).thenReturn(user.getId());
		when(actionContext.getTimestamp()).thenReturn(new Date());
		when(context.findUser(user.getId())).thenReturn(user);
	}

	@Test
	public void shouldSetTheAnnotationWithSolvedImpedimentStatus() throws Exception {
		annotation.setType(AnnotationType.OPEN_IMPEDIMENT, user, timestamp);
		executeAction();

		assertEquals(AnnotationType.SOLVED_IMPEDIMENT, annotation.getType());
	}

	@Test(expected = UnableToCompleteActionException.class)
	public void shouldNotBeAbleToSolveAnAnnotationWithSimpleType() throws Exception {
		annotation.setType(AnnotationType.SIMPLE, user, new Date());

		executeAction();
	}

	@Test(expected = UnableToCompleteActionException.class)
	public void shouldNotBeAbleToSolveAnAnnotationAlreadySolved() throws Exception {
		annotation.setType(AnnotationType.SOLVED_IMPEDIMENT, user, new Date());

		executeAction();
	}

	@Test(expected = UnableToCompleteActionException.class)
	public void shouldNotBeAbleToSolveImpedimentFromDeprecatedAnnotations() throws Exception {
		annotation.setDeprecation(DeprecationState.DEPRECATED, user, new Date());

		executeAction();
	}

	@Override
	protected ModelAction getNewInstance() {
		return new ImpedimentSolveAction(subjectId, annotation.getId());
	}

	@Override
	protected Class<? extends ModelAction> getActionType() {
		return ImpedimentSolveAction.class;
	}

	@Override
	protected Class<? extends ModelActionEntity> getEntityType() {
		return ImpedimentSolveActionEntity.class;
	}

}
