package marioandweegee3.nbtviewer.gui

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.widget.*
import io.github.cottonmc.cotton.gui.widget.data.Insets
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.nbt.*
import net.minecraft.network.chat.Component

@Environment(EnvType.CLIENT)
class NBTViewerGui private constructor(
    nbt: Tag,
    private val enclosing: NBTViewerGui?,
    val name: String?,
) : LightweightGuiDescription() {
    init {
        val root = WGridPanel()
        rootPanel = root

        with(root) {
            insets = Insets.ROOT_PANEL

            val children = when (nbt) {
                is CompoundTag -> {
                    buildList {
                        val seq = nbt
                            .keySet()
                            .asSequence()
                            .sortedWith { a, b -> String.CASE_INSENSITIVE_ORDER.compare(a, b) }
                            .map { it to nbt[it]!! }

                        for ((k, v) in seq) {
                            add(getWidgetForElement(k, v))
                        }
                    }
                }

                is CollectionTag -> {
                    buildList {
                        for ((index, value) in nbt.withIndex()) {
                            add(getWidgetForElement(index.toString(), value))
                        }
                    }
                }

                else -> emptyList()
            }

            add(
                WListPanel(children, ::WGridPanel) { w, p ->
                    p.add(w, 0, 0, 14, 1)
                },
                0, 1, 15, 9
            )

            val button = if (enclosing !== null) {
                WButton(Component.translatableWithFallback("text.nbtviewer.back", "Back"))
                    .setOnClick {
                        Minecraft
                            .getInstance()
                            .gui.setScreen(NBTViewerScreen(enclosing))
                    }
            } else {
                WButton(Component.translatableWithFallback("text.nbtviewer.close", "Close"))
                    .setOnClick {
                        Minecraft
                            .getInstance()
                            .gui.setScreen(null)
                    }
            }

            add(button, 12, 0, 2, 1)
        }

        root.validate(this)
    }

    constructor(nbt: CompoundTag) : this(nbt as Tag, null, null)

    private fun getWidgetForElement(index: String, element: CollectionTag): WWidget {
        val button = WButton(Component.translatable("text.nbtviewer.element.list", index))
        val name = getSubScreenName(index)
        button.setOnClick {
            Minecraft
                .getInstance()
                .gui.setScreen(NBTViewerScreen(NBTViewerGui(element, this, name)))
        }
        return button
    }

    private fun getWidgetForElement(index: String, element: Tag): WWidget {
        var widget: WWidget = WLabel(Component.literal("undefined for type"))

        val enclosing = this

        element.accept(object : TagVisitor {
            override fun visitString(element: StringTag) {
                widget = WButton(
                    Component.translatable("text.nbtviewer.element.string", index, element.asString().orElse(""))
                )
            }

            override fun visitByte(element: ByteTag) {
                widget = WButton(
                    Component.translatable("text.nbtviewer.element.int", index, element.intValue())
                )
            }

            override fun visitShort(element: ShortTag) {
                widget = WButton(
                    Component.translatable("text.nbtviewer.element.int", index, element.intValue())
                )
            }

            override fun visitInt(element: IntTag) {
                widget = WButton(
                    Component.translatable("text.nbtviewer.element.int", index, element.intValue())
                )
            }

            override fun visitLong(element: LongTag) {
                widget = WButton(
                    Component.translatable("text.nbtviewer.element.int", index, element.longValue())
                )
            }

            override fun visitFloat(element: FloatTag) {
                widget = WButton(
                    Component.translatable("text.nbtviewer.element.float", index, element.doubleValue())
                )
            }

            override fun visitDouble(element: DoubleTag) {
                widget = WButton(
                    Component.translatable("text.nbtviewer.element.float", index, element.doubleValue())
                )
            }

            override fun visitByteArray(element: ByteArrayTag) {
                widget = getWidgetForElement(index, element)
            }

            override fun visitIntArray(element: IntArrayTag) {
                widget = getWidgetForElement(index, element)
            }

            override fun visitLongArray(element: LongArrayTag) {
                widget = getWidgetForElement(index, element)
            }

            override fun visitList(element: ListTag) {
                widget = getWidgetForElement(index, element)
            }

            override fun visitCompound(compound: CompoundTag) {
                val button = WButton(Component.translatable("text.nbtviewer.element.object", index))
                val name = getSubScreenName(index)
                button.setOnClick {
                    Minecraft
                        .getInstance()
                        .gui.setScreen(NBTViewerScreen(NBTViewerGui(compound, enclosing, name)))
                }
                widget = button
            }

            override fun visitEnd(element: EndTag) {
            }

        })

        return widget
    }

    private fun getSubScreenName(sub: String): String {
        val name = if (name === null) sub else "$name/$sub"
        return if (name.length >= 40) {
            "...${name.substring(name.length - 37)}"
        } else {
            name
        }
    }
}
