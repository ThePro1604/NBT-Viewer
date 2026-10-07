package marioandweegee3.nbtviewer

import marioandweegee3.nbtviewer.gui.NBTViewerGui
import marioandweegee3.nbtviewer.gui.NBTViewerScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.Identifier
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Environment(EnvType.CLIENT)
@Suppress("unused")
object NBTViewerMod : ClientModInitializer {
    const val modId = "nbtviewer"
    val logger: Logger = LoggerFactory.getLogger("NBT Viewer")

    private val category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(modId, "keys"))
    private lateinit var key: KeyMapping

    override fun onInitializeClient() {
        logger.info("Initializing...")

        key = KeyMappingHelper.registerKeyMapping(
            KeyMapping(
                "key.nbtviewer.view_nbt",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_RALT,
                category
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (key.consumeClick()) {
                val player = client.player ?: return@register
                val stack = player.getItemInHand(InteractionHand.MAIN_HAND)

                if (stack.isEmpty) {
                    return@register
                }

                // Convert all item components to NBT format for display
                // This includes enchantments, damage, custom name, lore, etc.
                val registries = client.level?.registryAccess() ?: return@register

                // Use the ItemStack CODEC to encode the stack to NBT
                val registryOps = registries.createSerializationContext(NbtOps.INSTANCE)
                val encodedResult = ItemStack.CODEC.encodeStart(registryOps, stack)

                // Get the result or use empty compound if encoding failed
                val nbt = encodedResult.resultOrPartial { error ->
                    logger.error("Failed to encode ItemStack: $error")
                }.orElse(CompoundTag()) as? CompoundTag ?: CompoundTag()

                client.gui.setScreen(
                    NBTViewerScreen(NBTViewerGui(nbt))
                )
            }
        }
    }
}
