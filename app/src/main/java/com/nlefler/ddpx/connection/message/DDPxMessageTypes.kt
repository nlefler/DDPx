package com.nlefler.ddpx.connection.message

import kotlin.reflect.KClass

/**
 * Created by nathan on 1/2/16.
 */
public class DDPxMessageTypes {
    val messageTypes = mapOf(
            Pair(DDPxConnectMessage().messageName!!, DDPxConnectMessage::class),
            Pair(DDPxConnectedMessage().messageName!!, DDPxConnectedMessage::class),
            Pair(DDPxFailedMessage().messageName!!, DDPxFailedMessage::class),
            Pair(DDPxPingMessage().messageName!!, DDPxPingMessage::class),
            Pair(DDPxPongMessage().messageName!!, DDPxPongMessage::class),
            Pair(DDPxNoSubMessage().messageName!!, DDPxNoSubMessage::class),
            Pair(DDPxAddedMessage().messageName!!, DDPxAddedMessage::class),
            Pair(DDPxChangedMessage().messageName!!, DDPxChangedMessage::class)
    )

    public fun messageForName(msgType: String): KClass<out DDPxMessage>? {
        return messageTypes[msgType]
    }
}
