package br.com.oncast.ontrack.client.utils;

public class RuntimeEnvironment {
	private static Boolean isMac;

	public static boolean isMac() {
		if (isMac != null) return isMac;
		isMac = evaluateWhetherIsMac();
		return isMac;
	}

	private static native boolean evaluateWhetherIsMac() /*-{
		if (!navigator.platform)
			return false;
		return navigator.platform.indexOf('Mac') != -1;
	}-*/;
}
