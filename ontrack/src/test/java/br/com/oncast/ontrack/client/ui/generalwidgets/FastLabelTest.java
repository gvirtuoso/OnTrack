package br.com.oncast.ontrack.client.ui.generalwidgets;

import org.junit.Test;

import com.googlecode.gwt.test.GwtModule;
import com.googlecode.gwt.test.GwtTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@GwtModule("br.com.oncast.ontrack.Application")
public class FastLabelTest extends GwtTest {
	@Test
	public void shouldReturnEmptyStringIfTextNeverSet() {
		final FastLabel l = new FastLabel();
		assertEquals("", l.getText());
	}

	@Test
	public void shouldReturnSameSetStringToText() throws Exception {
		final FastLabel l = new FastLabel();
		l.setText("test text");
		assertEquals("test text", l.getText());
	}

	@Test
	public void shouldAllowResetingTheText() throws Exception {
		final FastLabel l = new FastLabel();
		l.setText("previous test text");
		l.setText("test text");
		assertEquals("test text", l.getText());
	}

	@Test
	public void shouldReturnEmptyStringIfTitleNeverSet() {
		final FastLabel l = new FastLabel();
		assertEquals("", l.getTitle());
	}

	@Test
	public void shouldReturnSameSetStringToTitle() throws Exception {
		final FastLabel l = new FastLabel();
		l.setTitle("test title");
		assertEquals("test title", l.getTitle());
	}

	@Test
	public void shouldAllowResetingTheTitle() throws Exception {
		final FastLabel l = new FastLabel();
		l.setText("previous test title");
		l.setText("test title");
		assertEquals("test title", l.getText());
	}

	@Test
	public void shouldAllowAddingStyleName() throws Exception {
		final FastLabel l = new FastLabel();
		l.addStyleName("style");
		assertTrue(l.hasStyleName("style"));
	}

	@Test
	public void shouldNotForgetAStyleNameAdded() throws Exception {
		final FastLabel l = new FastLabel();
		l.addStyleName("style");
		l.addStyleName("style1");
		l.addStyleName("style2");
		l.addStyleName("style3");
		l.addStyleName("style4");

		assertTrue(l.hasStyleName("style"));
		assertTrue(l.hasStyleName("style1"));
		assertTrue(l.hasStyleName("style2"));
		assertTrue(l.hasStyleName("style3"));
		assertTrue(l.hasStyleName("style4"));
	}

	@Test
	public void shouldAllowForgetRemovedStyleNames() throws Exception {
		final FastLabel l = new FastLabel();
		l.addStyleName("style");
		l.addStyleName("style1");
		l.addStyleName("style2");
		l.addStyleName("style3");
		l.addStyleName("style4");

		l.removeStyleName("style");

		assertFalse(l.hasStyleName("style"));
		assertTrue(l.hasStyleName("style1"));
		assertTrue(l.hasStyleName("style2"));
		assertTrue(l.hasStyleName("style3"));
		assertTrue(l.hasStyleName("style4"));
	}

	@Test
	public void shouldDelegateStyleChangeToSuperClass() throws Exception {
		final FastLabel l = new FastLabel();
		l.addStyleName("style");
		l.addStyleName("style1");
		l.addStyleName("style2");
		l.addStyleName("style3");
		l.addStyleName("style4");

		assertEquals("gwt-Label style style1 style2 style3 style4", l.getStyleName());
	}

	@Test
	public void shouldDelegateTextChangeToSuperClass() throws Exception {
		final FastLabel l = new FastLabel();
		l.setText("test text");
		assertEquals("test text", l.getElement().getInnerText());
	}
}
