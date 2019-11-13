package me.sargunvohra.mcmods.proletarian.name;

import blue.endless.jankson.*;
import blue.endless.jankson.api.SyntaxError;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerType;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class VillagerNamer implements SimpleSynchronousResourceReloadListener {
	private static Language DEFAULT_LANGUAGE;
	private static NameSet DEFAULT_NAME_SET;
	private static Map<VillagerType, Language> LANGUAGES = new HashMap<>();
	private static Map<VillagerType, NameSet> NAME_SETS = new HashMap<>();
	private static float genRate;
	private static Random random = new Random();

	@Override
	public void apply(ResourceManager manager) {
		DEFAULT_LANGUAGE = null;
		DEFAULT_NAME_SET = null;
		LANGUAGES.clear();
		NAME_SETS.clear();
		genRate = 0.5F;
		Jankson jankson = Jankson.builder().build();
		try {
			Collection<Resource> resources = manager.getAllResources(new Identifier("proletarian", "config/names.json5"));
			for (Resource res : resources) {
				JsonObject json = jankson.load(IOUtils.toString(res.getInputStream(), Charset.defaultCharset()));
				if (json.containsKey("generated_name_rate")) {
					genRate = json.getFloat("generated_name_rate", 0.5F);
				}
				if (json.containsKey("languages")) {
					parseLanguages(json.getObject("languages"));
				}
				if (json.containsKey("name_sets")) {
					parseNameSets(json.getObject("name_sets"));
				}
			}

		} catch (IOException | SyntaxError e) {

		}
	}

	private void parseLanguages(JsonObject json) {
		if (json.containsKey("default")) {
			Language lang = makeLanguage(json.getObject("default"));
			if (lang != null) DEFAULT_LANGUAGE = lang;
			json.remove("default");
		}
		for (String key : json.keySet()) {
			Identifier id = new Identifier(key);
			VillagerType type = Registry.VILLAGER_TYPE.get(id);
			LANGUAGES.put(type, makeLanguage(json.getObject(key)));
		}
	}

	private Language makeLanguage(JsonObject json) {
		if (json == null) return null; //TODO log
		Map<Character, String> categories = new HashMap<>();
		Map<String, String> replacements = new HashMap<>();
		List<String> syllables = new ArrayList<>();
		int dropoff = json.getInt("dropoff", 50);
		int sylDrop = json.getInt("syllable_dropoff", 50);
		float monoSyl = json.getFloat("monosyllable_rate", 0.2F);
		JsonObject cats = json.getObject("categories");
		if (cats == null) return null;
		for (String key : cats.keySet()) {
			if (key.length() == 1) categories.put(key.charAt(0), cats.get(String.class, key));
		}
		JsonObject reps = json.getObject("replacements");
		if (reps != null) {
			for (String key : reps.keySet()) {
				replacements.put(key, reps.get(String.class, key));
			}
		}
		JsonElement syl = json.get("syllables");
		if (!(syl instanceof JsonArray)) return null;
		JsonArray sylArray = (JsonArray)syl;
		for (JsonElement syllable : sylArray) {
			if (syllable instanceof JsonPrimitive) {
				syllables.add(((JsonPrimitive)syllable).toString());
			}
		}
		return new Language(categories, replacements, syllables, dropoff, sylDrop, monoSyl);
	}

	private void parseNameSets(JsonObject json) {

	}

	@Override
	public Identifier getFabricId() {
		return new Identifier("proletarian", "villager_namer");
	}

	public static String getFirstName(VillagerType type) {
		if (random.nextFloat() < genRate) {
			return LANGUAGES.getOrDefault(type, DEFAULT_LANGUAGE).getName();
		} else {
			return NAME_SETS.getOrDefault(type, DEFAULT_NAME_SET).getFirstName();
		}
	}

	public static String getLastName(VillagerType type) {
		if (random.nextFloat() < genRate) {
			return LANGUAGES.getOrDefault(type, DEFAULT_LANGUAGE).getName();
		} else {
			return NAME_SETS.getOrDefault(type, DEFAULT_NAME_SET).getLastName();
		}
	}

	public static Pair<String, String> getFullName(VillagerType type) {
		if (random.nextFloat() < genRate) {
			Language lang = LANGUAGES.getOrDefault(type, DEFAULT_LANGUAGE);
			return new Pair<>(lang.getName(), lang.getName());
		} else {
			NameSet nameSet = NAME_SETS.getOrDefault(type, DEFAULT_NAME_SET);
			return new Pair<>(nameSet.getFirstName(), nameSet.getLastName());
		}
	}
}
