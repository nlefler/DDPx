package com.nlefler.ddpx.connection.message

/**
 * Created by nathan on 1/1/16.
 */
public class DDPxConnectMessage: DDPxMessage() {
    override public var messageName: String? = "connect"

    public var session: String? = null
    public val version = "1"
    public val support = arrayOf("1")

    override public fun messageBody(): MutableMap<Any, Any> {
        val body = super.messageBody()
        body.put("version", version)
        body.put("support", support)
        if (session != null) {
            body.put("session", session!!)
        }
        return body
    }
}
