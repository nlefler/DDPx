package com.nlefler.ddpx.connection.message

/**
 * Created by nathan on 1/3/16.
 */
public class DDPxRemovedMessage: DDPxMessage() {
    override var messageName: String? = "removed"

    public var collection: String? = null
    public var id: String? = null

    override fun messageBody(): MutableMap<Any, Any> {
        return super.messageBody()
    }
}
