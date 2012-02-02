package br.com.oncast.ontrack.shared.model.kanban;

import static br.com.oncast.ontrack.shared.model.kanban.KanbanTest.assertColumns;
import static br.com.oncast.ontrack.utils.mocks.models.ReleaseTestUtils.createReleaseForKanbanWithColumns;

import org.junit.Test;

import br.com.oncast.ontrack.shared.model.progress.Progress;
import br.com.oncast.ontrack.shared.model.progress.Progress.ProgressState;

public class KanbanFactoryTest {

	private static final String DONE = ProgressState.DONE.getDescription();
	private static final String NOT_STARTED = Progress.DEFAULT_NOT_STARTED_NAME;

	@Test
	public void columnsShouldBeOrderedByProgressDeclarationOrder() throws Exception {
		final Kanban kanban = KanbanFactory.createFor(createReleaseForKanbanWithColumns("Planning", "Implementing", "Testing"));
		assertColumns(kanban, NOT_STARTED, "Planning", "Implementing", "Testing", DONE);
	}

	@Test
	public void columnsShouldNotBeDuplicated() throws Exception {
		final Kanban kanban = KanbanFactory.createFor(createReleaseForKanbanWithColumns("Planning", "Implementing", "Planning", "Testing"));
		assertColumns(kanban, NOT_STARTED, "Planning", "Implementing", "Testing", DONE);
	}

	@Test
	public void mergedKanbanShouldContainTheBaseKanbansColumnsInOrder() throws Exception {
		final SimpleKanban kanban = createSimpleKanban("Planning", "Implementing", "Testing");
		final SimpleKanban newKanban = KanbanFactory.merge(kanban, createSimpleKanban());

		assertColumns(newKanban, NOT_STARTED, "Planning", "Implementing", "Testing", DONE);
	}

	@Test
	public void shouldInsertNewColumnsOnBeginningOfNewKanban() throws Exception {
		final SimpleKanban kanban = createSimpleKanban("Planning", "Implementing", "Testing");
		final SimpleKanban newKanban = KanbanFactory.merge(kanban, createSimpleKanban("New Progress1", "New Progress2"));

		assertColumns(newKanban, NOT_STARTED, "New Progress1", "New Progress2", "Planning", "Implementing", "Testing", DONE);
	}

	@Test
	public void mergedKanbanShouldNotContainRepetitionsAndKeepOrderOfBaseKanban() throws Exception {
		final SimpleKanban kanban = createSimpleKanban("Planning", "Implementing", "Testing");
		final SimpleKanban newKanban = KanbanFactory.merge(kanban, createSimpleKanban("New Progress1", "Implementing"));

		assertColumns(newKanban, NOT_STARTED, "New Progress1", "Planning", "Implementing", "Testing", DONE);
	}

	private SimpleKanban createSimpleKanban(final String... columns) {
		final SimpleKanban kanban = new SimpleKanban();
		for (final String column : columns)
			kanban.appendColumn(column);
		return kanban;
	}
}
