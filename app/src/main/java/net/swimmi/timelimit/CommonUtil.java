package net.swimmi.timelimit;

/**
 * Created by swimmi on 2018/3/29.
 */

public class CommonUtil {

    public static String longToString(long time) {
        String timeStr = "";
        int hour = (int) (time / (1000 * 60 * 60));
        int minute = (int) (time / (1000 * 60)) % 60;
        int second = (int) (time / 1000) % 60;
        return String.format("%d时%d分%d秒", hour, minute, second);
    }
}
