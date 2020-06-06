package bg.sofia.uni.fmi.mjt.splitwise.server.activerecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.gson.Gson;

public class FileSystemPersister implements Persister {

	private Gson encoder = new Gson();
	private Map<String, HashMap<String, Base>> db = new HashMap<String, HashMap<String, Base>>();

	public FileSystemPersister() {
		File dir = new File("resources");
		File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
		for (File file : files) {
			String objectType = file.getName().replaceFirst("[.][^.]+$", "");
			String output = objectType.substring(0, 1).toUpperCase() + objectType.substring(1);
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {

				Class<?> klass = Class.forName(Base.class.toString().replace("class ", "")
						.replace("activerecord", "models").replace("Base", output));

				ensureObjectRepo(objectType);
				String st;
				while ((st = br.readLine()) != null) {
					Base object = (Base) encoder.fromJson(st, klass);
					db.get(objectType).put(object.getId(), object);
				}
			} catch (IOException e) {
				throw new RuntimeException("Unable to open db files", e);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Unable to load model class", e);
			}
		}
	}

	@Override
	public boolean write(Base object) {
		String objectType = object.getClass().getSimpleName().toLowerCase();
		ensureObjectRepo(objectType);
		db.get(objectType).put(object.getId(), object);
		return persistToFile(objectType);
	}

	private boolean persistToFile(String objectType) {
		try (FileWriter file = new FileWriter(getFileName(objectType))) {
			for(Map.Entry<String, Base> entry : db.get(objectType).entrySet()) {
				file.write(String.format("%s\n", encoder.toJson(entry.getValue())));
			}
			System.out.println("Successfully Copied JSON Object to File...");
		} catch (IOException e) {
			throw new RuntimeException(String.format("Unable to write to %s", getFileName(objectType)), e);
		}
		return true;
	}

	public String getFileName(String objectType) {
		return String.format("resources/%s.json", objectType);
	}

	private void ensureObjectRepo(String objectType) {
		if (!db.containsKey(objectType)) {
			db.put(objectType, new HashMap<String, Base>());
		}
	}

	@Override
	public Base findOneBy(String objectType, Predicate<Base> predicate) {
		if(!db.containsKey(objectType)) {
			return null;
		}
		return (Base) db.get(objectType).values().stream().filter(predicate).findFirst().orElse(null);
	}

	@Override
	public ArrayList<Base> findAllBy(String objectType, Predicate<Base> predicate) {
		if(!db.containsKey(objectType)) {
			return new ArrayList<Base>();
		}
		return (ArrayList<Base>) db.get(objectType).values().stream().filter(predicate).collect(Collectors.toList());
	}
}
