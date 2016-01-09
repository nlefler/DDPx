package com.nlefler.ddpx

import android.util.Log
import bolts.Task
import com.nlefler.ddpx.collection.DDPxChange
import com.nlefler.ddpx.connection.DDPxConnection
import com.nlefler.ddpx.connection.message.DDPxError
import rx.Observable
import rx.Subscriber
import rx.Observer
import java.util.*
import java.util.concurrent.ConcurrentHashMap


/**
 * Created by nathan on 1/1/16.
 */
public class DDPx(val remoteURL: String): DDPxConnection.DDPxConnectionDelegate {

    private var connection: DDPxConnection? = null

    private var subsForCollection = ConcurrentHashMap<String, Obsv>()
    private var subsForId = ConcurrentHashMap<String, Obsv>()

    public fun connect(): Task<String> {
        connection = DDPxConnection(remoteURL)
        connection?.delegate = this
        return connection!!.connect()
    }

    public fun disconnect() {

    }

    public fun sub(collection: String, params: String?): Observable<DDPxChange> {
        val currObsv = subsForCollection[collection]
        if (currObsv != null && !currObsv.isErrored && !currObsv.isComplete && currObsv.observable != null) {
            return currObsv.observable!!
        }

        val id = UUID.randomUUID().toString()
        val observers = ArrayList<Observer<DDPxChange>>()

        val obsv = Obsv(collection, id, observers)

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

        synchronized(subsForCollection) {
            subsForCollection.put(collection, obsv)
        }
        synchronized(subsForId) {
            subsForId.put(id, obsv)
        }
        connection?.sub(collection, params, id)

        return observable
    }

    // DDPxConnection.DDPxConnectionDelegate
    override public fun onNoSub(id: String, error: DDPxError?) {
        val obsv: Obsv = subsForId[id] ?: return
        subsForId.remove(id)
        subsForCollection[obsv.collection]

        obsv.isErrored = true
        val throwable = Throwable(error?.message)
        obsv.error = throwable
        obsv.observers.forEach { observer -> observer.onError(throwable) }
    }

    override public fun onChange(change: DDPxChange) {
        val obsv = subsForCollection[change.collection] ?: return

        val observers = obsv.observers
        val cache = obsv.changeCache

        var noObservers = false
        var sendCache = false
        synchronized(observers) {
            noObservers = observers.isEmpty()
            sendCache = !observers.isEmpty() && !cache.isEmpty()
        }
        if (noObservers) {
            synchronized(cache) {
                cache.add(change)
            }
            return
        }
        else if (sendCache) {
            synchronized(cache) {
                cache.forEach { cacheChange ->
                    obsv.observers.forEach { observer -> observer.onNext(change) }
                }
                cache.clear()
            }
        }
        obsv.observers.forEach { observer ->
            try {
                observer.onNext(change)
            } catch (e: Throwable) {
                Log.e("", e.message)
            }
        }
    }

    override public fun onReady(sub: String) {

    }

    private data class Obsv(val collection: String, val id: String,
                            val observers: MutableList<Observer<DDPxChange>>) {
        var observable: Observable<DDPxChange>? = null
        var isComplete: Boolean = false
        var isErrored: Boolean = false
        var error: Throwable? = null

        val changeCache = ArrayList<DDPxChange>()
    }
}
