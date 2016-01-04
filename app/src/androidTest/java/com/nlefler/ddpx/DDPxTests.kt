package com.nlefler.ddpx

import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import com.google.common.truth.Truth.*
import com.nlefler.ddpx.collection.DDPxChange
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import rx.Observer
import rx.Subscriber
import kotlin.test.*

/**
 * Created by nathan on 1/2/16.
 */

public class DDPxTests: TestCase() {
    val SERVER_URL = "http://10.65.106.103:3000/websocket"//"http://cairo-playground.meteor.com/websocket"
    var ddpx: DDPx? = null

    @Before
    override fun setUp() {
        ddpx = DDPx(SERVER_URL)
    }

    @After
    override fun tearDown() {
        ddpx = null
    }

    @Test
    fun testInit() {
        assertThat(ddpx).isNotNull()
    }

    @Test
    public fun testConnect() {
        val latch = CountDownLatch(1)

        ddpx?.connect()?.continueWith { result ->
            val failureMessage = result.error?.message ?: ""
            assert_().withFailureMessage(failureMessage).that(result.isFaulted).isFalse()
            latch.countDown()
        }

        latch.await(1000000, TimeUnit.SECONDS)
    }

    @Test
    public fun testSubscribe() {
        val latch = CountDownLatch(1)

        var gotNext = false

        ddpx?.connect()?.continueWith { result ->
            assertThat(result.isFaulted).isFalse()

            ddpx?.sub("places", null)?.subscribe {object: Observer<DDPxChange> {
                override fun onNext(t: DDPxChange?) {
                    gotNext = true
                    latch.countDown()
                }

                override fun onError(e: Throwable?) {
                    throw UnsupportedOperationException()
                }

                override fun onCompleted() {
                    throw UnsupportedOperationException()
                }
            }}
        }

        latch.await(1000000, TimeUnit.SECONDS)
        assertThat(gotNext).isTrue()
    }
}