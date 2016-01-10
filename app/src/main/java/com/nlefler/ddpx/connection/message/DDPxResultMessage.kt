package com.nlefler.ddpx.connection.message

import com.nlefler.ddpx.error.DDPxError

/**
 * Created by nathan on 1/9/16.
 */
public class DDPxResultMessage: DDPxMessage() {
    override var messageName: String? = "result"

    public var id: String = ""
    public var error: DDPxError? = null
    public var result: String? = null

    override fun messageBody(): MutableMap<Any, Any> {
        return super.messageBody()
    }
}
