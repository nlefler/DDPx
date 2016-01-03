package com.nlefler.ddpx.connection.message

/**
 * Created by nathan on 1/2/16.
 */
public class DDPxFailedMessage(): DDPxMessage() {
    override public var messageName: String? = "failed"

    public val version: String? = null
}
