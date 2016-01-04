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

    @Test
    fun testInit() {
        val ddpx = DDPx("http://cairo-playground.meteor.com/websocket")
        assertThat(ddpx).isNotNull()
    }

    @Test
    public fun testConnect() {
        val latch = CountDownLatch(1)

        val ddpx = DDPx("http://cairo-playground.meteor.com/websocket")
        ddpx.connect().continueWith { result ->
            assertThat(result.isFaulted).isFalse()
            latch.countDown()
        }

        latch.await(1000000, TimeUnit.SECONDS)
    }
}