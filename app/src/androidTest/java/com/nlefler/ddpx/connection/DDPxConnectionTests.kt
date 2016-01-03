package com.nlefler.ddpx.connection

import com.nlefler.ddpx.DDPx
import junit.framework.TestCase
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Created by nathan on 1/2/16.
 */
public class DDPxConnectionTests: TestCase() {
    @Test
    public fun stateDuringConnect() {
        val latch = CountDownLatch(1)

        val conn = DDPxConnection("http://cairo-playground.meteor.com/websocket")
        assertEquals(conn.state, DDPxConnection.DDPxConnectionState.Unknown)
        assertFalse(conn.connected)

        val task = conn.connect()
        assertEquals(conn.state, DDPxConnection.DDPxConnectionState.Connecting)

        task.continueWith { result ->
            assertFalse(result.isFaulted)
            assertEquals(conn.state, DDPxConnection.DDPxConnectionState.Connected)
            latch.countDown()
        }

        latch.await(1000000, TimeUnit.SECONDS)
    }
}
