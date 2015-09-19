package io.bosh.client.v2.deployments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author David Ehringer
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Resolution {

    private String name;
    private String plan;

    public String getName() {
        return name;
    }

    public String getPlan() {
        return plan;
    }

    @Override
    public String toString() {
        return "Resolution [name=" + name + ", plan=" + plan + "]";
    }

}
