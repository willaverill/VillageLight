package com.villagelight.app;

import android.app.Application;
import android.util.Log;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.log.CustomLogger;
import com.tencent.bugly.crashreport.CrashReport;
import com.villagelight.app.model.ControllerBean;
import com.villagelight.app.model.CustomSchedule;
import com.villagelight.app.model.SimpleSchedule;
import com.villagelight.app.model.ThemeColor;

import java.util.ArrayList;
import java.util.List;

import cn.fly2think.blelib.AppManager;


public class ProjectApp extends Application {

    private static ProjectApp mInstance;
    public AppManager manager = null;
    private int serialNumber = -1;
    private DbUtils db;
    private ControllerBean duplicateController;
    private ControllerBean currentControl;
    private JobManager jobManager;
    private List<ThemeColor> defaultThemeColors = new ArrayList<>();
    private List<ThemeColor> syncThemeColors = new ArrayList<>();
    private boolean isDebug = false;
    private int syncBulbs;
    private int syncSwitches;
    private List<SimpleSchedule> syncSimpleSchedule = new ArrayList<>();
    private List<CustomSchedule> syncCustomSchedule = new ArrayList<>();
    private boolean isCustomSchedule = false;

    private List<ThemeColor> duplThemeColors = new ArrayList<>();
    private List<SimpleSchedule> duplSimpleSchedule = new ArrayList<>();
    private List<CustomSchedule> duplCustomSchedule = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        manager = new AppManager(getApplicationContext());
        manager.setConnectTimeout(5000);
        CrashReport.initCrashReport(getApplicationContext(), "04a788b03e", false);

        //蓝牙UUID初始化
        if (isDebug) {
            manager.setReadServiceUUID("0000fff0-0000-1000-8000-00805f9b34fb");
            manager.setWriteServiceUUID("0000fff0-0000-1000-8000-00805f9b34fb");
            manager.setNotifyUUID("0000fff1-0000-1000-8000-00805f9b34fb");
            manager.setWriteUUID("0000fff1-0000-1000-8000-00805f9b34fb");

        } else {
            manager.setReadServiceUUID("00001000-0000-1000-8000-00805f9b34fb");
            manager.setWriteServiceUUID("00001000-0000-1000-8000-00805f9b34fb");
            manager.setNotifyUUID("00001002-0000-1000-8000-00805f9b34fb");
            manager.setWriteUUID("00001001-0000-1000-8000-00805f9b34fb");
        }

        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .tag("VillageLight")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();

        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));

        Configuration configuration = new Configuration.Builder(this).customLogger(new CustomLogger() {
            private static final String TAG = "JOBS";

            @Override
            public boolean isDebugEnabled() {
                return false;
            }

            @Override
            public void d(String text, Object... args) {
                Log.d(TAG, String.format(text, args));
            }

            @Override
            public void e(Throwable t, String text, Object... args) {
                Log.e(TAG, String.format(text, args), t);
            }

            @Override
            public void e(String text, Object... args) {
                Log.e(TAG, String.format(text, args));
            }
        }).minConsumerCount(1)// always keep at least one consumer alive
                .maxConsumerCount(1)// up to 3 consumers at a time
                .loadFactor(1)// 3 jobs per consumer
                .consumerKeepAlive(120)// wait 2 minute
                .build();
        jobManager = new JobManager(this, configuration);


        ThemeColor themeColor;

        themeColor = new ThemeColor();
        themeColor.setName("All Warm Clear");
        themeColor.setId(1);
        themeColor.setFade(0);
        defaultThemeColors.add(themeColor);

        themeColor = new ThemeColor();
        themeColor.setName("All Winter White");
        themeColor.setId(2);
        themeColor.setFade(0);
        defaultThemeColors.add(themeColor);

        themeColor = new ThemeColor();
        themeColor.setName("All Red");
        themeColor.setId(3);
        themeColor.setFade(0);
        defaultThemeColors.add(themeColor);

        themeColor = new ThemeColor();
        themeColor.setName("All Green");
        themeColor.setId(4);
        themeColor.setFade(0);
        defaultThemeColors.add(themeColor);

    }

    /**
     * https://www.jianshu.com/p/f665366b2a47
     */

    // 程序终止的时候执行
    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    // 低内存的时候执行
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    // 程序在内存清理的时候执行（回收内存）
    // HOME键退出应用程序、长按MENU键，打开Recent TASK都会执行
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    public JobManager getJobManager() {
        return jobManager;
    }


    public void stopJob() {

        jobManager.clear();
        jobManager.stop();

    }


    public void clearJob() {

        jobManager.clear();

    }

    public void startJob() {

        jobManager.start();

    }

    public static ProjectApp getInstance() {
        return mInstance;
    }

    public int getSerialNumber() {

        if (serialNumber < 255) {
            serialNumber++;
        } else {
            serialNumber = 0;
        }

        return serialNumber;
    }

    //数据库初始化
    public DbUtils getDb() {
        if (db == null) {
            DbUtils.DaoConfig daoConfig = new DbUtils.DaoConfig(this);
            daoConfig.setDbName("data.db");
            daoConfig.setDbVersion(10);
            daoConfig.setDbUpgradeListener(new DbUtils.DbUpgradeListener() {
                @Override
                public void onUpgrade(DbUtils dbUtils, int oldVer, int newVer) {

                    if (newVer == 10) {
                        try {
                            dbUtils.execNonQuery("ALTER TABLE Controller RENAME TO ControllerBak;");
                            dbUtils.execNonQuery("CREATE TABLE Controller(id INTEGER PRIMARY KEY AUTOINCREMENT, controllerName TEXT ,deviceName TEXT,deviceMac TEXT,password TEXT,year INTEGER);");
                            dbUtils.execNonQuery("INSERT INTO Controller(id,controllerName,deviceName,deviceMac,password) SELECT id,controllerName,deviceName,deviceMac,password FROM ControllerBak;");
                            dbUtils.execNonQuery("DROP TABLE ControllerBak;");
                        } catch (DbException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            db = DbUtils.create(daoConfig);
        }
        return db;
    }

    public ControllerBean getDuplicateController() {
        return duplicateController;
    }

    public void setDuplicateController(ControllerBean duplicateController) {
        this.duplicateController = duplicateController;
    }

    public ControllerBean getCurrentControl() {
        return currentControl;
    }

    public void setCurrentControl(ControllerBean currentControl) {
        this.currentControl = currentControl;
    }

    public List<ThemeColor> getDefaultThemeColors() {
        return defaultThemeColors;
    }

    public List<ThemeColor> getSyncThemeColors() {
        return syncThemeColors;
    }

    public void setSyncThemeColors(List<ThemeColor> syncThemeColors) {
        this.syncThemeColors = syncThemeColors;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public int getSyncBulbs() {
        return syncBulbs;
    }

    public void setSyncBulbs(int syncBulbs) {
        this.syncBulbs = syncBulbs;
    }

    public int getSyncSwitches() {
        return syncSwitches;
    }

    public void setSyncSwitches(int syncSwitches) {
        this.syncSwitches = syncSwitches;
    }

    public List<SimpleSchedule> getSyncSimpleSchedule() {
        return syncSimpleSchedule;
    }

    public void setSyncSimpleSchedule(List<SimpleSchedule> syncSimpleSchedule) {
        this.syncSimpleSchedule = syncSimpleSchedule;
    }

    public boolean isCustomSchedule() {
        return isCustomSchedule;
    }

    public void setCustomSchedule(boolean customSchedule) {
        isCustomSchedule = customSchedule;
    }

    public List<CustomSchedule> getSyncCustomSchedule() {
        return syncCustomSchedule;
    }

    public void setSyncCustomSchedule(List<CustomSchedule> syncCustomSchedule) {
        this.syncCustomSchedule = syncCustomSchedule;
    }

    public List<ThemeColor> getDuplThemeColors() {
        return duplThemeColors;
    }

    public List<SimpleSchedule> getDuplSimpleSchedule() {
        return duplSimpleSchedule;
    }

    public List<CustomSchedule> getDuplCustomSchedule() {
        return duplCustomSchedule;
    }
}
