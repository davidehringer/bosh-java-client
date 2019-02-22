package io.bosh.client.authentication;

/**
 * @author Jannik Heyl.
 */
public class OAuth implements Authentication {

    private boolean strictHostKeyChecking;

    public OAuth(boolean strictHostKeyChecking) {
        this.strictHostKeyChecking = strictHostKeyChecking;
    }

    public boolean isStrictHostKeyChecking() {
        return strictHostKeyChecking;
    }

    public void setStrictHostKeyChecking(boolean strictHostKeyChecking) {
        this.strictHostKeyChecking = strictHostKeyChecking;
    }
}
