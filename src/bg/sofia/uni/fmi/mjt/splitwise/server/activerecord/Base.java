package bg.sofia.uni.fmi.mjt.splitwise.server.activerecord;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Predicate;

public abstract class Base {
	
	public transient static Persister persister = new FileSystemPersister();
	protected String id;

	public Base() {
		id = UUID.randomUUID().toString();
	}
    
	protected abstract boolean validate();
    
	public synchronized boolean save() {
	  if (validate()) {
		  return persister.write(this);
	  }
	  return false;
	}
	
	public synchronized static Base findOneBy(String objectType, Predicate<Base> predicate) {
		return persister.findOneBy(objectType, predicate);
	}
	
	public synchronized static ArrayList<Base> findAllBy(String objectType, Predicate<Base> predicate) {
		return persister.findAllBy(objectType, predicate);
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
}
