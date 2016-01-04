package com.nlefler.ddpx.collection

/**
 * Created by nathan on 1/2/16.
 */
public class DDPxAdded : DDPxChange {
    override public var type = DDPxChange.DDPxChangeType.Added
    override public var collection: String? = null
    override public var id: String? = null
}
