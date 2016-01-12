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

/**
 * Created by nathan on 1/2/16.
 */

public class DDPxTests: TestCase() {
    val SERVER_URL = "http://cairo-playground.meteor.com/websocket"
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
        var latch = CountDownLatch(1)

        var gotNext = false

        ddpx?.connect()?.continueWith { result ->
            assertThat(result.isFaulted).isFalse()
            latch.countDown()
        }
        latch.await(1000000, TimeUnit.SECONDS)
        latch = CountDownLatch(1)


        val observer = object: Observer<DDPxChange> {
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
        }
        ddpx?.sub("places", null)?.subscribe(observer)

        latch.await(1000000, TimeUnit.SECONDS)
        assertThat(gotNext).isTrue()
    }

    @Test
    public fun testMethod() {
        val connectLatch = CountDownLatch(1)

        var gotNext = false

        ddpx?.connect()?.continueWith { result ->
            assertThat(result.isFaulted).isFalse()
            connectLatch.countDown()
        }
        connectLatch.await(1000000, TimeUnit.SECONDS)

        val subLatch = CountDownLatch(1)
        val observer = object: Observer<DDPxChange> {
            override fun onNext(t: DDPxChange?) {
                gotNext = true
                subLatch.countDown()
            }

            override fun onError(e: Throwable?) {
                throw UnsupportedOperationException()
            }

            override fun onCompleted() {
                throw UnsupportedOperationException()
            }
        }
        ddpx?.sub("places", null)?.subscribe(observer)

        subLatch.await(1000000, TimeUnit.SECONDS)
        assertThat(gotNext).isTrue()

        var gotResult = false
        val methodLatch = CountDownLatch(1)

        val placeName = "testPlace${Math.random()}"
        ddpx?.method("addPlace", arrayOf(placeName), null)?.continueWith { task ->
            val result = task.result
            gotResult = result.error == null
            methodLatch.countDown()
        }
        methodLatch.await(1000000, TimeUnit.SECONDS)
        assertThat(gotResult).isTrue()
    }
}