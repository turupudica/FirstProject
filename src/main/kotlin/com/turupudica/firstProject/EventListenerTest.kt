package com.turupudica.firstProject

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class EventListenerTest(
    private val plugin: FirstProject,
) : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        event.joinMessage(Component.text("${event.player.name}がサーバーに参加しました。yaa", NamedTextColor.YELLOW))
        event.player.inventory.addItem(ItemStack.of(Material.APPLE))

    }

//    アイテムを使用したとき onItemConsumed
    @EventHandler
    fun onItemConsumed(event: PlayerItemConsumeEvent) {
        // リンゴ以外早期リターン
        if (event.item.type != Material.APPLE) {
            return
        }
        // 俊足の効果を付与
        event.player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 120, 2))
    }

//    ブロックを破壊したとき　onBlockBreak
    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        handleSpruceLog(event)
        handleIce(event)
    }

    /**
     * トウヒの原木を破壊したときの処理
     */
    private fun handleSpruceLog(event: BlockBreakEvent) {
        val block = event.block

        if (block.type != Material.SPRUCE_LOG) return

        // トウヒの原木に対する処理

        // アイテムのドロップをさせないようにする
//        event.isDropItems = false
        //
//        event.player.inventory.addItem(ItemStack.of(Material.STICK,1))

        // 壊さなかったことにする
//        event.isCancelled = true

        // ブロックを壊すと何もドロップせず、一定時間後に本来の壊した際のアイテムがドロップする
        val delayTicks = 100L
        if (event.block.type != Material.SPRUCE_LOG) {
            return
        }
        event.isDropItems = false
        val dropLocation = event.block.location.clone().add(0.5, 0.5, 0.5)
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            dropLocation.world?.dropItemNaturally(dropLocation, ItemStack.of(Material.SPRUCE_LOG))
        }, delayTicks)
    }

    private fun handleIce(event: BlockBreakEvent) {
        val block = event.block
        val delayTicks = 60L
        if (block.type != Material.ICE) return

        // 氷に対する処理
        // 通常のブロック破壊をキャンセルする
        event.isCancelled = true

        // 氷を岩盤に変更する
        block.type = Material.BEDROCK

        // 1秒後に氷へ戻す
        plugin.server.scheduler.runTaskLater(
            plugin,
            Runnable {
                // ほかの処理で変更されていない場合だけ復元する
                if (block.type == Material.BEDROCK) {
                    block.type = Material.ICE
                }
            },
            delayTicks
        )
    }


}