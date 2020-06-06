package bg.sofia.uni.fmi.mjt.splitwise.server.activerecord;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.junit.Test;
import org.mockito.internal.util.reflection.FieldSetter;
import static org.mockito.Mockito.*;

public class FilesystemPersisterTest {

	private Map<String, HashMap<String, Base>> mockedDb = spy(new HashMap<String, HashMap<String, Base>>());
	private FileSystemPersister persister;
	private Predicate<Base> predicate = (model) -> { return true; };

	@Test
	public void findOneByReturnsNullTest() {
		persister = new FileSystemPersister();
		mockDb();
		doReturn(false).when(mockedDb).containsKey("class");
		assertNull(persister.findOneBy("class", predicate));
		verify(mockedDb).containsKey("class");
	}
	
	@Test
	public void findOneByReturnsBaseTest() {
		persister = new FileSystemPersister();
		mockDb();
		doReturn(true).when(mockedDb).containsKey("class");
		doReturn(new HashMap<String, Base>()).when(mockedDb).get("class");
		assertNull(persister.findOneBy("class", predicate));
		verify(mockedDb).get("class");
	}
	
	@Test
	public void findAllByReturnsNullTest() {
		persister = new FileSystemPersister();
		mockDb();
		doReturn(false).when(mockedDb).containsKey("class");
		assertEquals(0, persister.findAllBy("class", predicate).size());
		verify(mockedDb).containsKey("class");
	}
	
	@Test
	public void findAllByReturnsBaseTest() {
		persister = new FileSystemPersister();
		mockDb();
		doReturn(true).when(mockedDb).containsKey("class");
		doReturn(new HashMap<String, Base>()).when(mockedDb).get("class");
		assertEquals(0, persister.findAllBy("class", predicate).size());
		verify(mockedDb).get("class");
	}
	
	@Test
	public void getFileNameTest() {
		persister = new FileSystemPersister();
		final String fileName = "resources/class.json";
		assertEquals(fileName, persister.getFileName("class"));
	}

	private void mockDb() {
		try {
			FieldSetter.setField(persister, persister.getClass().getDeclaredField("db"), mockedDb);
		} catch (NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
