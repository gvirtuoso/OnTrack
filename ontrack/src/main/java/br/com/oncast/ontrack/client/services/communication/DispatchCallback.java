package br.com.oncast.ontrack.client.services.communication;

public interface DispatchCallback<T> {

	void onFailure(Throwable caught);

	void onRequestCompletition(T response);
}