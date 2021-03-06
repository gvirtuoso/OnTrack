package br.com.oncast.ontrack.client.ui.generalwidgets;

import com.google.gwt.user.client.ui.IsWidget;

public interface ModelWidget<T> extends IsWidget {

	/**
	 * @return if this widget was updated.
	 */
	boolean update();

	T getModelObject();
}
