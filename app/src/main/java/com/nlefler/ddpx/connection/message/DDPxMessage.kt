package com.nlefler.ddpx.connection.message

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by nathan on 1/1/16.
 */
open public class DDPxMessage() {
    @SerializedName("msg")
    open public var messageName: String? = null

    open public fun messageBody(): MutableMap<Any, Any> {
        val m = HashMap<Any, Any>()
        if (messageName != null) {
            m.put("msg", messageName!!)
        }
        return m
    }
}
