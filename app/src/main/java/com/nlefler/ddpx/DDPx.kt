package com.nlefler.ddpx

import android.util.Log
import bolts.Task
import bolts.TaskCompletionSource
import com.nlefler.ddpx.collection.DDPxChange
import com.nlefler.ddpx.connection.DDPxConnection
import com.nlefler.ddpx.error.DDPxError
import com.nlefler.ddpx.method.DDPxMethodResult
import rx.Observable
import rx.Subscriber
import rx.Observer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors


/**
 * Created by nathan on 1/1/16.
 */
public class DDPx(val remoteURL: String): DDPxConnection.DDPxConnectionDelegate {

    private var connection: DDPxConnection? = null

    private val subscriptions = HashMap<String, Subscription>()
    private val collectionObservables = HashMap<String, Obsv>()

    private val methodTasks  = HashMap<String, TaskCompletionSource<DDPxMethodResult>>()

    private var executor = Executors.newSingleThreadExecutor()

    public fun connect(): Task<String> {
        if (connection?.connected ?: false) {
            return Task.forError(Exception("Already connected with session: ${connection?.session}"))
        }
        connection = DDPxConnection(remoteURL)
        connection?.delegate = this
        return connection!!.connect()
    }

    public fun disconnect() {

    }

    public fun sub(collection: String, params: String?): Observable<DDPxChange> {
        var returnObservable: Observable<DDPxChange>? = null
        executor.submit({
            val id = UUID.randomUUID().toString()
            val observers = ArrayList<Observer<DDPxChange>>()

            val subscription = Subscription(collection, params, id)
            subscriptions.put(id, subscription)

            val obsv = Obsv(observers)

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

            collectionObservables.put(collection, obsv)

            connection?.sub(collection, params, id)

            returnObservable = observable
            return@submit
        }).get()

        return returnObservable!!
    }

    public fun unsub(collection: String, params: String?) {
        var sub: Subscription? = null
        executor.submit {
            subscriptions.forEach{ entry -> sub = if (entry.value.collection.equals(collection) &&
                    entry.value.params.equals(params)) entry.value else null }
            subscriptions.remove(sub?.id)
        }.get()
        val id = sub?.id ?: return
        connection?.unSub(id)
    }

    public fun method(method: String, params: Array<String>?, randomSeed: String?): Task<DDPxMethodResult> {
        val task = TaskCompletionSource<DDPxMethodResult>()
        executor.submit {
            val id = UUID.randomUUID().toString()
            methodTasks.put(id, task)
            connection?.method(method, params, id, randomSeed)
        }.get()

        return task.task
    }

    // DDPxConnection.DDPxConnectionDelegate
    override public fun onNoSub(id: String, error: DDPxError?) {
        executor.submit({
            subscriptions.remove(id)

            // TODO Error
        })
    }

    override public fun onChange(change: DDPxChange) {
        var obsv: Obsv? = null
        executor.submit({
            obsv = collectionObservables[change.collection]
        }).get()
        if (obsv == null) {
            return
        }

        val observers = obsv?.observers ?: return
        val cache = obsv?.changeCache ?: return

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
                    observers.forEach { observer -> observer.onNext(change) }
                }
                cache.clear()
            }
        }
        observers.forEach { observer ->
            try {
                observer.onNext(change)
            } catch (e: Throwable) {
                Log.e("", e.message)
            }
        }
    }

    override public fun onReady(sub: String) {

    }

    override fun onMethodResult(id: String, result: String?, error: DDPxError?) {
        executor.submit {
            val task = methodTasks.remove(id)
            task?.trySetResult(DDPxMethodResult(result, error))
        }
    }

    override fun onWritesUpdate(ids: Array<String>) {
    }

    private data class Subscription(val collection: String, val params: String?, val id: String) {}

    private data class Obsv(val observers: MutableList<Observer<DDPxChange>>) {
        var observable: Observable<DDPxChange>? = null
        var isComplete: Boolean = false
        var isErrored: Boolean = false
        var error: Throwable? = null

        val changeCache = ArrayList<DDPxChange>()
    }
}
