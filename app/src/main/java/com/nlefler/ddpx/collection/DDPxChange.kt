package com.nlefler.ddpx.collection

/**
 * Created by nathan on 1/2/16.
 */
public interface DDPxChange {
    public enum class DDPxChangeType {
        Unknown, Added, Changed, Removed
    }

    public var type: DDPxChangeType
    public var collection: String?
    public var id: String?
}
