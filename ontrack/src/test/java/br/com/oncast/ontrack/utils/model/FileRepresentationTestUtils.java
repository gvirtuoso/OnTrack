package br.com.oncast.ontrack.utils.model;

import br.com.oncast.ontrack.shared.model.file.FileRepresentation;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

public class FileRepresentationTestUtils {

	private static final Long DEFAULT_PROJECT = 1L;
	private static int fileRepresentationCounter = 0;

	public static FileRepresentation create() {
		final String fileName = "file_" + ++fileRepresentationCounter + ".extension";
		return new FileRepresentation(new UUID(), fileName, "path/to/" + fileName, new UUID(String.valueOf(DEFAULT_PROJECT)));
	}

}
