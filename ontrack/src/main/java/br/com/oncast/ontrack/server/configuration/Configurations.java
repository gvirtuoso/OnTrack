package br.com.oncast.ontrack.server.configuration;

import java.io.IOException;
import java.util.Properties;

public class Configurations {

	private static Configurations instance = null;
	private final Properties properties = new Properties();

	private Configurations() {
		try {
			properties.load(Configurations.class.getResourceAsStream("/environment.properties"));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Configurations get() {
		if (instance == null) instance = new Configurations();
		return instance;
	}

	public String getEmailUsername() {
		return properties.getProperty("email.user");
	}

	public String getEmailPassword() {
		return properties.getProperty("email.password");
	}

	public String getApplicationBaseUrl() {
		return properties.getProperty("application.base_url");
	}

	public String getStorageBaseDir() {
		return properties.getProperty("storage.base_dir");
	}

	public long getMaxFileSizeInBytes() {
		final String value = properties.getProperty("storage.file_max_upload_size");
		return value == null ? -1 : Long.valueOf(value);
	}

	public String getAdminUsername() {
		return properties.getProperty("admin.user");
	}

	public String getAdminPassword() {
		return properties.getProperty("admin.password");
	}

	public String getIntegrationApiUrl() {
		return properties.getProperty("integration.base_url");
	}

	public String integrationUsername() {
		return properties.getProperty("integration.user");
	}

	public String integrationPassword() {
		return properties.getProperty("integration.password");
	}

	public String getGoogleAnalyticsTrackingId() {
		return properties.getProperty("google_analytics.tracking_id");
	}

	public Integer getAuthenticationTokenExpirationInDays() {
		return Integer.valueOf(properties.getProperty("authentication.token_expiration_time_in_days"));
	}
}
