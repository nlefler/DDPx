package com.nlefler.ddpx.connection

import com.nlefler.ddpx.collection.DDPxChange
import org.junit.Test
import com.google.common.truth.Truth.*
import com.nlefler.ddpx.error.DDPxError
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Created by nathan on 1/2/16.
 */
public class DDPxConnectionTests: TestCase(),  DDPxConnection.DDPxConnectionDelegate {
    val SERVER_URL = "http://cairo-playground.meteor.com/websocket"
    
    var noSubBlock: ((String, DDPxError?) -> Unit)? = null
    var onChangeBlock: ((DDPxChange) -> Unit)? = null
    var onReadyBlock: ((String) -> Unit)? = null
    var conn: DDPxConnection? = null

    @Before
    override public fun setUp() {
        conn = DDPxConnection(SERVER_URL)
    }

    @After
    override public fun tearDown() {
        conn = null
        noSubBlock = null
        onChangeBlock = null
        onReadyBlock = null
    }

    @Test
    public fun testStateDuringConnect() {
        val latch = CountDownLatch(1)

        assertThat(conn).isNotNull()
        assertThat(conn?.state).isEqualTo(DDPxConnection.DDPxConnectionState.Unknown)
        assertThat(conn?.connected).isFalse()

        val task = conn?.connect()
        assertThat(conn?.state).isEqualTo(DDPxConnection.DDPxConnectionState.Connecting)

        task?.continueWith { result ->
            assertThat(result.isFaulted).isFalse()
            assertThat(conn?.state).isEqualTo(DDPxConnection.DDPxConnectionState.Connected)
            latch.countDown()
        }

        latch.await(1000000, TimeUnit.SECONDS)
    }

    @Test
    public fun testNoSub() {
        val latch = CountDownLatch(1)

        conn?.delegate = this

        val task = conn?.connect()

        task?.continueWith { result ->
            assertThat(result.isFaulted).isFalse()
            assertThat(conn?.state).isEqualTo(DDPxConnection.DDPxConnectionState.Connected)

            noSubBlock = { id, error ->
                latch.countDown()
            }
            conn?.sub("fake", null, "id")
        }

        latch.await(1000000, TimeUnit.SECONDS)
    }

    @Test
    public fun testSub() {
        val latch = CountDownLatch(2)

        conn?.delegate = this

        val task = conn?.connect()

        task?.continueWith { result ->
            assertThat(result.isFaulted).isFalse()
            assertThat(conn?.state).isEqualTo(DDPxConnection.DDPxConnectionState.Connected)

            val id = UUID.randomUUID().toString()
            val collection = "places"
            onChangeBlock = { change ->
                assertThat(change.collection).isEqualTo(collection)
                latch.countDown()
            }
            onReadyBlock = { sub ->
                assertThat(sub).isEqualTo(id)
                latch.countDown()
            }
            conn?.sub(collection, null, id)
        }

        latch.await(1000000, TimeUnit.SECONDS)
    }

    override public fun onNoSub(id: String, error: DDPxError?) {
        val block = noSubBlock
        if (block != null) {
            block(id, error)
        }
    }

    override public fun onChange(change: DDPxChange) {
        val block = onChangeBlock
        if (block != null) {
            block(change)
        }
    }

    override public fun onReady(sub: String) {
        val block = onReadyBlock
        if (block != null) {
            block(sub)
        }
    }

    override fun onMethodResult(id: String, result: String?, error: DDPxError?) {
    }

    override fun onWritesUpdate(ids: Array<String>) {
    }
}
