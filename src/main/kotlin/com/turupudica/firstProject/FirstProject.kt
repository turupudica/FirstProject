package com.turupudica.firstProject

import org.bukkit.plugin.java.JavaPlugin

class FirstProject : JavaPlugin() {

    override fun onEnable() {
        // Plugin startup logic

        server.pluginManager.registerEvents(EventListener(this), this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
