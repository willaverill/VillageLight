package com.villagelight.app.event;

/**
 * 通知pair数量更新
 */
public class PairEvent {
    int bulbs = -1;
    int switches = -1;

    public PairEvent() {

    }

    public PairEvent(int bulbs, int switches) {
        this.bulbs = bulbs;
        this.switches = switches;
    }

    public int getBulbs() {
        return bulbs;
    }

    public void setBulbs(int bulbs) {
        this.bulbs = bulbs;
    }

    public int getSwitches() {
        return switches;
    }

    public void setSwitches(int switches) {
        this.switches = switches;
    }
}
