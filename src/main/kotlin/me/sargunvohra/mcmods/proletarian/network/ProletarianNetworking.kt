package me.sargunvohra.mcmods.proletarian.network

import io.netty.buffer.Unpooled
import me.sargunvohra.mcmods.proletarian.id
import me.sargunvohra.mcmods.proletarian.mixinapi.NamedVillager
import me.sargunvohra.mcmods.proletarian.name.VillagerNamer
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.PacketByteBuf

object ProletarianNetworking {
    fun registerClient() {
        ClientSidePacketRegistry.INSTANCE.register(id("villager_name")) { context, buf ->
            val id = buf.readInt()
            val first = buf.readString()
            val last = buf.readString()
            val entity = context.player.world.getEntityById(id)
            if (entity is NamedVillager) {
                val villager = entity as NamedVillager
                villager.setName(first, last)
            }
        }
    }

    fun registerCommon() {
        ServerSidePacketRegistry.INSTANCE.register(id("villager_name_request")) { context, buf ->
            val id = buf.readInt()
            val entity = context.player.world.getEntityById(id)
            if (entity is NamedVillager) {
                val villager = entity as NamedVillager
                val first = VillagerNamer.getFirstName(villager.villagerType)
                val last = VillagerNamer.getLastName(villager.villagerType)
                villager.setName(first, last)
                sendVillagerName(context.player, entity, first, last)
            }
        }
    }

    fun sendVillagerName(player: PlayerEntity, villager: Entity, first: String, last: String) {
        val buf = PacketByteBuf(Unpooled.buffer())
        buf.writeInt(villager.entityId)
        buf.writeString(first)
        buf.writeString(last)
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, id("villager_name"), buf)
    }

    fun requestVillagerName(villager: Entity) {
        val buf = PacketByteBuf(Unpooled.buffer())
        buf.writeInt(villager.entityId)
        ClientSidePacketRegistry.INSTANCE.sendToServer(id("villager_name_request"), buf)
    }
}
