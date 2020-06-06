package bg.sofia.uni.fmi.mjt.splitwise.server.models;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

import bg.sofia.uni.fmi.mjt.splitwise.server.activerecord.Base;
import bg.sofia.uni.fmi.mjt.splitwise.server.activerecord.FileSystemPersister;
import bg.sofia.uni.fmi.mjt.splitwise.server.activerecord.Persister;

public class GroupTest {
	
	private Group group;
	User payer;
	User receiver;
	List<Transaction> transactions;

	
	@Mock
	static private Persister persiter = spy(new FileSystemPersister());
	
	@BeforeClass
	public static void beforeAll() {
		Base.persister = persiter;
	}
	@Before
	public void before() {
		group = new Group();
		group.setName("TEST_GROUP");
		group.isFriendship(true);
		
		payer = new User("payer", "password");
		receiver = new User("receiver", "password");
		
		List<User> users = new ArrayList<>();
		users.add(payer);
		users.add(receiver);
		
		transactions = new ArrayList<>();
		Transaction transaction = new Transaction();
		transaction.setPayer(payer);
		transaction.setReceiver(receiver);
		transaction.setAmount(10f);
		transaction.setDescription("description");
		transaction.setPayingFor(group);
		
		transactions.add(transaction);
		
		doReturn(users).when(persiter).findAllBy(eq("user"), any());
		doReturn(transactions).when(persiter).findAllBy(eq("transaction"), any());
	}
	
	@Test
	public void calculatesTheSummaryForThePayerForOneTransaction() {
		doReturn(receiver).when(persiter).findOneBy(eq("user"), any());
		PrintWriter writer = spy(new PrintWriter(System.out, true));
		doNothing().when(writer).println(any(String.class));
		
		group.getSummaryFor(payer, writer);
		verify(writer).println("- receiver: owes you 10.00");
	}
	
	@Test
	public void calculatesTheSummaryForTheReceiverForOneTransaction() {
		doReturn(payer).when(persiter).findOneBy(eq("user"), any());
		PrintWriter writer = spy(new PrintWriter(System.out, true));
		doNothing().when(writer).println(any(String.class));

		group.getSummaryFor(receiver, writer);
		verify(writer).println("- payer: you owe 10.00");
	}
}
