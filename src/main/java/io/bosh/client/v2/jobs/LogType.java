package io.bosh.client.v2.jobs;

/**
 * @author David Ehringer
 */
public enum LogType {

    AGENT("agent"), JOB("job");

    private final String type;

    private LogType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
