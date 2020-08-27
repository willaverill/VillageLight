package com.villagelight.app.util;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.villagelight.app.fragment.ScheduleSimpleFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    private static final SimpleDateFormat dsdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    private static final SimpleDateFormat tsdf = new SimpleDateFormat("h:mmaa", Locale.ENGLISH);
    private static final long TIMEINMILLIS = 24 * 60 * 60 * 1000;

    public static byte getXor(byte[] datas) {//除去帧头帧尾校验位之后异或运算

        byte temp = 0x00;

        for (int i = 1; i < datas.length - 2; i++) {
            temp ^= datas[i];
        }

        return temp;
    }

    public static byte[] getSendData(byte[] datas) {
        datas[2] = (byte) (datas.length - 3);
        datas[datas.length - 2] = Utils.getXor(datas);
        return datas;
    }

    public static boolean isTimeOK(String time1, String time2) {

        boolean isTimeOk = false;
        try {
            Date date1 = tsdf.parse(time1);
            Date date2 = tsdf.parse(time2);
            long timeOn = date1.getTime();
            long timeOff = date2.getTime();
            if (timeOff <= timeOn){
                isTimeOk = true;
            }else {
                long diff = timeOn - timeOff;
                if (diff >= ScheduleSimpleFragment.ValidTime){
                    isTimeOk = true;
                }
            }
//            return date2.getTime() > date1.getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return isTimeOk;
    }

    /**
     * 判断时间是否在时间段内
     *
     * @param time         System.currentTimeMillis()
     * @param strDateBegin 开始时间 00:00:00
     * @param strDateEnd   结束时间 00:05:00
     * @return
     */
    public static boolean isInTimeRange(long time, String strDateBegin, String strDateEnd) {

        try {

            String strDateNow = tsdf.format(new Date(time));
            Date now = tsdf.parse(strDateNow);
            Date date1 = tsdf.parse(strDateBegin);
            Date date2 = tsdf.parse(strDateEnd);

            time = now.getTime();
            long startTimeL = date1.getTime();
            long endTimeL = date2.getTime();

            return time >= startTimeL && time <= endTimeL;

        } catch (Exception e) {
            return false;
        }

    }

    /**
     * @param strDate
     * @return -1：小于 0：等于 1：大于
     */
    public static int judgeTime(String strDate) {

        try {

            String strDateNow = tsdf.format(new Date());
            Date now = tsdf.parse(strDateNow);
            Date date = tsdf.parse(strDate);

            long nowTime = now.getTime();
            long startTime = date.getTime();

            if (nowTime > startTime) {
                return 1;
            } else if (nowTime < startTime) {
                return -1;
            } else {
                return 0;
            }

        } catch (Exception e) {
            return 0;
        }

    }


    /**
     * @param time
     * @return -2:前天 -1：昨天 0：今天 1：明天 2：后天， 其他：其他日期
     */
    public static int judgeDay(long time) {
        int day = 0;
        try {
            Date date1 = dsdf.parse(dsdf.format(new Date()));
            Date date2 = dsdf.parse(dsdf.format(new Date(time)));

            long startTime = date1.getTime();

            long endTime = date2.getTime();

            //老的时间减去今天的时间
            long intervalMilli = endTime - startTime;

            day = (int) (intervalMilli / TIMEINMILLIS);


            LogUtils.d(endTime + "-" + startTime + "=" + intervalMilli + "\n"
                    + "today:" + dsdf.format(startTime) + ",other:" + dsdf.format(endTime) + ",between:" + day);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return day;

    }

    /**
     * 获取本地软件版本号名称
     */
    public static String getLocalVersionName(Context ctx) {
        String localVersion = "";
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }
}