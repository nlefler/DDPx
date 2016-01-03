package com.nlefler.ddpx

import bolts.Task
import com.nlefler.ddpx.connection.DDPxConnection
import rx.Observable


/**
 * Created by nathan on 1/1/16.
 */
public class DDPx(val remoteURL: String) {

    private var connection: DDPxConnection? = null

    public fun connect(): Task<String> {
        connection = DDPxConnection(remoteURL)
        return connection!!.connect()
    }

    public fun disconnect() {

    }
}
