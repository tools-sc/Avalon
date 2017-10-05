package avalon.group

import avalon.api.Flag.AT
import avalon.tool.pool.Constants
import avalon.util.GroupConfig
import avalon.util.GroupMessage
import java.util.regex.Pattern

object Quote : GroupMessageResponder() {
	override fun doPost(message: GroupMessage, groupConfig: GroupConfig) {
		if (message.senderUid !in groupConfig.admin) {
			message.response("${AT(message)} 致命错误：需要`sudo`已执行此操作！（雾")
			return
		}
		val split = message.content.replace("avalon quote ", "").split(" ")
		val speaker = split[0]
		val content = split[1]
		Constants.Database.currentDatabaseOperator.addQuote(message.time, speaker, content)
		message.response("${AT(message)} 给定的语录已添加至数据库~")
	}

	override fun getHelpMessage(): String = "avalon quote <发言者> <语录内容>：<管理员> 记录语录到Avalon数据库。"

	override fun getKeyWordRegex(): Pattern = Pattern.compile("^avalon quote \\S+ \\S+")

	override fun instance(): GroupMessageResponder? = this
}