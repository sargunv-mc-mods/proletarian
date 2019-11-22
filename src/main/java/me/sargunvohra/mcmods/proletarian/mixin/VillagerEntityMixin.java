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
import net.minecraft.entity.ai.brain.MemoryModuleType;
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
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

//TODO: transfer name to zombie when killed, death messages?
@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends AbstractTraderEntity implements NamedVillager {
    private String firstName;
    private String lastName;

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

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;Lnet/minecraft/village/VillagerType;)V", at = @At("RETURN"))
    private void genNameOnInit(EntityType<? extends VillagerEntity> type, World world, VillagerType villagerType, CallbackInfo info) {
        if (!world.isClient) {
            firstName = VillagerNamer.getFirstName(villagerType);
            lastName = VillagerNamer.getLastName(villagerType);
            PlayerStream.watching(this).forEach(player -> ProletarianNetworking.INSTANCE.sendVillagerName(player, this, firstName, lastName));
        }
    }

    @Inject(method = "getDisplayName", at = @At("TAIL"), cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void addName(CallbackInfoReturnable<Text> info, AbstractTeam team) {
        Text oldName = info.getReturnValue();
        if ((firstName == null || lastName == null) && world.isClient) {
            ProletarianNetworking.INSTANCE.requestVillagerName(this);
            return;
        }
        Text newName = new LiteralText(firstName + " " + lastName + " (");
        newName.append(oldName);
        newName.append(new LiteralText(")"));
        newName.styled((style) -> style.setHoverEvent(this.getHoverEvent()).setInsertion(this.getUuidAsString()));
        if (team != null) {
            newName.formatted(team.getColor());
        }
        info.setReturnValue(newName);
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
        if (tag.containsKey("Proletarian", NbtType.COMPOUND)) {
            CompoundTag proleTag = tag.getCompound("Proletarian");
            boolean shouldSendName = false;
            if (proleTag.containsKey("FirstName", NbtType.STRING)) {
                this.firstName = proleTag.getString("FirstName");
            } else {
                this.firstName = VillagerNamer.getFirstName(getVillagerData().getType());
                shouldSendName = true;
            }
            if (proleTag.containsKey("LastName", NbtType.STRING)) {
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

    @Inject(method = "method_7225", at = @At("RETURN"))
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
}
