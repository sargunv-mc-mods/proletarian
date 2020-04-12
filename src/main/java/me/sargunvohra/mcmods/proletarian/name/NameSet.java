package me.sargunvohra.mcmods.proletarian.name;

import java.util.List;
import java.util.Random;

public class NameSet {
	private List<String> FIRST_NAMES;
	private List<String> LAST_NAMES;
	private Random random = new Random();

	public NameSet(List<String> first, List<String> last) {
		this.FIRST_NAMES = first;
		this.LAST_NAMES = last;
	}

	public String getFirstName() {
		String ret = FIRST_NAMES.get(random.nextInt(FIRST_NAMES.size()));
		//for some reason the names get an extra set of quotes added
		return ret.substring(1, ret.length() - 1);
	}

	public String getLastName() {
		String ret = LAST_NAMES.get(random.nextInt(LAST_NAMES.size()));
		//for some reason the names get an extra set of quotes added
		return ret.substring(1, ret.length() - 1);
	}
}
