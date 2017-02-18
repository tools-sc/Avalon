package api;

import tool.ConstantPool;
import util.FriendMessage;
import util.GroupMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eldath on 2017/2/3 0003.
 *
 * @author Eldath
 */
public class Recorder {
    private static List<FriendMessage> friendMessageRecord = new ArrayList<>();
    private static List<GroupMessage> groupMessageRecord = new ArrayList<>();
    private static Recorder instance = null;
    private static final int MAX_RECODE_LIST_SIZE = 10;

    public static Recorder getInstance() {
        if (instance == null) instance = new Recorder();
        return instance;
    }

    void recodeGroupMessage(GroupMessage message) {
        groupMessageRecord.add(message);
        if (groupMessageRecord.size() > MAX_RECODE_LIST_SIZE)
            flushNow();
    }

    void recodeFriendMessage(FriendMessage message) {
        friendMessageRecord.add(message);
        if (friendMessageRecord.size() > MAX_RECODE_LIST_SIZE)
            flushNow();
    }

    public void flushNow() {
        for (GroupMessage thisGroupMessage : groupMessageRecord)
            ConstantPool.Database.currentDatabaseOperator.addGroupMessage(thisGroupMessage);
        groupMessageRecord.clear();
        for (FriendMessage thisFriendMessage : friendMessageRecord)
            ConstantPool.Database.currentDatabaseOperator.addFriendMessage(thisFriendMessage);
        friendMessageRecord.clear();
    }
}