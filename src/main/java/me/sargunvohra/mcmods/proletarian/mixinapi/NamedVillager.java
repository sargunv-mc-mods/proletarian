package me.sargunvohra.mcmods.proletarian.mixinapi;

import net.minecraft.village.VillagerType;

public interface NamedVillager {
	String getRenderedName();
	void setName(String first, String last);
	VillagerType getVillagerType();
}
