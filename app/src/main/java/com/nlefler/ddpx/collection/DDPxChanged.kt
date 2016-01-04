package com.nlefler.ddpx.collection

/**
 * Created by nathan on 1/3/16.
 */
public class DDPxChanged(override val collection: String, override val id: String,
                         val fields: Map<String, Any>?, val cleared: Array<String>?): DDPxChange {
    override public val type = DDPxChange.DDPxChangeType.Changed
}
