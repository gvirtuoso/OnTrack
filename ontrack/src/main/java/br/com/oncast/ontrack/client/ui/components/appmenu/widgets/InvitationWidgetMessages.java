package br.com.oncast.ontrack.client.ui.components.appmenu.widgets;

import com.google.gwt.i18n.client.LocalizableResource.Generate;
import com.google.gwt.i18n.client.LocalizableResource.GenerateKeys;
import com.google.gwt.i18n.client.Messages;

@Generate(format = { "com.google.gwt.i18n.rebind.format.PropertiesFormat" }, locales = { "default" })
@GenerateKeys
public interface InvitationWidgetMessages extends Messages {

	@Description("inivitation quota information message")
	@DefaultMessage("You have ''{0}'' invitations left.")
	String inivitationQuota(int invitationQuota);

	@Description("user invited with success message")
	@DefaultMessage("''{0}'' was invited!")
	String userInvited(String mail);

	@Description("user already invited error message")
	@DefaultMessage("''{0}'' already has been invited")
	String userAlreadyInvited(String mail);

}