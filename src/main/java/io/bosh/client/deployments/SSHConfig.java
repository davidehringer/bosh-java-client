package io.bosh.client.deployments;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Johannes Hiemer.
 */
public class SSHConfig {

    public SSHConfig(String deploymentName, String username, String publicKey, String jobTarget, int indexes) {
        this.command = "setup";
        this.deploymentName = deploymentName;
        this.target = new SSHTarget(jobTarget, indexes);
        this.params = new SSHParams(username, publicKey);
    }

    private String command;

    @JsonProperty("deployment_name")
    String deploymentName;

    private SSHParams params;

    private SSHTarget target;

    public SSHConfig(SSHConfig config, String pubKey) {
        this.command = config.getCommand();
        this.deploymentName = config.getDeploymentName();
        this.target = new SSHTarget(config.getTarget());
        this.params = new SSHParams(config.getParams().getUser(), pubKey);
    }

    public String getCommand() {
        return command;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public SSHParams getParams() {
        return params;
    }

    public SSHTarget getTarget() {
        return target;
    }

    public class SSHParams {

        private String user;

        @JsonProperty("public_key")
        private String publicKey;

        public SSHParams(String username, String publicKey) {
            this.user = username;
            this.publicKey = publicKey;
        }

        public String getUser() {
            return user;
        }

        public String getPublicKey() {
            return publicKey;
        }
    }

    public class SSHTarget {

        String job;

        int indexes;

        public SSHTarget(String jobTarget, int indexes) {
            this.job = jobTarget;
            this.indexes = indexes;
        }

        public SSHTarget(SSHTarget target) {
            this.job = target.getJob();
            this.indexes = target.getIndexes();
        }

        public String getJob() {
            return job;
        }

        public int getIndexes() {
            return indexes;
        }
    }
}