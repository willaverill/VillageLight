package com.villagelight.app.job;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.villagelight.app.ProjectApp;
import com.villagelight.app.event.JobDoneEvent;
import com.villagelight.app.util.LogUtils;

import cn.fly2think.blelib.BleConstants;
import cn.fly2think.blelib.TransUtils;
import de.greenrobot.event.EventBus;


public class SendCommandJob extends Job {

    private long localId;
    private byte[] cmds;
    private long delayTimeInMs;
    private int PACK_SIZE = 20;
    private static final int PACK_DELAY = 20;

    public SendCommandJob(byte[] cmds, int priority) {
        super(new Params(priority));
        localId = System.currentTimeMillis();
//        delayTimeInMs = 100;
        delayTimeInMs = PACK_DELAY;
        this.cmds = cmds;

        if (ProjectApp.getInstance().manager.cubicBLEDevice != null) {
            Integer mtu = BleConstants.MTU_MAP.get(ProjectApp.getInstance().manager.cubicBLEDevice.deviceMac);
            if (mtu == null) {
                mtu = 23;
            }

            PACK_SIZE = mtu - 3;//设置每包最大字节数
        }

    }

    public long getDelayTimeInMs() {
        return delayTimeInMs;
    }

    public void setDelayTimeInMs(long delayTimeInMs) {
//        this.delayTimeInMs = delayTimeInMs;
    }

    @Override
    public void onAdded() {
        // job has been secured to disk, add item to database
    }

    @Override
    public void onRun() throws Throwable {

        if (cmds != null && ProjectApp.getInstance().manager.cubicBLEDevice != null
                && ProjectApp.getInstance().manager.cubicBLEDevice.isConnected()) {

            byte cmd = cmds[1];

            if (cmds.length > PACK_SIZE) {

                int len = cmds.length / PACK_SIZE;
                int mod = cmds.length % PACK_SIZE;
                for (int i = 0; i < len; i++) {

                    Thread.sleep(delayTimeInMs);
                    byte[] tmps = new byte[PACK_SIZE];
                    System.arraycopy(cmds, i * PACK_SIZE, tmps, 0, tmps.length);
                    ProjectApp.getInstance().manager.cubicBLEDevice.writeValue(tmps);
                    LogUtils.d("send:" + TransUtils.appendSpace(TransUtils.bytes2hex(tmps)));

                }

                if (mod != 0) {
                    Thread.sleep(delayTimeInMs);
                    byte[] tmps = new byte[mod];
                    System.arraycopy(cmds, len * PACK_SIZE, tmps, 0, tmps.length);
                    ProjectApp.getInstance().manager.cubicBLEDevice.writeValue(tmps);
                    LogUtils.d("send:" + TransUtils.appendSpace(TransUtils.bytes2hex(tmps)));
                }

            } else {

                Thread.sleep(delayTimeInMs);
                ProjectApp.getInstance().manager.cubicBLEDevice.writeValue(cmds);
                LogUtils.d("send:" + TransUtils.appendSpace(TransUtils.bytes2hex(cmds)));

            }

            EventBus.getDefault().post(new JobDoneEvent(cmd));

        }

    }

    @Override
    protected void onCancel() {
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {

        return false;
    }
}
