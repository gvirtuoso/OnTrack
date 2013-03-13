package br.com.oncast.ontrack.server.services.metrics;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import br.com.oncast.ontrack.server.business.BusinessLogic;
import br.com.oncast.ontrack.server.model.project.UserAction;
import br.com.oncast.ontrack.server.services.multicast.ClientManager;
import br.com.oncast.ontrack.server.services.persistence.PersistenceService;
import br.com.oncast.ontrack.server.services.persistence.exceptions.PersistenceException;
import br.com.oncast.ontrack.server.services.serverPush.ServerPushConnection;
import br.com.oncast.ontrack.shared.model.project.Project;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.release.ReleaseEstimator;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.uuid.UUID;
import br.com.oncast.ontrack.shared.services.metrics.OnTrackStatisticsFactory;
import br.com.oncast.ontrack.shared.services.metrics.ProjectMetrics;
import br.com.oncast.ontrack.shared.utils.WorkingDay;

import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public class ServerMetricsService {

	private static final OnTrackStatisticsFactory FACTORY = AutoBeanFactorySource.create(OnTrackStatisticsFactory.class);
	private static final Logger LOGGER = Logger.getLogger(ServerMetricsService.class);

	private final PersistenceService persistenceService;
	private final ClientManager clientManager;
	private final BusinessLogic businessLogic;

	public ServerMetricsService(final PersistenceService persistenceService, final ClientManager clientManager, final BusinessLogic businessLogic) {
		this.persistenceService = persistenceService;
		this.clientManager = clientManager;
		this.businessLogic = businessLogic;
	}

	public long getActionsCountSince(final Date date) {
		try {
			return persistenceService.countActionsSince(date);
		}
		catch (final PersistenceException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getLocalizedMessage());
		}
	}

	public Map<String, Integer> getActionsRatio(final Date date) {
		try {
			final Map<String, Integer> map = new HashMap<String, Integer>();
			final List<UserAction> actions = persistenceService.retrieveActionsSince(date);
			for (final UserAction userAction : actions) {
				final String name = userAction.getModelAction().getClass().getSimpleName();
				int count = map.containsKey(name) ? map.get(name) : 0;
				map.put(name, ++count);
			}

			return map;
		}
		catch (final PersistenceException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getLocalizedMessage());
		}
	}

	public int getUsersCount() {
		try {
			return persistenceService.retrieveAllUsers().size();
		}
		catch (final PersistenceException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getLocalizedMessage());
		}
	}

	public int getProjectsCount() {
		try {
			return persistenceService.retrieveAllProjectRepresentations().size();
		}
		catch (final PersistenceException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getLocalizedMessage());
		}
	}

	public List<ProjectMetrics> getActiveProjectsMetrics() {
		final Set<UUID> activeProjects = new HashSet<UUID>();
		final Set<ServerPushConnection> clients = clientManager.getAllClients();
		for (final ServerPushConnection client : clients) {
			final UUID currentProject = clientManager.getCurrentProject(client);
			if (currentProject.isValid()) activeProjects.add(currentProject);
		}

		final List<ProjectMetrics> metrics = new ArrayList<ProjectMetrics>();
		for (final UUID projectId : activeProjects) {
			try {
				metrics.add(getProjectMetrics(projectId));
			}
			catch (final Exception e) {
				LOGGER.error("unable to get projects metrics", e);
			}
		}
		return metrics;
	}

	public ProjectMetrics getProjectMetrics(final UUID projectId) throws Exception {
		final ProjectMetrics metrics = FACTORY.createProjectMetrics().as();
		final Project project = businessLogic.loadProject(projectId);
		metrics.setProjectName(project.getProjectRepresentation().getName());
		metrics.setUsersCount(project.getUsers().size());
		final List<Scope> scopes = project.getProjectScope().getAllDescendantScopes();
		metrics.setScopesCount(scopes.size());
		final List<Integer> scopesDepth = new ArrayList<Integer>();
		for (final Scope scope : scopes) {
			if (scope.isLeaf()) scopesDepth.add(countToRoot(scope));
		}
		metrics.setScopesDepth(scopesDepth);

		final List<Release> releases = project.getProjectRelease().getAllReleasesInTemporalOrder();
		metrics.setReleasesCount(releases.size());
		final List<Integer> releasesDuration = new ArrayList<Integer>();
		final List<Integer> releasesDepth = new ArrayList<Integer>();
		final List<Integer> releasesStoryCount = new ArrayList<Integer>();
		final ReleaseEstimator estimator = new ReleaseEstimator(project.getProjectRelease());
		for (final Release release : releases) {
			if (!release.isRoot() && release.isLeaf()) {
				final WorkingDay startDay = estimator.getEstimatedStartDayFor(release);
				final WorkingDay endDay = estimator.getEstimatedEndDayFor(release);
				releasesDuration.add(startDay.countTo(endDay));
				releasesDepth.add(countToRootRelease(release));
			}
			if (release.hasDirectScopes()) {
				releasesStoryCount.add(release.getScopeList().size());
			}
		}
		metrics.setReleasesDepth(releasesDepth);
		metrics.setReleasesDuration(releasesDuration);
		metrics.setStoriesPerRelease(releasesStoryCount);

		return metrics;
	}

	private Integer countToRoot(Scope scope) {
		int count = 0;
		while (!scope.isRoot()) {
			count++;
			scope = scope.getParent();
		}
		return count;
	}

	private Integer countToRootRelease(Release release) {
		int count = 0;
		while (!release.isRoot()) {
			count++;
			release = release.getParent();
		}
		return count;
	}
}
