package br.com.oncast.ontrack.client.ui.components.report;

import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.scope.Scope;

import java.util.List;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;

public class ScopeDatabase {

	public static class ScopeItem implements Comparable<ScopeItem> {

		public static final ProvidesKey<ScopeItem> KEY_PROVIDER = new ProvidesKey<ScopeItem>() {
			@Override
			public Object getKey(final ScopeItem item) {
				return item == null ? null : item.getPriority();
			}
		};

		private final int priority;

		private final Scope scope;

		private final ProjectContext context;

		public ScopeItem(final ProjectContext context, final Scope scope, final int priority) {
			this.context = context;
			this.scope = scope;
			this.priority = priority;
		}

		@Override
		public int compareTo(final ScopeItem o) {
			return this.getPriority() - o.getPriority();
		}

		protected int getPriority() {
			return priority;
		}

		public String getDescription() {
			return scope.getDescription();
		}

		public Float getEffort() {
			return scope.getEffort().getInfered();
		}

		public Float getValue() {
			return scope.getValue().getInfered();
		}

		public Float getProgress() {
			return scope.getEffort().getAccomplishedPercentual();
		}

		public Long getCycleTime() {
			return scope.getProgress().getCycleTime();
		}

		public Long getLeadTime() {
			return scope.getProgress().getLeadTime();
		}

		public String getHumandReadableId() {
			return context.getHumanId(scope);
		}
	}

	private final ListDataProvider<ScopeItem> dataProvider = new ListDataProvider<ScopeItem>();

	public ScopeDatabase(final List<Scope> scopeList, final ProjectContext context) {
		final List<ScopeItem> list = dataProvider.getList();
		int i = 0;
		for (final Scope scope : scopeList) {
			list.add(new ScopeItem(context, scope, i++));
		}
	}

	public ListDataProvider<ScopeItem> getDataProvider() {
		return dataProvider;
	}

	public void addDataDisplay(final CellTable<ScopeItem> display) {
		dataProvider.addDataDisplay(display);
	}

	public void refreshDisplays() {
		dataProvider.refresh();
	}
}
