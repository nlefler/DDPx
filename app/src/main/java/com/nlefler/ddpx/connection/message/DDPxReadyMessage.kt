package com.nlefler.ddpx.connection.message

/**
 * Created by nathan on 1/4/16.
 */
public class DDPxReadyMessage: DDPxMessage() {
    override var messageName: String? = "ready"

    public var subs: Array<String>? = null

    override fun messageBody(): MutableMap<Any, Any> {
        return super.messageBody()
    }
}
