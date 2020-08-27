package com.villagelight.app.model;

import java.util.List;

/**
 * Created by CharlesLui on 22/03/2018.
 */

public class UpdateBean {


    private List<FirmwaresBean> firmwares;

    public List<FirmwaresBean> getFirmwares() {
        return firmwares;
    }

    public void setFirmwares(List<FirmwaresBean> firmwares) {
        this.firmwares = firmwares;
    }

    public static class FirmwaresBean {
        /**
         * version : V10
         * url : http://eeev01.hk79.2ifree.com/V10.bin
         */

        private String version;
        private String url;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
