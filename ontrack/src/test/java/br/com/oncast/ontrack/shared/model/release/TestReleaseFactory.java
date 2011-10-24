package br.com.oncast.ontrack.shared.model.release;

import br.com.oncast.ontrack.shared.model.uuid.UUID;

public class TestReleaseFactory {

	public static Release create(final String description) {
		return new Release(description, new UUID());
	}

}