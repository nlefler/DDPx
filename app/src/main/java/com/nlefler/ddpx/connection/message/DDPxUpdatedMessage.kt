package com.nlefler.ddpx.connection.message

/**
 * Created by nathan on 1/9/16.
 */
public class DDPxUpdatedMessage: DDPxMessage() {
    override var messageName: String? = "updated"

    public var methods: Array<String> = emptyArray()

    override fun messageBody(): MutableMap<Any, Any> {
        return super.messageBody()
    }
}
