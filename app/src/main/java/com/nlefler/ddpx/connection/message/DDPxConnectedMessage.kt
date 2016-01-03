package com.nlefler.ddpx.connection.message

/**
 * Created by nathan on 1/2/16.
 */
public class DDPxConnectedMessage(): DDPxMessage() {
    override public var messageName: String? = "connected"

    public var session: String? = null
}
