package br.com.oncast.ontrack.shared.model.action;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.scope.ScopeMoveRightActionEntity;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConversionAlias;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConvertTo;
import br.com.oncast.ontrack.shared.exceptions.ActionExecutionErrorMessageCode;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.action.helper.ActionHelper;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

@ConvertTo(ScopeMoveRightActionEntity.class)
public class ScopeMoveRightAction implements ScopeMoveAction {

	private static final long serialVersionUID = 1L;

	@ConversionAlias("referenceId")
	@Element
	private UUID referenceId;

	@ConversionAlias("position")
	@Attribute
	private int position;

	@ConversionAlias("wasIndexSet")
	@Attribute
	private boolean wasIndexSet;

	@ConversionAlias("subActionList")
	@ElementList
	private List<ModelAction> subActionList;

	public ScopeMoveRightAction(final UUID selectedScopeId) {
		this(selectedScopeId, -1, new ArrayList<ModelAction>());
		this.wasIndexSet = false;
	}

	// TODO Analyze the possibility of replacing the sub-action list for a single typed action.
	public ScopeMoveRightAction(final UUID selectedScopeId, final int position, final List<ModelAction> subActionList) {
		this.referenceId = selectedScopeId;
		this.position = position;
		this.subActionList = subActionList;
		this.wasIndexSet = true;
	}

	// IMPORTANT A package-visible default constructor is necessary for serialization. Do not remove this.
	protected ScopeMoveRightAction() {}

	@Override
	public ModelAction execute(final ProjectContext context, final ActionContext actionContext) throws UnableToCompleteActionException {
		final Scope selectedScope = ActionHelper.findScope(referenceId, context, this);
		if (selectedScope.isRoot()) throw new UnableToCompleteActionException(this, ActionExecutionErrorMessageCode.MOVE_ROOT_NODE);

		final Scope parent = selectedScope.getParent();
		if (isFirstNode(parent.getChildIndex(selectedScope))) throw new UnableToCompleteActionException(this, ActionExecutionErrorMessageCode.MOVE_RIGHT_FIRST_NODE);

		final List<ModelAction> subActionRollbackList = new ArrayList<ModelAction>();

		final Scope upperSibling = parent.getChildren().get(parent.getChildIndex(selectedScope) - 1);
		if (upperSibling.isLeaf()) subActionRollbackList.add(removeDeclaredProgress(upperSibling, context, actionContext));

		selectedScope.getParent().remove(selectedScope);
		if (wasIndexSet) upperSibling.add(position, selectedScope);
		else upperSibling.add(selectedScope);

		return new ScopeMoveLeftAction(referenceId, subActionRollbackList);
	}

	private ModelAction removeDeclaredProgress(final Scope scope, final ProjectContext context, final ActionContext actionContext) throws UnableToCompleteActionException {
		final ScopeDeclareProgressAction removeProgressAction = new ScopeDeclareProgressAction(scope.getId(), "");
		subActionList.add(removeProgressAction);

		return removeProgressAction.execute(context, actionContext);
	}

	private boolean isFirstNode(final int index) {
		return index == 0;
	}

	@Override
	public UUID getReferenceId() {
		return referenceId;
	}

	@Override
	public boolean changesEffortInference() {
		return true;
	}

	@Override
	public boolean changesValueInference() {
		return true;
	}

	@Override
	public boolean changesProgressInference() {
		return true;
	}
}