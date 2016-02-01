package com.nlefler.ddpx.connection

import android.util.Log
import bolts.Task
import bolts.TaskCompletionSource
import com.google.gson.Gson
import com.koushikdutta.async.http.AsyncHttpClient
import com.koushikdutta.async.http.WebSocket
import com.nlefler.ddpx.collection.DDPxAdded
import com.nlefler.ddpx.collection.DDPxChange
import com.nlefler.ddpx.collection.DDPxChanged
import com.nlefler.ddpx.collection.DDPxRemoved
import com.nlefler.ddpx.connection.message.*
import com.nlefler.ddpx.error.DDPxError
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by nathan on 1/1/16.
 */
public class DDPxConnection(val remoteURL: String) {
    public var delegate: DDPxConnectionDelegate? = null

    public val connected: Boolean
        get() = this.state == DDPxConnectionState.Connected

    public var session: String? = null
        private set

    private val msgTypes = DDPxMessageTypes()

    public var state = DDPxConnectionState.Unknown
    private var websocket: WebSocket? = null
    private val gson = Gson()

    private var connectTask = TaskCompletionSource<String>()

    private val messageExecutor = Executors.newSingleThreadExecutor()
    private val delegateExecutor = Executors.newSingleThreadExecutor()


    public fun connect(): Task<String> {
        when (state) {
            DDPxConnectionState.Connecting, DDPxConnectionState.Connected ->
                return connectTask.task
            DDPxConnectionState.Disconnected -> return Task.forError(Exception("Cannot reopen disconnected connection"))
            DDPxConnectionState.Unknown -> {}
        }

        state = DDPxConnectionState.Connecting
        AsyncHttpClient.getDefaultInstance().websocket(remoteURL, "", {ex, ws ->
            if (ex != null) {
                connectTask.trySetError(ex)
                return@websocket
            }
            websocket = ws
            setupWebsocketHandlers(ws)
            val openMsg = DDPxConnectMessage()
            sendMessage(openMsg, ws)
        })
        return connectTask.task
    }

    public fun sub(collection: String, params: String?, id: String) {
        if (state != DDPxConnectionState.Connected) {
            return
        }

        val msg = DDPxSubMessage()
        msg.collection = collection
        msg.params = params
        msg.id = id

        websocket?.send(gson.toJson(msg.messageBody()))
    }

    public fun unSub(id: String) {
        if (state != DDPxConnectionState.Connected) {
            return
        }

        val msg = DDPxUnSubMessage()
        msg.id = id

        websocket?.send(gson.toJson(msg.messageBody()))
    }

    public fun method(method: String, params: Array<String>?, id: String, randomSeed: String?) {
        if (state != DDPxConnectionState.Connected) {
            return
        }

        val msg = DDPxMethodMessage()
        msg.method = method
        msg.params = params
        msg.id = id
        msg.randomSeed = randomSeed

        websocket?.send(gson.toJson(msg.messageBody()))
    }

    private fun setupWebsocketHandlers(ws: WebSocket) {
        ws.setStringCallback { str ->
            messageExecutor.submit {
                Log.d(LOG_TAG, "Got message $str")
                val msg = gson.fromJson(str, DDPxMessage::class.java)
                if (msg.messageName == null) {
                    return@submit
                }
                val msgType = msgTypes.messageForName(msg.messageName!!) ?: return@submit

                val typedMsg = gson.fromJson(str, msgType.java)
                handleMessage(typedMsg)
            }
        }
    }

    private fun sendMessage(msg: DDPxMessage, ws: WebSocket) {
        ws.send(gson.toJson(msg.messageBody()))
    }

    private fun handleMessage(msg: DDPxMessage) {
        when (state) {
            DDPxConnectionState.Connecting -> {
                handleConnectionResponse(msg)
            }
            DDPxConnectionState.Connected -> {
                when (msg) {
                    is DDPxPingMessage -> handlePing(msg)
                    is DDPxNoSubMessage -> handleNoSub(msg)
                    is DDPxAddedMessage, is DDPxChangedMessage, is DDPxRemovedMessage -> handleChangeMessage(msg)
                    is DDPxReadyMessage -> msg.subs?.forEach { sub -> delegateExecutor.submit({delegate?.onReady(sub)}) }
                    is DDPxResultMessage -> delegateExecutor.submit { delegate?.onMethodResult(msg.id, msg.result, msg.error) }
                    is DDPxUpdatedMessage -> delegateExecutor.submit { delegate?.onWritesUpdate(msg.methods) }
                }
            }
            DDPxConnectionState.Disconnected, DDPxConnectionState.Unknown -> {}
        }
    }

    private fun handleConnectionResponse(msg: DDPxMessage) {
        when (msg) {
            is DDPxConnectedMessage -> {
                session = msg.session
                state = DDPxConnectionState.Connected
                connectTask.trySetResult(session)
            }
            is DDPxFailedMessage -> {
                state = DDPxConnectionState.Disconnected
                connectTask.trySetError(Exception("Invalid protocol version"))
            }
        }
    }

    private fun handlePing(ping: DDPxPingMessage) {
        delegateExecutor.submit({
            val pong = DDPxPongMessage()
            pong.id = ping.id
            websocket?.send(gson.toJson(pong.messageBody()))
            Log.d(LOG_TAG, "Responded to pong id:${pong.id}")
        })
    }

    private fun handleNoSub(msg: DDPxNoSubMessage) {
        delegateExecutor.submit({
            val id = msg.id ?: return@submit
            delegate?.onNoSub(id, msg.error)
        })
    }

    private fun handleChangeMessage(msg: DDPxMessage) {
        delegateExecutor.submit({

            when (msg) {
                is DDPxAddedMessage -> {
                    val collection = msg.collection
                    val id = msg.id
                    if (collection == null || id == null) {
                        return@submit
                    }

                    val add = DDPxAdded(collection, id, msg.fields)
                    delegate?.onChange(add)
                }
                is DDPxChangedMessage -> {
                    val collection = msg.collection
                    val id = msg.id
                    if (collection == null || id == null) {
                        return@submit
                    }
                    val changed = DDPxChanged(collection, id, msg.fields, msg.cleared)
                    delegate?.onChange(changed)
                }
                is DDPxRemovedMessage -> {
                    val collection = msg.collection
                    val id = msg.id
                    if (collection == null || id == null) {
                        return@submit
                    }

                    val removed = DDPxRemoved(collection, id)
                    delegate?.onChange(removed)
                }
                else -> {
                }
            }
        })
    }

    public enum class DDPxConnectionState {
        Unknown, Connecting, Connected, Disconnected
    }

    public interface DDPxConnectionDelegate {
        public fun onNoSub(id: String, error: DDPxError?)
        public fun onChange(change: DDPxChange)
        public fun onReady(sub: String)
        public fun onMethodResult(id: String, result: String?, error: DDPxError?)
        public fun onWritesUpdate(ids: Array<String>)
    }
}

const val LOG_TAG = "DDPxConnection"
