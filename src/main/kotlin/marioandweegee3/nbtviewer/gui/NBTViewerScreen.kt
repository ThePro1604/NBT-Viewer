package marioandweegee3.nbtviewer.gui

import io.github.cottonmc.cotton.gui.client.CottonClientScreen
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.network.chat.Component

@Environment(EnvType.CLIENT)
class NBTViewerScreen(
    description: NBTViewerGui,
) : CottonClientScreen(
    when (description.name) {
        null -> Component.translatable("text.nbtviewer.title")
        else -> Component.literal(description.name)
    },
    description
)
