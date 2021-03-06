/*
  use Kotlin version instead.
 */
//package avalon.group;
//
//import avalon.api.CustomGroupResponder;
//import avalon.extend.Recorder;
//import avalon.main.MainServer;
//import avalon.main.MessageChecker;
//import avalon.tool.APIRateLimit;
//import avalon.tool.ObjectCaster;
//import avalon.tool.pool.APISurvivePool;
//import avalon.tool.pool.AvalonPluginPool;
//import avalon.tool.pool.Constants;
//import avalon.tool.pool.Variables;
//import avalon.tool.system.Configs;
//import avalon.tool.system.GroupConfigs;
//import avalon.tool.system.RunningData;
//import avalon.util.ConfigurationError;
//import avalon.util.GroupMessage;
//import org.apache.commons.lang3.ArrayUtils;
//import org.json.JSONObject;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.*;
//import java.util.regex.Pattern;
//import java.util.stream.LongStream;
//
//import static avalon.api.Flag.AT;
//import static avalon.api.RegisterResponder.register;
//import static avalon.tool.ObjectCaster.toStringArray;
//import static avalon.tool.pool.Constants.Basic.DEBUG_MESSAGE_UID;
//
//*
// * Created by Eldath Ray on 2017/3/30.
// *
// * @author Eldath Ray
//
//
//public class GroupMessageHandler {
//	private static final Map<Pattern, GroupMessageResponder> apiList = new LinkedHashMap<>();
//	private static final Map<String, GroupMessageResponder> apiNameMap = new HashMap<>();
//	private static final Map<Pattern, CustomGroupResponder> customApiList = new LinkedHashMap<>();
//	private static final Map<? super GroupMessageResponder, Boolean> enableMap = new HashMap<>();
//	private static final Map<Long, Integer> publishPeopleMap = new HashMap<>();
//
//	private static final List<GroupMessageResponder> disableNotAllowedResponder = new ArrayList<>();
//
//	private static final String[] blockWordList = toStringArray(Configs.Companion.instance().getConfigArray("block_words"));
//	public static final int punishFrequency = (int) Configs.Companion.instance().get("block_words_punish_frequency");
//	private static final long coolingDuration = ObjectCaster.toLong(Configs.INSTANCE.get("cooling_duration"));
//
//	private static final APIRateLimit cooling = new APIRateLimit(coolingDuration);
//	private static GroupMessageHandler instance = new GroupMessageHandler();
//	private static final Logger LOGGER = LoggerFactory.getLogger(GroupMessageHandler.class);
//
//	public static Map<Pattern, GroupMessageResponder> getApiList() {
//		return apiList;
//	}
//
//	public static GroupMessageHandler getInstance() {
//		return instance;
//	}
//
//	static {
//		 * 指令优先级排序依据：单词 >> 多词，管理类 >> 服务类 >> 娱乐类，触发类 >> 自由类
//
//
//		// 管理类
//		register(Shutdown.INSTANCE, false);
//		register(Flush.INSTANCE, false);
//		register(Manager.INSTANCE, false);
//		register(Blacklist.INSTANCE, false);
//		register(Quote.INSTANCE, false);
//		// 服务类
//		register(Help.INSTANCE, true);
//		register(Version.INSTANCE, true);
//		register(ShowAdmin.INSTANCE, false);
//		register(Echo.INSTANCE, false);
//		register(ExecuteInfo.INSTANCE, false);
//		register(Execute.INSTANCE, false);
//		// 娱乐类
//		register(Wolfram.INSTANCE, false);
//		register(Hitokoto.INSTANCE, false);
//		register(Mo.INSTANCE, false);
//		register(AnswerMe.INSTANCE, false);
//	}
//
//	static {
//		JSONObject object = Configs.INSTANCE.getJSONObject("responders");
//		String[] enable = toStringArray(object.getJSONArray("enable").toList().toArray());
//		String[] disable = toStringArray(object.getJSONArray("disable").toList().toArray());
//
//
//		for (String thisDisable : disable)
//			enableMap.put(apiNameMap.get(thisDisable), false);
//
//		for (String thisEnable : enable) {
//			GroupMessageResponder thisEnableResponder = apiNameMap.get(thisEnable);
//			if (!enableMap.containsKey(thisEnableResponder))
//				enableMap.put(thisEnableResponder, true);
//		}
//
//		for (GroupMessageResponder responder : apiList.values()) {
//			if (!enableMap.containsKey(responder))
//				enableMap.put(responder, false);
//		}
//
//		// 校验
//		for (Map.Entry<? super GroupMessageResponder, Boolean> entry : enableMap.entrySet()) {
//			//noinspection SuspiciousMethodCalls
//			if (!entry.getValue() && disableNotAllowedResponder.contains(entry.getKey())) {
//				throw new ConfigurationError("CAN NOT disabled basic responder: `" + entry.getKey().getClass().getSimpleName() + "`. Please:\n\t1. Remove this responder from entry `responders.disable` in file `config.json`.\n\t2. Add it into `responders.enable` in file`config.json`.\n\t3. Restart the program.");
//			}
//		}
//	}
//
//	GroupMessageResponder getGroupResponderByKeywordRegex(String keyword) {
//		for (Map.Entry<Pattern, GroupMessageResponder> patternAPIEntry : apiList.entrySet()) {
//			Pattern key = patternAPIEntry.getKey();
//			GroupMessageResponder value = patternAPIEntry.getValue();
//			if (key.matcher(keyword).find())
//				return value;
//		}
//		return null;
//	}
//
//	public boolean isResponderEnable(GroupMessageResponder api) {
//		if (!enableMap.containsKey(api))
//			return true;
//		return enableMap.get(api);
//	}
//
//	public void handle(GroupMessage message) {
//		long groupUid = message.getGroupUid();
//		String sender = message.getSenderNickName();
//		long senderUid = message.getSenderUid();
//
//		avalon.util.GroupConfig groupConfig = GroupConfigs.instance().getConfig(groupUid);
//		if (groupConfig == null) {
//			LOGGER.warn("listened message from not configured group " +
//					groupUid + " . Ignored this message. Please config this group in `.\\group.json`.");
//			return;
//		}
//		if (groupConfig.isRecord() && !Constants.Basic.DEBUG)
//			Recorder.getInstance().recodeGroupMessage(message);
//		if (!groupConfig.isListen())
//			return;
//		if (ArrayUtils.contains(groupConfig.getBlacklist(), senderUid))
//			return;
//
//		LongStream adminUidStream = Arrays.stream(groupConfig.getAdmin());
//		boolean admin = adminUidStream.anyMatch(e -> e == senderUid);
//
//		if (Constants.Setting.Block_Words_Punishment_Mode_Enabled)
//			if (publishPeopleMap.containsKey(senderUid)) {
//				if (publishPeopleMap.get(senderUid) >= punishFrequency) {
//					LOGGER.info("Account " + senderUid + ":" + sender + " was blocked. Please entered " +
//							"\"Avalon blacklist remove " + senderUid + "\" to the group " + groupUid + ":" +
//							message.getGroupName() + " if you really want to unblock this account.");
//					if (!admin)
//						message.response(AT(message) + " 您的帐号由于发送过多不允许关键字，现已被屏蔽~o(╯□╰)o！");
//					return;
//				}
//			} else
//				publishPeopleMap.put(senderUid, 0);
//
//		for (Map.Entry<Pattern, GroupMessageResponder> patternAPI : apiList.entrySet()) {
//			GroupMessageResponder value = patternAPI.getValue();
//			ResponderInfo info = value.responderInfo();
//
//			if (patternCheck(patternAPI.getKey(), message)) {
//				if (!APISurvivePool.getInstance().isSurvive(value)) {
//					if (!APISurvivePool.getInstance().isNoticed(value)) {
//						if (!info.getKeyWordRegex().matcher("+1s").find())
//							message.response(AT(message) + " 对不起，您调用的指令响应器目前已被停止；注意：此消息仅会显示一次。");
//						APISurvivePool.getInstance().setNoticed(value);
//					}
//				} else if (MessageChecker.check(message) &&
//						isResponderEnable(value) &&
//						permissionCheck(info.getPermission(), groupConfig, message))
//					value.doPost(message, groupConfig);
//				return;
//			}
//		}
//
//		for (Map.Entry<Pattern, CustomGroupResponder> patternAPIEntry : customApiList.entrySet()) {
//			CustomGroupResponder value = patternAPIEntry.getValue();
//			if (patternCheck(patternAPIEntry.getKey(), message)) {
//				if (MessageChecker.check(message))
//					value.doPost(message, groupConfig);
//				return;
//			}
//		}
//	}
//
//	private boolean permissionCheck(ResponderPermission permission, avalon.util.GroupConfig config, GroupMessage message) {
//		long senderUid = message.getSenderUid();
//		boolean result = false;
//
//		if (senderUid == DEBUG_MESSAGE_UID)
//			return true;
//
//		if (permission == ResponderPermission.ADMIN)
//			result = ArrayUtils.contains(config.getAdmin(), senderUid);
//		else if (permission == ResponderPermission.OWNER)
//			result = config.getOwner() == senderUid;
//		else if (permission == ResponderPermission.ALL)
//			result = true;
//
//		if (!result)
//			message.response(AT(message) + " 致命错误：需要`sudo`以执行此操作！（雾");
//		return result;
//	}
//
//	private boolean patternCheck(Pattern key, GroupMessage groupMessage) {
//		String lowerContent = groupMessage.getContent().toLowerCase();
//		long time = groupMessage.getTimeLong();
//		if (!key.matcher(lowerContent).find())
//			return false;
//		// 屏蔽词判断
//		for (String thisBlockString : blockWordList)
//			if (groupMessage.getContent().
//					trim().
//					toLowerCase().
//					replaceAll("[\\pP\\p{Punct}]", "").contains(thisBlockString)) {
//				String notice = "您发送的消息含有不允许的关键词！";
//				if (Constants.Setting.Block_Words_Punishment_Mode_Enabled) {
//					notice = "您发送的消息含有不允许的关键词，注意：" + punishFrequency +
//							"次发送不允许关键词后帐号将被屏蔽！⊙﹏⊙!";
//					blackListPlus(groupMessage.getSenderUid());
//				}
//				groupMessage.response(AT(groupMessage) + " " + notice);
//				return false;
//			}
//		// 冷却判断
//		if (!cooling.trySet(time)) {
//			if (!Variables.Limit_Noticed) {
//				if (key.matcher("+1s").find())
//					return false;
//				groupMessage.response(
//						String.format(
//								"%s 对不起，您的指令超频。%dms内仅能有一次指令输入，未到%dms内的输入将被忽略。注意：此消息仅会显示一次。",
//								AT(groupMessage),
//								coolingDuration,
//								coolingDuration
//						));
//				Variables.Limit_Noticed = true;
//			}
//			LOGGER.info(
//					String.format("cooling blocked message %d sent by %d in %s.",
//							groupMessage.getId(), groupMessage.getSenderUid(), groupMessage.getGroupName()));
//			return false;
//		}
//		return true;
//	}
//
//	public static void main(String[] args) {
//		System.setProperty("file.encoding", "UTF-8");
//
//		Runtime.getRuntime().addShutdownHook(new MainServer.ShutdownHook());
//
//		// FIXME 感觉可以删掉，因为是自动加载的
//		Configs.Companion.instance();
//		RunningData.getInstance();
//		new Constants.Basic();
//		new Constants.Address();
//		AvalonPluginPool.INSTANCE.load();
//		if (!Constants.Basic.DEBUG) {
//			System.err.println("Debug not on! Exiting...");
//			return;
//		}
//		Scanner scanner = new Scanner(System.in);
//		int id = 0;
//		//noinspection InfiniteLoopStatement
//		while (true) {
//			System.out.print("Input here:");
//			String content = scanner.nextLine();
//			GroupMessage message = new GroupMessage(++id, System.currentTimeMillis(),
//					DEBUG_MESSAGE_UID, "Test", 617118724, "Test Group", content);
//			GroupMessageHandler.getInstance().handle(message);
//			System.out.println("===");
//		}
//	}
//
//	private void blackListPlus(long senderUid) {
//		int pastValue = publishPeopleMap.get(senderUid);
//		publishPeopleMap.put(senderUid, ++pastValue);
//	}
//
//	public static void addGroupMessageResponder(GroupMessageResponder responder) {
//		apiList.put(responder.responderInfo().getKeyWordRegex(), responder);
//		apiNameMap.put(responder.getClass().getSimpleName(), responder);
//	}
//
//	public static void addCustomGroupResponder(CustomGroupResponder responder) {
//		customApiList.put(responder.getKeyWordRegex(), responder);
//	}
//
//	public static void setDisabledNotAllowed(GroupMessageResponder responder) {
//		disableNotAllowedResponder.add(responder);
//	}
//
//	static Map<Pattern, CustomGroupResponder> getCustomApiList() {
//		return customApiList;
//	}
//
//static Map<Long, Integer> getSetBlackListPeopleMap() {
//	return publishPeopleMap;
//}
//}
