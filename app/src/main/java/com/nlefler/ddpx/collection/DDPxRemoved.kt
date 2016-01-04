package com.nlefler.ddpx.collection

/**
 * Created by nathan on 1/3/16.
 */
public class DDPxRemoved(override val collection: String, override val id: String) : DDPxChange {
    override val type: DDPxChange.DDPxChangeType = DDPxChange.DDPxChangeType.Removed
}
