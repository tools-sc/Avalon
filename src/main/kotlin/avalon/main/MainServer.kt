package avalon.main

import avalon.api.DelayResponse
import avalon.extend.RSSFeeder
import avalon.extend.Recorder
import avalon.extend.Scheduler
import avalon.extend.ShowMsg
import avalon.friend.FriendMessageHandler
import avalon.group.GroupMessageHandler
import avalon.group.Hitokoto
import avalon.tool.pool.AvalonPluginPool
import avalon.tool.pool.Constants
import avalon.tool.pool.Constants.Basic.CURRENT_SERVLET
import avalon.tool.system.Configs
import avalon.tool.system.GroupConfigs
import avalon.tool.system.RunningData
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created by Eldath on 2017/1/28 0028.
 *
 * @author Eldath
 */
object MainServer {
	private val logger = LoggerFactory.getLogger(MainServer::class.java)
	private val followGroup = GroupConfigs.instance().followGroups

	class ShutdownHook : Thread() {
		override fun run() {
			logger.info("Catch INT signal, Bye!")
			println("If you have some problems you CAN NOT SOLVE, please visit `https://github.com/Ray-Eldath/Avalon/issues` or contact with Ray-Eldath<ray-eldath@aol.com>.")
			Recorder.getInstance().flushNow()
			RunningData.getInstance().save()
			//
			for (thisFollowFollow in followGroup)
				CURRENT_SERVLET.responseGroup(thisFollowFollow, "服务已经停止。")
			CURRENT_SERVLET.shutdown()
			Constants.Database.CURRENT_DATABASE_OPERATOR.close()
			CURRENT_SERVLET.clean()
		}
	}

	@Throws(Exception::class)
	@JvmStatic
	fun main(args: Array<String>) {
		// 字符集处理
		System.setProperty("file.encoding", "UTF-8")
		// debug检测
		if (Constants.Basic.DEBUG)
			logger.warn("Avalon is running under DEBUG mode!")

		//		if (!CURRENT_SERVLET.test()) {
		//			logger.error("can not connect to servlet " + CURRENT_SERVLET.name() + "! please check this servlet is DO running...");
		//			System.exit(-1);
		//		}
		// 响应速度太慢。

		Configs.Companion.instance()
		RunningData.getInstance()
		Constants.Basic()
		Constants.Address()
		AvalonPluginPool.load()
		// 线程池
		ShowMsg()
		val executor = ScheduledThreadPoolExecutor(5)
		executor.scheduleAtFixedRate(Scheduler(), 6, 5, TimeUnit.SECONDS)

		if (Constants.Setting.RSS_Enabled)
			executor.scheduleAtFixedRate(RSSFeeder, 2, 10, TimeUnit.MINUTES)
		// 关车钩子
		Runtime.getRuntime().addShutdownHook(ShutdownHook())
		val address: InetSocketAddress
		val addressString = CURRENT_SERVLET.listenAddress().replace("http://", "")
		address = if (!addressString.contains(":")) InetSocketAddress(addressString, 8000)
		else {
			val addressStringArray = addressString.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
			InetSocketAddress(addressStringArray[0].replace("//", ""),
					Integer.parseInt(addressStringArray[1]))
		}
		//
		val server = Server(address)
		val context = ServletContextHandler(ServletContextHandler.SESSIONS)
		server.handler = context
		server.stopAtShutdown = true

		CURRENT_SERVLET.setGroupMessageReceivedHook { GroupMessageHandler.handle(it) }
		CURRENT_SERVLET.setFriendMessageReceivedHook { FriendMessageHandler.handle(it) }

		context.addServlet(ServletHolder(CURRENT_SERVLET), "/post_url")
		server.join()
		server.start()

		logger.info("Is server on (y or n, default n, await for 5 seconds): ")
		val isOn = readInput()
		when (isOn) {
			1 -> {
				for (thisFollowGroup in followGroup) {
					var str = "Avalon已经上线。\n发送`avalon help`以获取帮助信息。"
					val config = Configs.getResponderConfig("Hitokoto", "push_when_start")
					if (config != null && config as Boolean)
						str += "\n\n" + Hitokoto.Hitokoto.get()
					CURRENT_SERVLET.responseGroup(thisFollowGroup, str)
				}
				logger.info("Login message sent.")
			}
			0 -> logger.info("Cancel send login message.")
			else -> logger.info("Invalid input or reached the maximum waiting time, use default value: `n`.")
		}
		DelayResponse().start()
		logger.info("DelayResponse thread is loaded.")
		logger.info("Server now running!")
		logger.info("IMPORTANCE: Please exit this system by pressed Ctrl-C, DO NOT close this window directly!")
	}

	@Throws(IOException::class, InterruptedException::class)
	private fun readInputStreamWithTimeout(`is`: InputStream, buf: ByteArray): Int {
		var bufferOffset = 0
		val maxTimeMillis = System.currentTimeMillis() + 5000
		while (System.currentTimeMillis() < maxTimeMillis && bufferOffset < buf.size) {
			val readLength = Math.min(`is`.available(), buf.size - bufferOffset)
			val readResult = `is`.read(buf, bufferOffset, readLength)
			if (readResult == -1)
				break
			bufferOffset += readResult
			if (readResult > 0)
				break
			Thread.sleep(10)
		}
		return bufferOffset
	}


	@Throws(IOException::class, InterruptedException::class)
	private fun readInput(): Int {
		var result = 0
		val inputData = ByteArray(1)
		val readLength = readInputStreamWithTimeout(System.`in`, inputData)
		if (readLength > 0)
			when (inputData[0].toChar()) {
				'y', 'Y' -> result = 1
				else -> {
				}
			}
		else result = -1
		return result
	}
}