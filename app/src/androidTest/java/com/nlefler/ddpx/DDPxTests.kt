package com.nlefler.ddpx

import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import com.google.common.truth.Truth.*
import junit.framework.TestCase
import kotlin.test.*

/**
 * Created by nathan on 1/2/16.
 */

public class DDPxTests: TestCase() {
    val SERVER_URL = "http://10.65.106.103:3000/websocket"//"http://cairo-playground.meteor.com/websocket"

    @Test
    fun testInit() {
        val ddpx = DDPx(SERVER_URL)
        assertThat(ddpx).isNotNull()
    }

    @Test
    public fun testConnect() {
        val latch = CountDownLatch(1)

        val ddpx = DDPx(SERVER_URL)
        ddpx.connect().continueWith { result ->
            assertThat(result.isFaulted).isFalse()
            latch.countDown()
        }

        latch.await(1000000, TimeUnit.SECONDS)
    }
}