package br.com.oncast.ontrack.client.services.user;

import br.com.oncast.ontrack.shared.model.color.Color;
import br.com.oncast.ontrack.shared.model.color.ColorPack;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.user.UserRepresentation;

import java.util.List;

public interface ColorProviderService {

	Color getSelectionColorFor(UserRepresentation user);

	Color getColorFor(Scope scope);

	List<Selection> getMembersSelectionsFor(Scope scope);

	Color pickColor();

	ColorPack pickColorPack();

	Color getDueDateColor(Scope scope);

}
