package br.com.oncast.ontrack.utils.mmConverter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.oncast.ontrack.mocks.models.ScopeMock;
import br.com.oncast.ontrack.server.utils.mmConverter.FreeMindExporter;
import br.com.oncast.ontrack.server.utils.mmConverter.abstractions.FreeMindMap;
import br.com.oncast.ontrack.server.utils.mmConverter.abstractions.Icon;
import br.com.oncast.ontrack.server.utils.mmConverter.abstractions.MindNode;
import br.com.oncast.ontrack.shared.model.project.Project;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.scope.inference.EffortInferenceEngine;

public class FreeMindExporterTest {
	private static final File PROJECT_MM_FILE = new File("src/test/java/br/com/oncast/ontrack/utils/mmConverter/ProjetoTeste.mm");
	private Scope scope;

	@Before
	public void setUp() {
		scope = ScopeMock.getScope();
	}

	@After
	public void tearDown() {
		if (PROJECT_MM_FILE.exists()) PROJECT_MM_FILE.delete();
	}

	/**
	 * The root scope is the project of the hierarchy.
	 */
	@Test
	public void projectNameShouldBeTheRootNodeDescription() throws FileNotFoundException {
		final FreeMindMap exportedMindMap = exportToMindMap(scope);

		assertEquals(scope.getDescription(), exportedMindMap.root().getText());
	}

	@Test
	public void legendShouldBeTheFirstChildOfProject() throws FileNotFoundException {
		final FreeMindMap exportedMindMap = exportToMindMap(scope);

		final MindNode legendNode = exportedMindMap.root().getChildren().get(0);
		assertEquals("Legenda", legendNode.getText());
		assertTrue(legendNode.hasIcon(Icon.INFO));
	}

	@Test
	public void legendShouldContainInformationAboutIcons() throws FileNotFoundException {
		final FreeMindMap exportedMindMap = exportToMindMap(scope);

		final MindNode legendNode = exportedMindMap.root().getChildren().get(0);
		assertEquals("Associação com entrega", legendNode.getChildren().get(0).getText());
		assertTrue(legendNode.getChildren().get(0).hasIcon(Icon.CALENDAR));

		assertEquals("Declaração de esforço", legendNode.getChildren().get(1).getText());
		assertTrue(legendNode.getChildren().get(1).hasIcon(Icon.LAUNCH));

		assertEquals("Inferência de esforço", legendNode.getChildren().get(2).getText());
		assertTrue(legendNode.getChildren().get(2).hasIcon(Icon.LAUNCH));
		assertTrue(legendNode.getChildren().get(2).hasIcon(Icon.WIZARD));

		assertEquals("Declaração de progresso", legendNode.getChildren().get(3).getText());
		assertTrue(legendNode.getChildren().get(3).hasIcon(Icon.HOURGLASS));
	}

	@Test
	public void scopeHierarchyShouldBePlacedInsideANodeForThem() throws FileNotFoundException {
		final FreeMindMap exportedMindMap = exportToMindMap(scope);

		final MindNode scopeHierarchyContainer = exportedMindMap.root().getChildren().get(1);
		assertEquals("Árvore de escopo", scopeHierarchyContainer.getText());
		assertTrue(scopeHierarchyContainer.hasIcon(Icon.LIST));
	}

	@Test
	public void shouldExportEntireScopeHierarchy() throws FileNotFoundException {
		final FreeMindMap exportedMap = exportToMindMap(scope);

		final MindNode scopeHierarchyContainer = exportedMap.root().getChildren().get(1);
		assertEqualityOfEntireHierarchy(scope, scopeHierarchyContainer);
	}

	@Test
	public void exportedMapShouldRepresentEffort() throws FileNotFoundException {
		scope.getEffort().setDeclared(100);
		new EffortInferenceEngine().process(scope);

		final FreeMindMap exportedMap = exportToMindMap(scope);
		final MindNode scopeHierarchyContainerNode = exportedMap.root().getChildren().get(1);

		final MindNode effort100 = scopeHierarchyContainerNode.getChildren().get(0);
		assertEquals("100", effort100.getText());
		assertTrue(effort100.hasIcon(Icon.LAUNCH));

		final MindNode effort33 = scopeHierarchyContainerNode.getChildren().get(1).getChildren().get(0);
		assertEquals("33.333", effort33.getText());
		assertTrue(effort33.hasIcon(Icon.LAUNCH));
		assertTrue(effort33.hasIcon(Icon.WIZARD));

		final MindNode effort16 = scopeHierarchyContainerNode.getChildren().get(1).getChildren().get(1).getChildren().get(0);
		assertEquals("16.666", effort16.getText());
		assertTrue(effort16.hasIcon(Icon.LAUNCH));
		assertTrue(effort16.hasIcon(Icon.WIZARD));

		final MindNode effort8 = scopeHierarchyContainerNode.getChildren().get(1).getChildren().get(1).getChildren().get(1).getChildren().get(0);
		assertEquals("8.333", effort8.getText());
		assertTrue(effort8.hasIcon(Icon.LAUNCH));
		assertTrue(effort8.hasIcon(Icon.WIZARD));
	}

	@Test
	public void exportedMapShouldRepresentProgress() throws FileNotFoundException {
		scope.getChild(0).getChild(0).getProgress().setDescription("Under work");

		final FreeMindMap exportedMap = exportToMindMap(scope);
		final MindNode progressNode = exportedMap.root().getChildren().get(1).getChildren().get(0).getChildren().get(0).getChildren().get(0);

		assertEquals("Under work", progressNode.getText());
		assertTrue(progressNode.hasIcon(Icon.HOURGLASS));
	}

	/**
	 * Assert equality of scope description and a mind map node text of an entire hierarchy, regardless the root scope, because of the difference between the
	 * scope and mind map hierarchy.<br>
	 * The root scope must be the root node of the map, and below it must be a scope hierarchy container.
	 */
	private void assertEqualityOfEntireHierarchy(final Scope expectedScope, final MindNode scopeHierarchyContainer) {
		for (int i = 0; i < expectedScope.getChildCount(); i++) {
			final Scope childScope = expectedScope.getChild(i);
			final MindNode childNode = scopeHierarchyContainer.getChildren().get(i);

			assertEquals(childScope.getDescription(), childNode.getText());
			assertEqualityOfEntireHierarchy(childScope, childNode);
		}
	}

	private FreeMindMap exportToMindMap(final Scope scopeToBeExported) throws FileNotFoundException {
		return FreeMindExporter.export(new Project(scopeToBeExported, null), new FileOutputStream(PROJECT_MM_FILE));
	}
}
