package br.com.oncast.ontrack.shared.model.color;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;

public class Color implements Serializable {

	private static final String HASH = "#";

	private static final long serialVersionUID = 1L;

	private static final List<Color> PRESET_COLORS = new ArrayList<Color>();

	private static final double DEFAULT_ALPHA = 1.0;

	public static final Color TRANSPARENT = new Color(255, 255, 255, 0);
	public static final Color GREEN = new Color(212, 250, 22);
	public static final Color YELLOW = new Color(250, 231, 22);
	public static final Color BLUE = new Color(154, 203, 230);
	public static final Color GRAY = new Color(200, 200, 180);
	public static final Color RED = new Color(240, 84, 45);
	public static final Color INFO = new Color(250, 250, 250);
	public static final Color ORANGE_LIGHT = new Color(191, 179, 72);
	public static final Color DARK_GREEN = new Color(20, 175, 113);
	public static final Color WHITE = new Color(255, 255, 255);

	static {
		PRESET_COLORS.add(new Color("#FF4D50"));
		PRESET_COLORS.add(new Color("#00DD00"));
		PRESET_COLORS.add(new Color("#9970FF"));
		PRESET_COLORS.add(new Color("#FFAACC"));
		PRESET_COLORS.add(new Color("#EE7F2B"));
		PRESET_COLORS.add(new Color("#07D2FF"));
		PRESET_COLORS.add(new Color("#227FFF"));
		PRESET_COLORS.add(new Color("#AAFA55"));
		PRESET_COLORS.add(new Color("#FFC14B"));
		PRESET_COLORS.add(new Color("#A8C102"));
		PRESET_COLORS.add(new Color("#9C7835"));
		PRESET_COLORS.add(new Color("#BB8F73"));
		PRESET_COLORS.add(new Color("#AACCFF"));
		PRESET_COLORS.add(new Color("#CCF56F"));
		PRESET_COLORS.add(new Color("#CC6FF5"));
		PRESET_COLORS.add(new Color("#FA55AA"));
	}

	@Attribute
	private int r;

	@Attribute
	private int g;

	@Attribute
	private int b;

	@Attribute
	private double a = -DEFAULT_ALPHA;

	protected Color() {}

	public Color(final String hexColor) {
		if (hexColor.length() != 7 || hexColor.indexOf(HASH) != 0) throw new IllegalArgumentException("hexColor should be CCS Color style (Eg. #aabbcc)");

		r = Integer.parseInt(hexColor.substring(1, 3), 16);
		g = Integer.parseInt(hexColor.substring(3, 5), 16);
		b = Integer.parseInt(hexColor.substring(5, 7), 16);

		a = DEFAULT_ALPHA;
	}

	public Color(final int r, final int g, final int b) {
		this(r, g, b, DEFAULT_ALPHA);
	}

	public Color(final int r, final int g, final int b, final double a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	public String toHex() {
		String hex = HASH;
		hex += toTwoDigitHex(r);
		hex += toTwoDigitHex(g);
		hex += toTwoDigitHex(b);
		return hex;
	}

	private String toTwoDigitHex(final int x) {
		final String hexString = Integer.toHexString(x);
		return hexString.length() <= 1 ? "0" + hexString : hexString;
	}

	public int getRed() {
		return r;
	}

	public int getGreen() {
		return g;
	}

	public int getBlue() {
		return b;
	}

	public double getAlpha() {
		return a;
	}

	public Color setAlpha(final double a) {
		this.a = a;
		return this;
	}

	public Color setRed(final int r) {
		this.r = r;
		return this;
	}

	public Color setGreen(final int g) {
		this.g = g;
		return this;
	}

	public Color setBlue(final int b) {
		this.b = b;
		return this;
	}

	public String toCssRepresentation() {
		if (a < 0) return "rgb(" + r + ", " + g + ", " + b + ")";
		if (a == 0) return "transparent";
		return "rgba(" + r + ", " + g + ", " + b + ", " + a + ")";
	}

	public Color copy() {
		return new Color(r, g, b, a);
	}

	public static List<Color> getPresetColors() {
		return PRESET_COLORS;
	}

	@Override
	public String toString() {
		return "Color(" + r + ", " + g + ", " + b + ", " + a + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = (long) a;
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + b;
		result = prime * result + g;
		result = prime * result + r;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final Color other = (Color) obj;
		if (a != a) return false;
		if (b != other.b) return false;
		if (g != other.g) return false;
		if (r != other.r) return false;
		return true;
	}

}
