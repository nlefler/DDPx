package com.nlefler.ddpx

import bolts.Task
import com.nlefler.ddpx.collection.DDPxChange
import com.nlefler.ddpx.connection.DDPxConnection
import com.nlefler.ddpx.connection.message.DDPxError
import rx.Observable
import java.util.*


/**
 * Created by nathan on 1/1/16.
 */
public class DDPx(val remoteURL: String): DDPxConnection.DDPxConnectionDelegate {

    private var connection: DDPxConnection? = null

    private var subs = HashMap<String, Observable<DDPxChange>>()

    public fun connect(): Task<String> {
        connection = DDPxConnection(remoteURL)
        connection?.delegate = this
        return connection!!.connect()
    }

    public fun disconnect() {

    }

    public fun sub(collection: String, params: String?): Observable<DDPxChange> {
        val id = UUID.randomUUID().toString()
        val observable = Observable.create<DDPxChange> { subscriber ->  }
        subs.put(id, observable)
        connection?.sub(collection, params, id)

        return observable
    }

    // DDPxConnection.DDPxConnectionDelegate
    override public fun onNoSub(id: String?, error: DDPxError?) {

    }

    override public fun onChange(change: DDPxChange) {

    }
}
