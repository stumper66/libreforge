package com.willfp.libreforge

import com.willfp.eco.util.NamespacedKeyUtils
import com.willfp.eco.util.NumberUtils
import com.willfp.libreforge.conditions.ConditionList
import com.willfp.libreforge.effects.EffectList
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import java.util.Objects

/**
 * A holder 'holds' a list of effects and a list of conditions.
 * This is essentially the core of libreforge, and implementations
 * are things like Talismans, Enchantment Levels, Items, etc.
 */
interface Holder {
    /**
     * The ID of the holder, must be unique.
     */
    val id: NamespacedKey

    /**
     * The effects.
     */
    val effects: EffectList

    /**
     * The conditions.
     */
    val conditions: ConditionList
}

/**
 * A blank holder is a holder with no effects or conditions.
 *
 * It's used in triggers in order to be able to provide an
 * empty provided holder so the holders can then be attached
 * to a copy of the data before it's processed by effects.
 */
object BlankHolder : Holder {
    override val id = plugin.namespacedKeyFactory.create("blank")

    override val effects = EffectList(emptyList())
    override val conditions = ConditionList(emptyList())
}

/**
 * A provided holder is a holder with the item that has provided it,
 * i.e. The physical ItemStack that has the enchantment on it.
 */
interface ProvidedHolder<T> {
    /**
     * The holder.
     */
    val holder: Holder

    /**
     * The item.
     */
    val item: T

    // Destructuring support
    operator fun component1() = holder
    operator fun component2() = item
}

/**
 * An empty provided holder is a provided holder with no item.
 *
 * Used internally to provide a default value for TriggerData.
 */
object EmptyProvidedHolder : ProvidedHolder<Nothing?> {
    override val holder = BlankHolder
    override val item: Nothing? = null
}

/**
 * Provided holder for nothing.
 */
class SimpleProvidedHolder(
    override val holder: Holder
) : ProvidedHolder<Nothing?> {
    override val item: Nothing? = null
}

/**
 * A provided holder for an ItemStack.
 */
class ItemProvidedHolder(
    override val holder: Holder,
    override val item: ItemStack
) : ProvidedHolder<ItemStack>

/**
 * A template that a may create a holder when given an ID.
 */
data class HolderTemplate(
    val effects: EffectList,
    val conditions: ConditionList
) {
    /**
     * Create a holder from the template.
     */
    fun toHolder() = toHolder(
        NamespacedKeyUtils.createEcoKey("template_${NumberUtils.randInt(0, 1000000)}")
    )

    /**
     * Create a holder with a given key from the template.
     */
    fun toHolder(key: NamespacedKey): Holder = HolderFromTemplate(
        effects,
        conditions,
        key
    )

    private class HolderFromTemplate(
        override val effects: EffectList,
        override val conditions: ConditionList,
        override val id: NamespacedKey
    ) : Holder {
        override fun equals(other: Any?): Boolean {
            if (other !is Holder) {
                return false
            }

            return other.id == this.id
        }

        override fun hashCode(): Int {
            return Objects.hash(this.id)
        }
    }
}