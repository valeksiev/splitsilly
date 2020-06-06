package bg.sofia.uni.fmi.mjt.splitwise.server.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

import bg.sofia.uni.fmi.mjt.splitwise.server.activerecord.Base;
import bg.sofia.uni.fmi.mjt.splitwise.server.activerecord.FileSystemPersister;
import bg.sofia.uni.fmi.mjt.splitwise.server.activerecord.Persister;

public class UserTest {
	
	private User user;
	
	@Mock
	static private Persister persiter = spy(new FileSystemPersister());
	
	@BeforeClass
	public static void beforeAll() {
		Base.persister = persiter;
	}
	@Before
	public void before() {
		user = new User();
	}
	
	@Test
	public void validateTest() {
		assertFalse(user.validate());
		final String username = "username";
		final String password = "password";
		
		user.setUsername(username);
		assertFalse(user.validate());
		user.setPassword("");
		assertFalse(user.validate());
		user.setPassword(null);
		assertFalse(user.validate());
		user.setPassword(password);
		assertTrue(user.validate());
	}
	
	@Test
	public void validateUsernameTakenTest() {
		final String username = "username";
		final String password = "password";
		user = new User(username, password);
		doReturn(new User("username", "password")).when(persiter).findOneBy(any(), any());
		assertFalse(user.validate());
	}

	@Test
	public void hashPasswordTest() {
		final String password = "some_password";
		user.setPassword(password);
		assertNotEquals(password, user.getPassword());
	}
	
	@Test
	public void constructorTest() {
		final String username = "username";
		final String password = "password";
		user = new User(username, password);
		assertTrue(user.validate());
		assertEquals(username, user.getUsername());
	}
	
	@Test
	public void passwordEqualsTest() {
		final String username = "username";
		final String password = "password";
		user = new User(username, password);
		assertFalse(user.passwordEquals(null));
		assertFalse(user.passwordEquals(""));
		assertFalse(user.passwordEquals("other"));
		assertTrue(user.passwordEquals(password));
	}
	
	@Test
	public void findOneByTest() {
		Predicate<Base> predicate = (model) -> { return true; };
		doReturn(null).when(persiter).findOneBy("user", predicate);
		User.findOneBy(predicate);
		verify(persiter).findOneBy("user", predicate);
	}

	@Test
	public void findAllByTest() {
		Predicate<Base> predicate = (model) -> { return true; };
		doReturn(new ArrayList<User>()).when(persiter).findAllBy("user", predicate);
		User.findAllBy(predicate);
		verify(persiter).findAllBy("user", predicate);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void getFriendhips() {
		Group friendshipGroup = spy(new Group());
		Group nonfriendshipGroup = spy(new Group());
		friendshipGroup.setFriendship(true);
		nonfriendshipGroup.setFriendship(false);

		List<Group> groups = new ArrayList<>();
		groups.add(friendshipGroup);
		groups.add(nonfriendshipGroup);
		
		doReturn(groups).when(persiter).findAllBy(eq("group"), any(Predicate.class));
		
		List<Group> friendships = user.getFriendships();
		assertTrue(friendships.contains(friendshipGroup));
		assertFalse(friendships.contains(nonfriendshipGroup));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void getNonFriendhips() {
		Group friendshipGroup = spy(new Group());
		Group nonfriendshipGroup = spy(new Group());
		friendshipGroup.setFriendship(true);
		nonfriendshipGroup.setFriendship(false);

		List<Group> groups = new ArrayList<>();
		groups.add(friendshipGroup);
		groups.add(nonfriendshipGroup);
		
		doReturn(groups).when(persiter).findAllBy(eq("group"), any(Predicate.class));
		
		List<Group> friendships = user.getNonFriendships();
		assertFalse(friendships.contains(friendshipGroup));
		assertTrue(friendships.contains(nonfriendshipGroup));
	}
}
