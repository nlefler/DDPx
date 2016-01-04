package com.nlefler.ddpx.connection.message

/**
 * Created by nathan on 1/3/16.
 */
public class DDPxChangedMessage: DDPxMessage() {
    override var messageName: String? = "changed"

    public var collection: String? = null
    public var id: String? = null
    public var fields: Map<String, Any>? = null
    public var cleared: Array<String>? = null

    override fun messageBody(): MutableMap<Any, Any> {
        return super.messageBody()
    }
}