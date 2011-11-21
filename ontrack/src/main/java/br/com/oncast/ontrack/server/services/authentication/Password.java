package br.com.oncast.ontrack.server.services.authentication;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.user.PasswordEntity;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConvertTo;

@ConvertTo(PasswordEntity.class)
public class Password {

	private long id;

	private long userId;

	private String passwordHash;

	private String passwordSalt;

	public Password() {}

	public void setId(final long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setUserId(final long userId) {
		this.userId = userId;
	}

	public long getUserId() {
		return userId;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(final String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getPasswordSalt() {
		return passwordSalt;
	}

	public void setPasswordSalt(final String passwordSalt) {
		this.passwordSalt = passwordSalt;
	}

	public void setPassword(final String password) {
		try {
			final PasswordHash hash = new PasswordHash(password);
			passwordHash = hash.getPasswordHash();
			passwordSalt = hash.getPasswordSalt();
		}
		catch (final Exception e) {
			throw new RuntimeException("Error encoding password for storage.", e);
		}
	}

	public boolean authenticate(final String password) {
		if (passwordHash == null && passwordSalt == null) {
			return password.isEmpty();
		}
		else {
			try {
				return new PasswordHash(password, passwordSalt).compareAgainst(passwordHash);
			}
			catch (final Exception e) {
				throw new RuntimeException("Error during authentication.", e);
			}
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final Password other = (Password) obj;
		if (id != other.id) return false;
		if (passwordHash == null) {
			if (other.passwordHash != null) return false;
		}
		else if (!passwordHash.equals(other.passwordHash)) return false;
		if (passwordSalt == null) {
			if (other.passwordSalt != null) return false;
		}
		else if (!passwordSalt.equals(other.passwordSalt)) return false;
		if (userId != other.userId) return false;
		return true;
	}

}