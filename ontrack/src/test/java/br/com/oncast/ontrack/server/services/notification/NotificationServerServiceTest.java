package br.com.oncast.ontrack.server.services.notification;

import br.com.oncast.ontrack.server.services.authentication.AuthenticationManager;
import br.com.oncast.ontrack.server.services.email.MailService;
import br.com.oncast.ontrack.server.services.email.NotificationMail;
import br.com.oncast.ontrack.server.services.multicast.MulticastService;
import br.com.oncast.ontrack.server.services.persistence.PersistenceService;
import br.com.oncast.ontrack.server.services.persistence.exceptions.NoResultFoundException;
import br.com.oncast.ontrack.server.services.persistence.exceptions.PersistenceException;
import br.com.oncast.ontrack.shared.exceptions.business.UnableToCreateNotificationException;
import br.com.oncast.ontrack.shared.exceptions.business.UnableToRetrieveNotificationListException;
import br.com.oncast.ontrack.shared.model.user.User;
import br.com.oncast.ontrack.shared.model.uuid.UUID;
import br.com.oncast.ontrack.shared.services.notification.Notification;
import br.com.oncast.ontrack.shared.services.notification.NotificationBuilder;
import br.com.oncast.ontrack.shared.services.notification.NotificationCreatedEvent;
import br.com.oncast.ontrack.shared.services.notification.NotificationTestUtils;
import br.com.oncast.ontrack.shared.services.notification.NotificationType;
import br.com.oncast.ontrack.utils.model.ProjectTestUtils;
import br.com.oncast.ontrack.utils.model.UserTestUtils;

import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static br.com.oncast.ontrack.utils.model.UserTestUtils.createUser;

public class NotificationServerServiceTest {

	NotificationServerService notificationServerService;

	@Mock
	AuthenticationManager authenticationManager;

	@Mock
	PersistenceService persistenceService;

	@Mock
	MulticastService multicastService;

	@Mock
	MailService mailService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		notificationServerService = new NotificationServerServiceImpl(authenticationManager, persistenceService, multicastService, mailService);
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void registerNewNotificationShouldPersistAndMulticast() throws PersistenceException, UnableToCreateNotificationException, MessagingException, NoResultFoundException {
		final User user1 = createUser();
		final User user2 = createUser();
		final ArrayList<User> userList = new ArrayList<User>();
		userList.add(user1);
		userList.add(user2);
		when(persistenceService.retrieveUsersByIds(Mockito.any(List.class))).thenReturn(userList);
		final Notification notification = getBuilder().setDescription("msg1").addReceipient(user1.getId()).addReceipient(user2.getId()).getNotification();

		final ArgumentCaptor<Notification> persistenceCaptor = ArgumentCaptor.forClass(Notification.class);
		final ArgumentCaptor<NotificationCreatedEvent> multicastEventCaptor = ArgumentCaptor.forClass(NotificationCreatedEvent.class);
		final ArgumentCaptor<List> multicastUsersCaptor = ArgumentCaptor.forClass(List.class);

		notificationServerService.registerNewNotification(notification);

		verify(persistenceService).persistOrUpdateNotification(persistenceCaptor.capture());
		final Notification capturedNotificationFromPersistence = persistenceCaptor.getValue();
		assertEquals(notification, capturedNotificationFromPersistence);

		verify(multicastService).multicastToUsers(multicastEventCaptor.capture(), multicastUsersCaptor.capture());
		final NotificationCreatedEvent capturedNotificationFromMulticastEvent = multicastEventCaptor.getValue();
		final List<User> capturedNotificationFromMulticastUserList = multicastUsersCaptor.getValue();
		assertEquals(notification, capturedNotificationFromMulticastEvent.getNotification());
		assertEquals(2, capturedNotificationFromMulticastUserList.size());
		assertEquals(user1, capturedNotificationFromMulticastUserList.get(0));
		assertEquals(user2, capturedNotificationFromMulticastUserList.get(1));
	}

	private NotificationBuilder getBuilder() {
		return new NotificationBuilder(NotificationType.IMPEDIMENT_CREATED, ProjectTestUtils.createRepresentation(new UUID("")), new UUID());
	}

	@Test
	public void retrieveCurrentUserNotificationListShouldReturnEmptyListIfThereAreNoNotificationsOnPersistence() throws UnableToRetrieveNotificationListException, NoResultFoundException,
			PersistenceException {
		final User user1 = createUser();
		when(authenticationManager.getAuthenticatedUser()).thenReturn(user1);
		when(persistenceService.retrieveLatestNotificationsForUser(user1.getId(), NotificationServerServiceImpl.MAX_NUMBER_OF_NOTIFICATIONS)).thenThrow(new NoResultFoundException("", null));
		final List<Notification> notificationList = notificationServerService.retrieveCurrentUserNotificationList();
		assertEquals(0, notificationList.size());
	}

	@Test
	public void retrieveCurrentUserNotificationListShouldReturnNotificationsReturnedByPersistence() throws UnableToRetrieveNotificationListException, NoResultFoundException, PersistenceException {
		final User user1 = createUser();
		final User user2 = createUser();
		final Notification notification = getBuilder().setDescription("msg1").addReceipient(user1.getId()).addReceipient(user2.getId()).getNotification();
		final List<Notification> list = new ArrayList<Notification>();
		list.add(notification);

		when(authenticationManager.getAuthenticatedUser()).thenReturn(user1);
		when(persistenceService.retrieveLatestNotificationsForUser(user1.getId(), NotificationServerServiceImpl.MAX_NUMBER_OF_NOTIFICATIONS)).thenReturn(list);
		final List<Notification> notificationList = notificationServerService.retrieveCurrentUserNotificationList();

		assertEquals(list, notificationList);
	}

	@Test
	public void shouldSendNotificationEmailWhenAnImportantNotificationIsRegistered() throws Exception {
		final int numberOfNotificationRecipients = 5;
		final Notification importantNotification = spy(NotificationTestUtils.createImportantMail());
		final List<UUID> userIds = new ArrayList<UUID>();
		final List<User> users = new ArrayList<User>();;
		for (int i = 0; i < numberOfNotificationRecipients; i++) {
			final User user = UserTestUtils.createUser();
			users.add(user);
			userIds.add(user.getId());
		}
		when(importantNotification.getRecipientsAsUserIds()).thenReturn(userIds);
		when(persistenceService.retrieveUsersByIds(userIds)).thenReturn(users);

		notificationServerService.registerNewNotification(importantNotification);

		verify(mailService, times(numberOfNotificationRecipients)).send(Mockito.any(NotificationMail.class));
	}

	@Test
	public void shouldNotSendNotificationEmailWhenANotImportantNotificationIsRegistered() throws Exception {
		final Notification notification = NotificationTestUtils.createNotImportantMail();
		notificationServerService.registerNewNotification(notification);
		Mockito.verifyZeroInteractions(mailService);
	}
}
