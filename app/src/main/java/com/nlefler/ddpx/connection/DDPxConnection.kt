package com.nlefler.ddpx.connection

import bolts.Task
import bolts.TaskCompletionSource
import com.google.gson.Gson
import com.koushikdutta.async.http.AsyncHttpClient
import com.koushikdutta.async.http.WebSocket
import com.nlefler.ddpx.connection.message.*

/**
 * Created by nathan on 1/1/16.
 */
public class DDPxConnection(val remoteURL: String) {
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

    private fun setupWebsocketHandlers(ws: WebSocket) {
        ws.setStringCallback { str ->
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
    }

    public enum class DDPxConnectionState {
        Unknown, Connecting, Connected, Disconnected
    }
}

const val LOG_TAG = "DDPxConnection"
