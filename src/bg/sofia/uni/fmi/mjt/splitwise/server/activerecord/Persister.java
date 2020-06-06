package bg.sofia.uni.fmi.mjt.splitwise.server.activerecord;

import java.util.ArrayList;
import java.util.function.Predicate;

public interface Persister {
		
	boolean write(Base object);


	Base findOneBy(String objectType, Predicate<Base> predicate);


	ArrayList<Base> findAllBy(String objectType, Predicate<Base> prediacte);
	
}