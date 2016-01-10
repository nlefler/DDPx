package com.nlefler.ddpx.connection.message

import com.nlefler.ddpx.error.DDPxError

/**
 * Created by nathan on 1/3/16.
 */
public class DDPxNoSubMessage: DDPxMessage() {
    override var messageName: String? = "nosub"

    public val id: String? = null
    public val error: DDPxError? = null

    override fun messageBody(): MutableMap<Any, Any> {
        return super.messageBody()
    }
}
