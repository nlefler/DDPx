package com.nlefler.ddpx

import junit.framework.TestCase
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Created by nathan on 1/2/16.
 */

public class DDPxTests: TestCase() {

    @Test
    public fun testInit() {
        val ddpx = DDPx("http://cairo-playground.meteor.com/websocket")
    }

    @Test
    public fun testConnect() {
        val latch = CountDownLatch(1)

        val ddpx = DDPx("http://cairo-playground.meteor.com/websocket")
        ddpx.connect().continueWith { result ->
            assertFalse(result.isFaulted)
            latch.countDown()
        }

        latch.await(1000000, TimeUnit.SECONDS)
    }
}