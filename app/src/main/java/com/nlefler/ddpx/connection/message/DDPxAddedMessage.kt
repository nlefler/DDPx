package com.nlefler.ddpx.connection.message

/**
 * Created by nathan on 1/3/16.
 */
public class DDPxAddedMessage: DDPxMessage() {
    override var messageName: String? = "added"

    public var collection: String? = null
    public var id: String? = null
    public var fields: Map<String, Any>? = null

    override fun messageBody(): MutableMap<Any, Any> {
        return super.messageBody()
    }
}
