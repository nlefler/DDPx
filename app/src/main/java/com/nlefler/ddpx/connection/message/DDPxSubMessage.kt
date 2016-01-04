package com.nlefler.ddpx.connection.message

/**
 * Created by nathan on 1/2/16.
 */
public class DDPxSubMessage: DDPxMessage() {
    override public var messageName: String? = "sub"

    public var collection: String? = null
    public var id: String? = null
    public var params: String? = null

    override public fun messageBody(): MutableMap<Any, Any> {
        val body = super.messageBody()
        if (collection != null) {
            body.put("name", collection!!)
        }
        if (id != null) {
            body.put("id", id!!)
        }
        if (params != null) {
            body.put("params", params!!)
        }
        return body
    }
}
