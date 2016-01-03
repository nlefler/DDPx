package com.nlefler.ddpx.connection.message

/**
 * Created by nathan on 1/2/16.
 */
public class DDPxPongMessage: DDPxMessage() {
    override public var messageName: String? = "pong"

    public var id: String? = null

    override public fun messageBody(): MutableMap<Any, Any> {
        val b = super.messageBody()
        if (id != null) {
            b.put("id", id!!)
        }
        return b
    }
}
