package br.com.oncast.ontrack.shared.model.action.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.annotation.AnnotationCreateActionEntity;
import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.model.ModelActionEntity;
import br.com.oncast.ontrack.shared.model.action.AnnotationCreateAction;
import br.com.oncast.ontrack.shared.model.action.ModelAction;
import br.com.oncast.ontrack.shared.model.action.ModelActionTest;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.annotation.Annotation;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.user.User;
import br.com.oncast.ontrack.shared.model.user.exceptions.UserNotFoundException;
import br.com.oncast.ontrack.shared.model.uuid.UUID;
import br.com.oncast.ontrack.utils.mocks.models.UserTestUtils;

public class AnnotationCreateActionTest extends ModelActionTest {

	private ProjectContext context;
	private User author;
	private UUID annotatedObjectId;
	private String message;

	@Before
	public void setUp() throws Exception {
		context = mock(ProjectContext.class);
		annotatedObjectId = new UUID();
		author = UserTestUtils.createUser();
		message = "Any message";

		when(context.findUser(author.getId())).thenReturn(author);
	}

	@Test
	public void shouldAssociateTheAnnotationWithTheAnnotatedObjectsUUID() throws Exception {
		annotatedObjectId = new UUID();
		execute();

		verify(context).addAnnotation(Mockito.any(Annotation.class), Mockito.eq(annotatedObjectId));
	}

	@Test
	public void shouldAssociateTheAnnotationWithTheUser() throws Exception {
		execute();

		final ArgumentCaptor<Annotation> captor = ArgumentCaptor.forClass(Annotation.class);
		verify(context).addAnnotation(captor.capture(), Mockito.any(UUID.class));

		assertEquals(author, captor.getValue().getAuthor());
	}

	@Test(expected = UnableToCompleteActionException.class)
	public void shouldNotCompleteWhenTheSpecifiedUserDoesNotExist() throws Exception {
		when(context.findUser(author.getId())).thenThrow(new UserNotFoundException(""));
		execute();
	}

	@Test
	public void shouldBeAbleToOverrideTheAuthor() throws Exception {
		final AnnotationCreateAction action = new AnnotationCreateAction(annotatedObjectId, author, message);
		final User expectedAuthor = UserTestUtils.createUser();
		action.setAuthor(expectedAuthor);

		when(context.findUser(expectedAuthor.getId())).thenReturn(expectedAuthor);
		action.execute(context);

		final ArgumentCaptor<Annotation> captor = ArgumentCaptor.forClass(Annotation.class);
		verify(context).addAnnotation(captor.capture(), Mockito.any(UUID.class));
		verify(context, never()).findUser(author.getId());
		verify(context).findUser(expectedAuthor.getId());

		final User obtainedAuthor = captor.getValue().getAuthor();
		assertFalse(author.equals(obtainedAuthor));
		assertEquals(expectedAuthor, obtainedAuthor);
	}

	@Test
	public void shouldHaveTheMessage() throws Exception {
		execute();

		final ArgumentCaptor<Annotation> captor = ArgumentCaptor.forClass(Annotation.class);
		verify(context).addAnnotation(captor.capture(), Mockito.any(UUID.class));

		assertEquals(message, captor.getValue().getMessage());
	}

	// FIXME Mats move this test to another class
	@Test
	public void shouldRemoveTheCreatedAnnotationOnUndo() throws Exception {
		final ModelAction undoAction = execute();

		final ArgumentCaptor<Annotation> captor = ArgumentCaptor.forClass(Annotation.class);
		verify(context).addAnnotation(captor.capture(), Mockito.any(UUID.class));
		final Annotation createdAnnotation = captor.getValue();

		when(context.findAnnotation(createdAnnotation.getId(), annotatedObjectId)).thenReturn(createdAnnotation);

		undoAction.execute(context);

		verify(context).removeAnnotation(createdAnnotation, annotatedObjectId);
	}

	private ModelAction execute() throws UnableToCompleteActionException {
		return new AnnotationCreateAction(annotatedObjectId, author, message).execute(context);
	}

	@Override
	protected Class<? extends ModelActionEntity> getEntityType() {
		return AnnotationCreateActionEntity.class;
	}

	@Override
	protected Class<? extends ModelAction> getActionType() {
		return AnnotationCreateAction.class;
	}

	@Override
	protected ModelAction getInstance() {
		return new AnnotationCreateAction(annotatedObjectId, author, message);
	}

}
