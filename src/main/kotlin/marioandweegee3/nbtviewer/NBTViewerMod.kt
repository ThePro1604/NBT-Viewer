package marioandweegee3.nbtviewer

import marioandweegee3.nbtviewer.gui.NBTViewerGui
import marioandweegee3.nbtviewer.gui.NBTViewerScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.component.DataComponentTypes
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Hand
import org.lwjgl.glfw.GLFW
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Environment(EnvType.CLIENT)
@Suppress("unused")
object NBTViewerMod : ClientModInitializer {
    const val modId = "nbtviewer"
    val logger: Logger = LoggerFactory.getLogger("NBT Viewer")

    private lateinit var key: KeyBinding

    override fun onInitializeClient() {
        logger.info("Initializing...")

        key = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.nbtviewer.view_nbt",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_ALT,
                "category.nbtviewer.keys"
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (key.wasPressed()) {
                val player = client.player ?: return@register
                val stack = player.getStackInHand(Hand.MAIN_HAND)

                if (stack.isEmpty) {
                    return@register
                }

                // Get custom NBT data from the CUSTOM_DATA component
                val customData = stack.get(DataComponentTypes.CUSTOM_DATA)
                val nbt = customData?.copyNbt() ?: NbtCompound()

                client.setScreen(
                    NBTViewerScreen(NBTViewerGui(nbt))
                )
            }
        }
    }
}