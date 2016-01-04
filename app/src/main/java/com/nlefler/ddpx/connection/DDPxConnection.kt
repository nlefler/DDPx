package com.nlefler.ddpx.connection

import android.util.Log
import bolts.Task
import bolts.TaskCompletionSource
import com.google.gson.Gson
import com.koushikdutta.async.http.AsyncHttpClient
import com.koushikdutta.async.http.WebSocket
import com.nlefler.ddpx.collection.DDPxChange
import com.nlefler.ddpx.connection.message.*
import java.util.*

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

    private fun setupWebsocketHandlers(ws: WebSocket) {
        ws.setStringCallback { str ->
            Log.d(LOG_TAG, "Got message $str")
            val msg = gson.fromJson(str, DDPxMessage::class.java)
            if (msg.messageName == null) {
                return@setStringCallback
            }
            val msgType = msgTypes.messageForName(msg.messageName!!) ?: return@setStringCallback

            val typedMsg = gson.fromJson(str, msgType.java)
            handleMessage(typedMsg)
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
                    is DDPxNoSubMessage -> delegate?.onNoSub(msg.id, msg.error)
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
        val pong = DDPxPongMessage()
        pong.id = ping.id
        websocket?.send(gson.toJson(pong))
        Log.d(LOG_TAG, "Responded to pong ${pong.id}")
    }

    public enum class DDPxConnectionState {
        Unknown, Connecting, Connected, Disconnected
    }

    public interface DDPxConnectionDelegate {
        public fun onNoSub(id: String?, error: DDPxError?)
        public fun onChange(change: DDPxChange)
    }
}

const val LOG_TAG = "DDPxConnection"
