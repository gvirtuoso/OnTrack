package br.com.oncast.ontrack.server.util.converter;

import br.com.oncast.ontrack.server.util.converter.mocks.ModelActionEntityMock;
import br.com.oncast.ontrack.server.util.converter.mocks.ModelActionEntityMockWithListOfActions;
import br.com.oncast.ontrack.server.util.converter.mocks.ModelActionMock;
import br.com.oncast.ontrack.server.util.converter.mocks.ModelActionMockWithListOfActions;
import br.com.oncast.ontrack.server.utils.typeConverter.GeneralTypeConverter;
import br.com.oncast.ontrack.server.utils.typeConverter.exceptions.TypeConverterException;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TypeConverterTest {

	/**
	 * Simple attribute types are String and primitive types.
	 */
	@Test
	public void shouldMapAClassWithSimpleAttributeTypes() throws Exception {
		final ModelActionMock modelAction = new ModelActionMock();
		final ModelActionEntityMock entity = (ModelActionEntityMock) new GeneralTypeConverter().convert(modelAction);

		assertEquals(modelAction.getAString(), entity.getAString());
	}

	@Test
	public void shouldConvertAnInteger() throws TypeConverterException {
		final int original = 1;
		final Object converted = new GeneralTypeConverter().convert(original);

		assertEquals(original, converted);
	}

	@Test
	public void shouldConvertAnUUID() throws TypeConverterException {
		final UUID original = new UUID();
		final Object converted = new GeneralTypeConverter().convert(original);

		assertEquals(original.toString(), converted);
	}

	@Test
	public void shouldConvertAnBoolean() throws TypeConverterException {
		final boolean original = true;
		final Object converted = new GeneralTypeConverter().convert(original);

		assertEquals(original, converted);
	}

	@Test
	public void shouldMapAClassWithAListOfActions() throws Exception {
		final ModelActionMockWithListOfActions modelAction = new ModelActionMockWithListOfActions();
		modelAction.addAction(new ModelActionMockWithListOfActions());
		modelAction.addAction(new ModelActionMockWithListOfActions());
		modelAction.addAction(new ModelActionMockWithListOfActions());

		final ModelActionEntityMockWithListOfActions entity = (ModelActionEntityMockWithListOfActions) new GeneralTypeConverter().convert(modelAction);

		assertEquals(modelAction.getAnActionList().size(), entity.getAnActionList().size());

		assertEquals(modelAction.getAnActionList().get(0).getAString(), entity.getAnActionList().get(0).getAString());
		assertEquals(modelAction.getAnActionList().get(1).getAString(), entity.getAnActionList().get(1).getAString());
		assertEquals(modelAction.getAnActionList().get(2).getAString(), entity.getAnActionList().get(2).getAString());
	}

}
