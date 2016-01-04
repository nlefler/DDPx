package com.nlefler.ddpx.connection

import com.nlefler.ddpx.collection.DDPxChange
import com.nlefler.ddpx.connection.DDPxConnection
import org.junit.Test
import com.google.common.truth.Truth.*
import com.nlefler.ddpx.connection.message.DDPxError
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Created by nathan on 1/2/16.
 */
public class DDPxConnectionTests: TestCase(),  DDPxConnection.DDPxConnectionDelegate {
    private var noSubLatch: CountDownLatch? = null
    var conn: DDPxConnection? = null

    @Before
    override public fun setUp() {
        conn = DDPxConnection("http://cairo-playground.meteor.com/websocket")
    }

    @After
    override public fun tearDown() {
        conn = null
        noSubLatch = null
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

            noSubLatch = latch
            conn?.sub("fake", null, "id")
        }

        latch.await(1000000, TimeUnit.SECONDS)
    }

    @Test
    public fun testSub() {
        val latch = CountDownLatch(1)

        conn?.delegate = this

        val task = conn?.connect()

        task?.continueWith { result ->
            assertThat(result.isFaulted).isFalse()
            assertThat(conn?.state).isEqualTo(DDPxConnection.DDPxConnectionState.Connected)

            conn?.sub("places", null, "id")
        }

        latch.await(1000000, TimeUnit.SECONDS)
    }

    override public fun onNoSub(id: String?, error: DDPxError?) {
        assertThat(id).isNotNull()
        noSubLatch?.countDown()
    }

    override public fun onChange(change: DDPxChange) {

    }
}
