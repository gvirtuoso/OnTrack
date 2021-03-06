package br.com.oncast.ontrack.client.ui.generalwidgets.scope;

import br.com.oncast.ontrack.client.services.ClientServices;
import br.com.oncast.ontrack.client.ui.components.members.DraggableMemberWidget;
import br.com.oncast.ontrack.client.ui.generalwidgets.AnimatedContainer;
import br.com.oncast.ontrack.client.ui.generalwidgets.ModelWidgetContainer;
import br.com.oncast.ontrack.client.ui.generalwidgets.ModelWidgetFactory;
import br.com.oncast.ontrack.client.ui.generalwidgets.animation.AnimationFactory;
import br.com.oncast.ontrack.client.ui.generalwidgets.animation.HideAnimation;
import br.com.oncast.ontrack.client.ui.generalwidgets.animation.ShowAnimation;
import br.com.oncast.ontrack.client.ui.generalwidgets.animation.SlideAndFadeAnimation;
import br.com.oncast.ontrack.client.ui.generalwidgets.dnd.DragAndDropManager;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.user.UserRepresentation;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ScopeAssociatedMembersWidget extends Composite {

	private static final int USER_WIDGET_WIDTH = 34;

	private static final int VISIBLE_USERS_COUNT = 5;

	private static ScopeAssociatedMembersWidgetUiBinder uiBinder = GWT.create(ScopeAssociatedMembersWidgetUiBinder.class);

	interface ScopeAssociatedMembersWidgetUiBinder extends UiBinder<Widget, ScopeAssociatedMembersWidget> {}

	interface ScopeAssociatedMembersWidgetStyle extends CssResource {
		String usersContainerOverflow();

		String associatedUsersOverflow();
	}

	@UiField
	ScopeAssociatedMembersWidgetStyle style;

	@UiField(provided = true)
	ModelWidgetContainer<UserRepresentation, DraggableMemberWidget> associatedUsers;

	private FlowPanel associatedUsersContainer;

	@UiField
	HTMLPanel usersContainer;

	@UiField
	Label hiddenAssociatedUsersIndicator;

	private int visibleUsersCount = VISIBLE_USERS_COUNT;

	private Scope scope;

	private boolean shouldShowDone;

	public ScopeAssociatedMembersWidget(final Scope scope, final DragAndDropManager userDragAndDropMananger) {
		this(scope, userDragAndDropMananger, VISIBLE_USERS_COUNT);
	}

	public ScopeAssociatedMembersWidget(final Scope scope, final DragAndDropManager userDragAndDropMananger, final int maxVisibleUsers) {
		this(scope, userDragAndDropMananger, maxVisibleUsers, !scope.getProgress().isDone());
	}

	public ScopeAssociatedMembersWidget(final Scope scope, final DragAndDropManager userDragAndDropMananger, final int maxVisibleUsers,
			final boolean shouldShowDone) {
		this.scope = scope;
		this.visibleUsersCount = maxVisibleUsers;
		this.shouldShowDone = shouldShowDone;
		associatedUsers = createAssociatedUsersListWidget(userDragAndDropMananger);

		initWidget(uiBinder.createAndBindUi(this));

		update();
	}

	@UiHandler("hiddenAssociatedUsersIndicator")
	public void onMouseOver(final MouseOverEvent event) {
		setUserListVisible(true);
	}

	@UiHandler("hiddenAssociatedUsersIndicator")
	public void onMouseOut(final MouseOutEvent event) {
		setUserListVisible(false);
	}

	public void setShouldShowDone(final boolean shouldShow) {
		this.shouldShowDone = shouldShow;
	}

	public void update() {
		if (!shouldShowDone) {
			this.setVisible(false);
			return;
		}

		final List<UserRepresentation> associatedUsersList = ClientServices.get().userAssociation().getAssociatedUsers(scope);
		associatedUsers.update(associatedUsersList);
		final int userCount = associatedUsersList.size();
		this.setVisible(userCount > 0);
		hiddenAssociatedUsersIndicator.setVisible(userCount > visibleUsersCount);
		hiddenAssociatedUsersIndicator.setText("+\n" + (userCount - visibleUsersCount));
		associatedUsersContainer.setWidth(Math.min(userCount, visibleUsersCount) * USER_WIDGET_WIDTH + "px");
	}

	public void add(final DraggableMemberWidget memberWidget) {
		memberWidget.setSizeSmall();
		associatedUsers.getContainningPanel().add(memberWidget);
	}

	private void setUserListVisible(final boolean isOver) {
		usersContainer.setStyleName(style.usersContainerOverflow(), isOver);
		associatedUsers.setStyleName(style.associatedUsersOverflow(), isOver);
	}

	private ModelWidgetContainer<UserRepresentation, DraggableMemberWidget> createAssociatedUsersListWidget(final DragAndDropManager userDragAndDropMananger) {
		associatedUsersContainer = new FlowPanel();
		return new ModelWidgetContainer<UserRepresentation, DraggableMemberWidget>(new ModelWidgetFactory<UserRepresentation, DraggableMemberWidget>() {
			@Override
			public DraggableMemberWidget createWidget(final UserRepresentation modelBean) {
				final DraggableMemberWidget widget = new DraggableMemberWidget(modelBean, scope);
				widget.setSizeSmall();
				if (userDragAndDropMananger != null) userDragAndDropMananger.monitorNewDraggableItem(widget, widget.getDraggableItem());
				return widget;
			}
		}, new AnimatedContainer(associatedUsersContainer, new AnimationFactory() {

			@Override
			public ShowAnimation createShowAnimation(final Widget widget) {
				return new SlideAndFadeAnimation(widget, true);
			}

			@Override
			public HideAnimation createHideAnimation(final Widget widget) {
				return new SlideAndFadeAnimation(widget, true);
			}
		}));

	}

	public int getWidgetCount() {
		return associatedUsers.getWidgetCount();
	}

	public void setScope(final Scope scope) {
		this.scope = scope;
	}

}
