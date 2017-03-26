package command;

import main.MainServlet;
import org.slf4j.LoggerFactory;
import util.GroupMessage;

import java.util.regex.Pattern;

/**
 * Created by Eldath Ray on 2017/3/25 0025.
 *
 * @author Eldath Ray
 */
public class GShutdown extends BaseGroupMessageCommandRunner {
    private static GShutdown instance = null;

    public static GShutdown getInstance() {
        if (instance == null) instance = new GShutdown();
        return instance;
    }

    @Override
    public void doPost(GroupMessage message) {
        long[] admins = MainServlet.getAdminUid();
        for (long admin : admins)
            if (admin == message.getSenderUid()) {
                System.exit(0);
                LoggerFactory.getLogger(GShutdown.class).warn("Avalon is stopped remotely by " +
                        message.getSenderUid() + " : " + message.getSenderNickName() + " on " +
                        message.getGroupUid() + " : " + message.getGroupName() + " at " +
                        message.getTime().toString().replace("T", " "));
            }
    }

    @Override
    public String getHelpMessage() {
        return "avalon shutdown/exit：<管理员> 退出Avalon。";
    }

    @Override
    public Pattern getKeyWordRegex() {
        return Pattern.compile("avalon shutdown|avalon exit");
    }
}
