package me.sargunvohra.mcmods.proletarian.mixin;

import com.google.common.collect.ImmutableList;
import me.sargunvohra.mcmods.proletarian.mixinapi.NamedVillager;
import me.sargunvohra.mcmods.proletarian.name.VillagerNamer;
import me.sargunvohra.mcmods.proletarian.network.ProletarianNetworking;
import me.sargunvohra.mcmods.proletarian.profession.CustomProfessionInit;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.passive.AbstractTraderEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

//TODO: transfer name to zombie when killed, death messages?
@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends AbstractTraderEntity implements NamedVillager {
    private String firstName;
    private String lastName;

    private static final Schedule CRAFTER_SCHEDULE = new ScheduleBuilder(Registry.register(Registry.SCHEDULE, new Identifier("proletarian", "villager_crafting"), new Schedule()))
            .withActivity(10, Activity.IDLE)
            .withActivity(1000, Activity.WORK)
            .withActivity(9000, Activity.MEET)
            .withActivity(10000, Activity.WORK)
            .withActivity(12000, Activity.REST)
            .build();

    @Shadow @Final @Mutable private static ImmutableList<MemoryModuleType<?>> MEMORY_MODULES =
            ImmutableList.of(MemoryModuleType.HOME,
                    MemoryModuleType.JOB_SITE,
                    MemoryModuleType.MEETING_POINT,
                    MemoryModuleType.MOBS,
                    MemoryModuleType.VISIBLE_MOBS,
                    MemoryModuleType.VISIBLE_VILLAGER_BABIES,
                    MemoryModuleType.NEAREST_PLAYERS,
                    MemoryModuleType.NEAREST_VISIBLE_PLAYER,
                    MemoryModuleType.WALK_TARGET,
                    MemoryModuleType.LOOK_TARGET,
                    MemoryModuleType.INTERACTION_TARGET,
                    MemoryModuleType.BREED_TARGET,
                    new MemoryModuleType[]{
                            MemoryModuleType.PATH,
                            MemoryModuleType.INTERACTABLE_DOORS,
                            MemoryModuleType.OPENED_DOORS,
                            MemoryModuleType.NEAREST_BED,
                            MemoryModuleType.HURT_BY,
                            MemoryModuleType.HURT_BY_ENTITY,
                            MemoryModuleType.NEAREST_HOSTILE,
                            MemoryModuleType.SECONDARY_JOB_SITE,
                            MemoryModuleType.HIDING_PLACE,
                            MemoryModuleType.HEARD_BELL_TIME,
                            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                            MemoryModuleType.LAST_SLEPT,
                            MemoryModuleType.LAST_WORKED_AT_POI,
                            MemoryModuleType.GOLEM_LAST_SEEN_TIME,
                            CustomProfessionInit.INSTANCE.getLastEatenModule()
            });

    @Shadow public abstract VillagerData getVillagerData();

    @Shadow public abstract void writeCustomDataToTag(CompoundTag compoundTag_1);

    public VillagerEntityMixin(EntityType<? extends AbstractTraderEntity> type, World world) {
        super(type, world);
    }

    @ModifyArg(method = "initBrain", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/brain/Brain;setSchedule(Lnet/minecraft/entity/ai/brain/Schedule;)V"))
    private Schedule initCrafterBrain(Schedule original) {
        if (!this.isBaby() && this.getVillagerData().getProfession().equals(CustomProfessionInit.INSTANCE.getProfession())) {
            return CRAFTER_SCHEDULE;
        }
        return original;
    }

    public Text getName() {
        Text oldName = getDefaultName();
        if ((firstName == null || lastName == null) && world.isClient) {
            ProletarianNetworking.INSTANCE.requestVillagerName(this);
            return oldName;
        }
        Text newName = new LiteralText(firstName + " " + lastName + " (");
        newName.append(oldName);
        newName.append(new LiteralText(")"));
        return newName;
    }

    @Inject(method = "writeCustomDataToTag", at = @At("RETURN"))
    private void writeTagData(CompoundTag tag, CallbackInfo info) {
        CompoundTag proleTag = new CompoundTag();
        boolean shouldSendName = false;
        if (firstName == null) {
            firstName = VillagerNamer.getFirstName(getVillagerData().getType());
            proleTag.putString("FirstName", VillagerNamer.getFirstName(getVillagerData().getType()));
            shouldSendName = true;
        } else {
            proleTag.putString("FirstName", firstName);
        }
        if (lastName == null) {
            lastName = VillagerNamer.getLastName(getVillagerData().getType());
            proleTag.putString("LastName", lastName);
            shouldSendName = true;
        } else {
            proleTag.putString("LastName", lastName);
        }
        if (!world.isClient && shouldSendName && firstName != null && lastName != null) {
            PlayerStream.watching(this).forEach((player -> ProletarianNetworking.INSTANCE.sendVillagerName(player, (VillagerEntity)(Object)this, firstName, lastName)));
        }
        tag.put("Proletarian", proleTag);
    }

    @Inject(method = "readCustomDataFromTag", at = @At("RETURN"))
    private void readTagData(CompoundTag tag, CallbackInfo info) {
        if (tag.contains("Proletarian", NbtType.COMPOUND)) {
            CompoundTag proleTag = tag.getCompound("Proletarian");
            boolean shouldSendName = false;
            if (proleTag.contains("FirstName", NbtType.STRING)) {
                this.firstName = proleTag.getString("FirstName");
            } else {
                this.firstName = VillagerNamer.getFirstName(getVillagerData().getType());
                shouldSendName = true;
            }
            if (proleTag.contains("LastName", NbtType.STRING)) {
                this.lastName = proleTag.getString("LastName");
            } else {
                this.lastName = VillagerNamer.getLastName(getVillagerData().getType());
                shouldSendName = true;
            }
            if (!world.isClient && shouldSendName && firstName != null && lastName != null) {
                PlayerStream.watching(this).forEach((player -> ProletarianNetworking.INSTANCE.sendVillagerName(player, (VillagerEntity)(Object)this, firstName, lastName)));
            }
        }
    }

    @Inject(method = "createChild", at = @At("RETURN"))
    private void injectBabyLastName(PassiveEntity partner, CallbackInfoReturnable<VillagerEntity> info) {
        ((VillagerEntityMixin)(Object)info.getReturnValue()).lastName = this.lastName;
    }

    @Inject(method = "onStruckByLightning", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void transferNameToWitch(LightningEntity lightning, CallbackInfo info, WitchEntity witch) {
        if (!this.hasCustomName()) {
            witch.setCustomName(new LiteralText(this.firstName + " " + this.lastName));
            witch.setCustomNameVisible(true);
        }
    }

    @Override
    public boolean shouldRenderName() {
        return true;
    }

    @Override
    public String getRenderedName() {
        return firstName + " " + lastName;
    }

    @Override
    public void setName(String first, String last) {
        this.firstName = first;
        this.lastName = last;
    }

    @Override
    public VillagerType getVillagerType() {
        return getVillagerData().getType();
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }
}
