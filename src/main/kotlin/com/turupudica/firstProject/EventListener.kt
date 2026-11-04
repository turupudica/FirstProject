package com.turupudica.firstProject

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack

class EventListener(
    private val plugin: FirstProject,
) : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        event.joinMessage(Component.text("${event.player.name}がサーバーに参加しました。yaa", NamedTextColor.YELLOW))
        event.player.inventory.addItem(ItemStack.of(Material.APPLE))

    }


    // ========================================
    // 設定
    // ========================================

    // ① 効果を発生させるブロックの種類
    private val effectMaterial = Material.DIAMOND_ORE

    // ③ アイテムをドロップするまでの時間：5秒
    private val dropDelayTicks = 100L

    // ④ 岩盤から元に戻るまでの時間：1秒
    private val restoreDelayTicks = 20L

    // ② ブロック破壊をキャンセルする範囲
    private val cancelRange = BlockRange(
        worldName = "world",
        point1 = BlockPosition(0, 20, 0),
        point2 = BlockPosition(50, 120, 50)
    )

    // ③ 破壊後、5秒経過してからドロップする範囲
    private val dropRange = BlockRange(
        worldName = "world",
        point1 = BlockPosition(0, 20, 50),
        point2 = BlockPosition(50, 120, 100)
    )

    // ④ 一度ベッドロックにして、1秒後に元に戻す範囲
    private val restoreRange = BlockRange(
        worldName = "world",
        point1 = BlockPosition(0, 20, 100),
        point2 = BlockPosition(50, 120, 150)
    )

    // ========================================
    // 座標データ
    // ========================================

    private data class BlockPosition(
        val x: Int,
        val y: Int,
        val z: Int
    )

    private data class BlockRange(
        val worldName: String,
        val point1: BlockPosition,
        val point2: BlockPosition
    ) {
        fun contains(block: Block): Boolean {
            if (block.world.name != worldName) return false

            return block.x in minOf(point1.x, point2.x)..maxOf(point1.x, point2.x) &&
                    block.y in minOf(point1.y, point2.y)..maxOf(point1.y, point2.y) &&
                    block.z in minOf(point1.z, point2.z)..maxOf(point1.z, point2.z)
        }
    }

    // ========================================
    // ブロック破壊イベント
    // ========================================

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        // ① ブロックの種類による効果
        handleBlockEffect(event)

        // ② 破壊をキャンセル
        if (cancelRange.contains(event.block)) {
            event.isCancelled = true
            return
        }

        // ③ 遅延ドロップ
        if (dropRange.contains(event.block)) {
            handleDelayedDrop(event)// 秒数、ブロック、範囲を引数で指定
            return
        }

        // ④ ベッドロックに変更してから復元
        if (restoreRange.contains(event.block)) {
            handleBedrockRestore(event)
            return
        }

        // どの範囲にも一致しなければ終了
        return
    }

    // ========================================
    // ① ブロックの種類による効果
    // ========================================

    private fun handleBlockEffect(event: BlockBreakEvent) {
        if (event.block.type != effectMaterial) return

        event.player.sendMessage(
            "指定したブロックを破壊しました！"
        )
    }

    // ========================================
    // ③ 遅延ドロップ
    // ========================================

    private fun handleDelayedDrop(event: BlockBreakEvent) {
        val delayTicks = 100L
        val selectBlock = Material.SPRUCE_LOG

        if (event.block.type != selectBlock) {
            return
        }
        event.isDropItems = false
        val dropLocation = event.block.location.toCenterLocation()
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            dropLocation.world?.dropItemNaturally(dropLocation, ItemStack.of(selectBlock))
        }, delayTicks)
    }

    // ========================================
    // ④ 岩盤化と復元
    // ========================================

    private fun handleBedrockRestore(event: BlockBreakEvent) {
        val block = event.block
        val delayTicks = 60L
        val selectBlock = Material.ICE

        if (block.type != selectBlock) return

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
                    block.type = selectBlock
                }
            },
            delayTicks
        )
    }


}