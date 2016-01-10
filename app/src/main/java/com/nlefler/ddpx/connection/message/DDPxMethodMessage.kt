package com.nlefler.ddpx.connection.message

/**
 * Created by nathan on 1/9/16.
 */
public class DDPxMethodMessage: DDPxMessage() {
    override var messageName: String? = "method"

    public var method: String? = null
    public var params: Array<String>? = null
    public var id: String? = null
    public var randomSeed: String? = null

    override fun messageBody(): MutableMap<Any, Any> {
        val body = super.messageBody()

        if (method != null) {
            body.put("method", method!!)
        }
        if (params != null) {
            body.put("params", params!!)
        }
        if (id != null) {
            body.put("id", id!!)
        }
        if (randomSeed != null) {
            body.put("randomSeed", randomSeed!!)
        }

        return body
    }

}
