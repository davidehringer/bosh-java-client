package io.bosh.client.vms;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author David Ehringer
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VmVitals {

    private Cpu cpu;
    private Disk disk;
    private List<String> load = new ArrayList<String>();
    private Memory mem;
    private Memory swap;
    @JsonProperty("resurrection_paused")
    private boolean resurrectionPaused;

    public Cpu getCpu() {
        return cpu;
    }

    public Disk getDisk() {
        return disk;
    }

    public List<String> getLoad() {
        return load;
    }

    public Memory getMem() {
        return mem;
    }

    public Memory getSwap() {
        return swap;
    }

    public boolean isResurrectionPaused() {
        return resurrectionPaused;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Cpu {

        private String sys;
        private String user;
        private String wait;

        public String getSys() {
            return sys;
        }

        public String getUser() {
            return user;
        }

        public String getWait() {
            return wait;
        }

        @Override
        public String toString() {
            return "Cpu [sys=" + sys + ", user=" + user + ", wait=" + wait + "]";
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Disk {
        private DiskUsage ephemeral;
        private DiskUsage persistent;
        private DiskUsage system;

        public DiskUsage getEphemeral() {
            return ephemeral;
        }

        /**
         * May be null if VM does not have persistent disk
         * @return
         */
        public DiskUsage getPersistent() {
            return persistent;
        }

        public DiskUsage getSystem() {
            return system;
        }

        @Override
        public String toString() {
            return "Disk [ephemeral=" + ephemeral + ", persistent=" + persistent + ", system="
                    + system + "]";
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DiskUsage {

        @JsonProperty("inode_percent")
        private int inodePercent;
        @JsonProperty("percent")
        private int percent;

        public int getInodePercent() {
            return inodePercent;
        }

        public int getPercent() {
            return percent;
        }

        @Override
        public String toString() {
            return "DiskUsage [inodePercent=" + inodePercent + ", percent=" + percent + "]";
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Memory {

        private int kb;
        private int percent;

        public int getKb() {
            return kb;
        }

        public int getPercent() {
            return percent;
        }

        @Override
        public String toString() {
            return "Memory [kb=" + kb + ", percent=" + percent + "]";
        }

    }

    @Override
    public String toString() {
        return "VmVitals [cpu=" + cpu + ", disk=" + disk + ", load=" + load + ", mem=" + mem
                + ", swap=" + swap + ", resurrectionPaused=" + resurrectionPaused + "]";
    }

}
