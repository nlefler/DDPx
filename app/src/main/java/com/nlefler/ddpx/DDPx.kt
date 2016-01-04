package com.nlefler.ddpx

import bolts.Task
import com.nlefler.ddpx.collection.DDPxChange
import com.nlefler.ddpx.connection.DDPxConnection
import com.nlefler.ddpx.connection.message.DDPxError
import rx.Observable
import rx.Subscriber
import rx.Observer
import java.util.*


/**
 * Created by nathan on 1/1/16.
 */
public class DDPx(val remoteURL: String): DDPxConnection.DDPxConnectionDelegate {

    private var connection: DDPxConnection? = null

    private var subs = HashMap<String, Obsv>()

    public fun connect(): Task<String> {
        connection = DDPxConnection(remoteURL)
        connection?.delegate = this
        return connection!!.connect()
    }

    public fun disconnect() {

    }

    public fun sub(collection: String, params: String?): Observable<DDPxChange> {
        val id = UUID.randomUUID().toString()
        val observers = ArrayList<Observer<DDPxChange>>()

        val obsv = Obsv(id, observers)

        val observable = Observable.create<DDPxChange> { subscriber ->
            if (obsv.isComplete) {
                subscriber.onCompleted()
                return@create
            }
            if (obsv.isErrored) {
                subscriber.onError(obsv.error)
                return@create
            }
            observers.add(subscriber as Observer<DDPxChange>)
        }
        obsv.observable = observable

        subs.put(id, obsv)
        connection?.sub(collection, params, id)

        return observable
    }

    // DDPxConnection.DDPxConnectionDelegate
    override public fun onNoSub(id: String, error: DDPxError?) {
        val obsv: Obsv = subs[id] ?: return
        subs.remove(id)

        obsv.isErrored = true
        val throwable = Throwable(error?.message)
        obsv.error = throwable
        obsv.observers.forEach { observer -> observer.onError(throwable) }
    }

    override public fun onChange(change: DDPxChange) {
        val obsv: Obsv = subs[change.id] ?: return
        obsv.observers.forEach { observer -> observer.onNext(change) }
    }

    override public fun onReady(sub: String) {

    }

    private data class Obsv(val id: String,
                            val observers: MutableList<Observer<DDPxChange>>) {
        var observable: Observable<DDPxChange>? = null
        var isComplete: Boolean = false
        var isErrored: Boolean = false
        var error: Throwable? = null
    }
}
