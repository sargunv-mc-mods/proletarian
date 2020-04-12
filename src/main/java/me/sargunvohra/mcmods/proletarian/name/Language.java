package me.sargunvohra.mcmods.proletarian.name;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class Language {
	private Map<Character, String> categories;
	private Map<String, String> replacements; //TODO: priority ordering
	private List<String> syllables;
	private String name;
	private float dropoff;
	private float sylDrop;
	private float monoSyl;
	private Random random = new Random();

	public Language(Map<Character, String> categories, Map<String, String> replacements, List<String> syllables, float dropoff, float sylDrop, float monoSyl, String name) {
		this(categories, replacements, syllables, dropoff, sylDrop, monoSyl);
		this.name = name;
	}

	public Language(Map<Character, String> categories, Map<String, String> replacements, List<String> syllables, float dropoff, float sylDrop, float monoSyl) {
		this.categories = categories;
		this.replacements = replacements;
		this.syllables = syllables;
		this.dropoff = dropoff;
		this.sylDrop = sylDrop;
		this.monoSyl = monoSyl;
		this.name = getName();
	}

	/**
	 * Cheap, iterative implementation of a power law.
	 * @param max How many bins to hop between.
	 * @param percent The chance of staying at any given bin.
	 * @return The number of the bin to stop at.
	 */
	private int powerLaw(int max, float percent) {
		for (int i = 0; true; i = (i + 1) % max) {
			if (random.nextFloat() < percent) return i;
		}
	}

	/**
	 * Power law with a set peak mode.
	 * @param max How many bins to hop between.
	 * @param mode The set peak.
	 * @param percent The chance of staying at any given bim.
	 * @return The number of the bin to stop at.
	 */
	private int peakedPowerLaw(int max, int mode, float percent) {
		if (random.nextBoolean()) {
			return mode + powerLaw(max - mode, percent);
		} else {
			return mode - powerLaw(mode + 1, percent);
		}
	}

	/**
	 * @return A constructed syllable from the set categories.
	 */
	private String syllable() {
		StringBuilder ret = new StringBuilder();
		int bin = powerLaw(syllables.size(), sylDrop);
		String pattern = syllables.get(bin);

		for (char c : pattern.toCharArray()) {
			if (categories.containsKey(c)) {
				char[] expansion = categories.get(c).toCharArray();
				int index;
				if (dropoff == 0) {
					index = random.nextInt(expansion.length);
				} else {
					index = powerLaw(expansion.length, dropoff);
				}
				ret.append(expansion[index]);
			} else {
				//not found; output directly
				ret.append(c);
			}
		}

		return ret.toString();
	}

	/**
	 * Generate a name!
	 * @return A string of syllables, with the first letter capitalized, and all replacements applied
	 */
	public String getName() {
		StringBuilder builder = new StringBuilder();

		int syllables = 1;
		if (monoSyl > 0F) {
			if (random.nextFloat() > monoSyl) {
				syllables += 1 + powerLaw(4, 0.5F);
			}
		}

		for (int i = 0; i < syllables; i++) {
			builder.append(syllable());
		}

		String ret = builder.toString();
		for (String regex : replacements.keySet()) {
			ret = ret.replaceAll(regex, replacements.get(regex));
		}
		//for some reason the names get an extra set of quotes added
		ret = ret.substring(1, ret.length() - 1);
		ret = ret.substring(0, 1).toUpperCase() + ret.substring(1);

		return ret;
	}

	public String getLanguageName() {
		return name;
	}

}