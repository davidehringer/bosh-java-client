package io.bosh.client.deployments;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author David Ehringer, Jannik Heyl.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Deployment {

    private String name;

    private String manifest;

    private Map<String, Object> manifestMap = new HashMap<String, Object>();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getRawManifest() {
        return manifest;
    }

    public void setRawManifest(String manifest) { this.manifest = manifest; }

    public Map<String, Object> getManifest() {
        return Collections.unmodifiableMap(manifestMap);
    }

    public void setManifestMap(Map<String, Object> manifestMap) {
        this.manifestMap.clear();
        this.manifestMap.putAll(manifestMap);
    }

    @Override
    public String toString() {
        return "DeploymentDetails [name=" + name + ", manifest=" + manifest + "]";
    }
}
