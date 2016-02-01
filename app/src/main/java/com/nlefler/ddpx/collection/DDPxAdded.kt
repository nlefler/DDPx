package com.nlefler.ddpx.collection

/**
 * Created by nathan on 1/2/16.
 */
public class DDPxAdded(override val collection: String, override val id: String, val fields: Map<String, Any>?): DDPxChange {
    override public val type = DDPxChange.DDPxChangeType.Added
}
