package bg.sofia.uni.fmi.mjt.splitwise.server;

import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;

import bg.sofia.uni.fmi.mjt.splitwise.server.activerecord.Base;
import bg.sofia.uni.fmi.mjt.splitwise.server.activerecord.FileSystemPersister;
import bg.sofia.uni.fmi.mjt.splitwise.server.activerecord.Persister;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;

public class ConnectionHandlerRunnableTest {
	
	ConnectionHandlerRunnable runnable;
	@Mock
	static private Persister persiter = spy(new FileSystemPersister());

	private PrintWriter mockedWriter = spy(new PrintWriter(System.out));

	@BeforeClass
	public static void beforeAll() {
		Base.persister = persiter;
	}
	
	@Before
	public void beforeEach() {
		runnable = new ConnectionHandlerRunnable();
		mockWriter();
	}
	
	@Test
	public void registerTest() {
		final String command = "register";
		Queue<String> tokens = new LinkedList<>();
		tokens.add("username");
		tokens.add("password");
		
		doReturn(true).when(persiter).write(any());
		doReturn(null).when(persiter).findOneBy(eq("user"), any());

		
		runnable.handleCommandForUnsignedUser(command, tokens);
		verify(persiter).write(any());
	}
	
	@Test
	public void registerFailTest() {
		final String command = "register";
		Queue<String> tokens = new LinkedList<>();
		tokens.add("username");
		tokens.add("password");
		User user = new User("username", "password");

		doReturn(false).when(persiter).write(any());
		doReturn(user).when(persiter).findOneBy(eq("user"), any());
		
		runnable.handleCommandForUnsignedUser(command, tokens);
		verify(persiter).write(any());
		verify(mockedWriter).println("Unable to register as username");
	}
	
	@Test
	public void loginTest() {
		final String command = "login";
		Queue<String> tokens = new LinkedList<>();
		User user = new User("username", "password");
		tokens.add("username");
		tokens.add("password");
		
		doReturn(user).when(persiter).findOneBy(eq("user"), any());
	
		runnable.handleCommandForUnsignedUser(command, tokens);
		verify(persiter).findOneBy(eq("user"), any());
		verify(mockedWriter).println("Successfully signed in as username");
	}
	
	@Test
	public void loginFailTest() {
		final String command = "login";
		Queue<String> tokens = new LinkedList<>();
		tokens.add("username");
		tokens.add("password");
		
		doReturn(null).when(persiter).findOneBy(eq("user"), any());
		runnable.handleCommandForUnsignedUser(command, tokens);
		verify(mockedWriter).println("Unable to sign in as username");
	}
	
	private void mockWriter() {
		try {
			FieldSetter.setField(runnable, runnable.getClass().getDeclaredField("writer"), mockedWriter);
			doNothing().when(mockedWriter).println(any(String.class));
		} catch (NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
