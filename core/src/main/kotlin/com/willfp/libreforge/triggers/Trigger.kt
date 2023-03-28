package com.willfp.libreforge.triggers

import com.willfp.eco.core.registry.Registrable
import com.willfp.libreforge.ProvidedHolder
import com.willfp.libreforge.getProvidedActiveEffects
import com.willfp.libreforge.plugin
import com.willfp.libreforge.providedActiveEffects
import com.willfp.libreforge.triggers.DispatchedTrigger.Companion.inheritPlaceholders
import com.willfp.libreforge.triggers.event.TriggerDispatchEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener

abstract class Trigger(
    val id: String
) : Listener, Registrable {
    /**
     * The TriggerData parameters that are sent.
     */
    abstract val parameters: Set<TriggerParameter>

    /**
     * Dispatch the trigger.
     */
    protected fun dispatch(
        player: Player,
        data: TriggerData,
        forceHolders: Collection<ProvidedHolder>? = null
    ) {
        val dispatch = DispatchedTrigger(player, this, data)

        val dispatchEvent = TriggerDispatchEvent(player, dispatch)
        Bukkit.getPluginManager().callEvent(dispatchEvent)
        if (dispatchEvent.isCancelled) {
            return
        }

        val effects = forceHolders?.getProvidedActiveEffects(player) ?: player.providedActiveEffects

        for ((holder, blocks) in effects) {
            val withHolder = data.copy(holder = holder)
            val dispatchWithHolder = DispatchedTrigger(player, this, withHolder).inheritPlaceholders(dispatch)

            for (block in blocks) {
                block.tryTrigger(dispatchWithHolder)
            }
        }
    }

    final override fun onRegister() {
        if (plugin.isEnabled) {
            plugin.eventManager.registerListener(this)
            postRegister()
        } else {
            plugin.onEnable {
                plugin.eventManager.registerListener(this)
                postRegister()
            }
        }
    }

    open fun postRegister() {
        // Override when needed.
    }

    override fun getID() = id
}
