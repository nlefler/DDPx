package com.nlefler.ddpx.connection.message

/**
 * Created by nathan on 1/9/16.
 */
public class DDPxUnSubMessage: DDPxMessage() {
    override var messageName: String? = "unsub"

    public var id: String? = null

    override fun messageBody(): MutableMap<Any, Any> {
        val body = super.messageBody()
        if (id != null) {
            body.put("id", id!!)
        }
        return body
    }
}
