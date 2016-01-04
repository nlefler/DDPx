package com.nlefler.ddpx.collection

/**
 * Created by nathan on 1/2/16.
 */
public interface DDPxChange {
    public enum class DDPxChangeType {
        Unknown, Added, Changed, Removed
    }

    public val type: DDPxChangeType
    public val collection: String
    public val id: String
}
