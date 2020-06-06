package bg.sofia.uni.fmi.mjt.splitwise.server.activerecord;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.function.Predicate;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

public class BaseTest {

	static private Persister persiter = mock(FileSystemPersister.class);
	
	@BeforeClass
	public static void before() {
		Base.persister = persiter;		
	}

	@Test
	public void saveInvokesPersistersWriteTest() {
		ModelStub model = new ModelStub();
		doReturn(true).when(persiter).write(model);
		assertTrue(model.save());
		verify(persiter).write(model);
	}
	
	@Test
	public void saveDoesNotInvokePersistersWriteIfValidateFailsTest() {
		ModelStub model = spy(new ModelStub());
		doReturn(false).when(model).validate();
		doReturn(true).when(persiter).write(model);
		assertFalse(model.save());
		verify(persiter, never()).write(model);
	}
	
	@Test
	public void fineOneByInvokesPersistersFindOneBy() {
		Predicate<Base> predicate = (model) -> { return true; };
		
		doReturn(null).when(persiter).findOneBy("modelstub", predicate);
		
		ModelStub.findOneBy(predicate);
		verify(persiter).findOneBy("modelstub", predicate);
	}
	
	@Test
	public void fineAllByInvokesPersistersFindOneBy() {
		Predicate<Base> predicate = (model) -> { return true; };
		
		doReturn(new ArrayList<Base>()).when(persiter).findAllBy("modelstub", predicate);
		
		ModelStub.findAllBy(predicate);
		verify(persiter).findAllBy("modelstub", predicate);
	}
}
