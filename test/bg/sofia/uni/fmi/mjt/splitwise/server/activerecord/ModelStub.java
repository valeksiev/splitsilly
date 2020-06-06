package bg.sofia.uni.fmi.mjt.splitwise.server.activerecord;

import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import bg.sofia.uni.fmi.mjt.splitwise.server.activerecord.Base;

public class ModelStub extends Base {

	@Override
	protected boolean validate() {
		return true;
	}
	
	public static ModelStub findOneBy(Predicate<Base> predicate) {
		return (ModelStub) findOneBy("modelstub", predicate);
	}

	public static ArrayList<ModelStub> findAllBy(Predicate<Base> predicate) {
		return (ArrayList<ModelStub>) findAllBy("modelstub", predicate)
				.stream()
				.map(model -> (ModelStub) model)
				.collect(Collectors.toList());	
	}
}
