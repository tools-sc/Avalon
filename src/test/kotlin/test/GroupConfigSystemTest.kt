package test

import avalon.tool.system.GroupConfigs
import org.apache.commons.lang3.ArrayUtils.toString

fun main(args: Array<String>) {
	val config = GroupConfigs.instance().getConfig(399863405)
	println(toString(config.admin))
	println(toString(config.blacklist))
	config.permissions.forEach { println(it) }
	println(config.isListen)
	println(config.isRecord)
}
