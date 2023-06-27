package net.arvandor.talekeeper.scheduler

import org.bukkit.plugin.java.JavaPlugin

fun asyncTask(plugin: JavaPlugin, task: () -> Unit) = plugin.server.scheduler.runTaskAsynchronously(plugin, task)
fun syncTask(plugin: JavaPlugin, task: () -> Unit) = plugin.server.scheduler.runTask(plugin, task)
