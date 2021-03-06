package br.com.oncast.ontrack.shared.model.action.scope;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.model.ModelActionEntity;
import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.scope.ScopeRemoveAssociatedUserActionEntity;
import br.com.oncast.ontrack.shared.model.action.ModelAction;
import br.com.oncast.ontrack.shared.model.action.ModelActionTest;
import br.com.oncast.ontrack.shared.model.action.ScopeAction;
import br.com.oncast.ontrack.shared.model.action.ScopeRemoveAssociatedUserAction;
import br.com.oncast.ontrack.shared.model.metadata.Metadata;
import br.com.oncast.ontrack.shared.model.metadata.MetadataFactory;
import br.com.oncast.ontrack.shared.model.metadata.UserAssociationMetadata;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.user.UserRepresentation;
import br.com.oncast.ontrack.shared.model.uuid.UUID;
import br.com.oncast.ontrack.utils.mocks.models.UserRepresentationTestUtils;
import br.com.oncast.ontrack.utils.model.ScopeTestUtils;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScopeRemoveAssociatedUserActionTest extends ModelActionTest {

	private Scope scope;
	private UUID scopeId;

	private UserRepresentation user;
	private UUID userId;

	private UserAssociationMetadata metadata;
	private List<Metadata> metadataList;

	@Before
	public void setup() throws Exception {
		scope = ScopeTestUtils.createScope();
		scopeId = scope.getId();

		user = UserRepresentationTestUtils.createUser();
		userId = user.getId();

		metadata = MetadataFactory.createUserMetadata(new UUID(), scope, user);

		when(context.findScope(scopeId)).thenReturn(scope);
		when(context.findUser(userId)).thenReturn(user);
		metadataList = new ArrayList<Metadata>();
		metadataList.add(metadata);
		when(context.getMetadataList(scope, UserAssociationMetadata.getType())).thenReturn(metadataList);
	}

	@Test
	public void doesNotChangesAnyInference() throws Exception {
		final ScopeAction action = new ScopeRemoveAssociatedUserAction(scopeId, userId);
		assertFalse(action.changesEffortInference());
		assertFalse(action.changesProgressInference());
		assertFalse(action.changesValueInference());
	}

	@Test
	public void shouldRemoveTheGivenAssociationOfTheGivenScope() throws Exception {
		executeAction();

		final Metadata value = captureRemovedMetadata();
		assertTrue(value instanceof UserAssociationMetadata);
		final UserAssociationMetadata metadata = (UserAssociationMetadata) value;

		assertEquals(scope, metadata.getSubject());
		assertEquals(userId, metadata.getUser().getId());
	}

	@Test
	public void undoShouldAddTheAssociation() throws Exception {
		final ModelAction undoAction = executeAction();
		final Metadata metadata = captureRemovedMetadata();
		metadataList.clear();

		undoAction.execute(context, actionContext);
		verify(context).addMetadata(metadata);
	}

	@Override
	protected ModelAction getNewInstance() {
		return new ScopeRemoveAssociatedUserAction(scopeId, userId);
	}

	@Override
	protected Class<? extends ModelAction> getActionType() {
		return ScopeRemoveAssociatedUserAction.class;
	}

	@Override
	protected Class<? extends ModelActionEntity> getEntityType() {
		return ScopeRemoveAssociatedUserActionEntity.class;
	}

}
