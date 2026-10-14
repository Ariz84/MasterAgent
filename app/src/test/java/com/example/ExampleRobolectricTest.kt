package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.trading.TradingEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Master Agent", appName)
    }

    @Test
    fun `test forex risk lot sizing calculation`() {
        val result = TradingEngine.calculateForexRisk(
            pair = "EUR/USD",
            balance = 1000.0,
            riskPercent = 2.0,
            entryPrice = 1.0850,
            stopLossPrice = 1.0820,
            takeProfitPrice = 1.0940
        )
        // 30 pips SL, $20 risk, $10/pip standard lot -> 20 / (30 * 10) = ~0.06 lot
        assertEquals(20.0, result.dollarRisk, 0.01)
        assertEquals(30.0, result.stopLossPips, 0.5)
        assertEquals(0.06, result.lotSize, 0.02)
        assertEquals(3.0, result.riskRewardRatio, 0.1)
    }

    @Test
    fun `test quotex pocket option setups have risk warnings`() {
        val setups = TradingEngine.getQuotexPocketOptionSetups()
        assertTrue(setups.isNotEmpty())
        for (s in setups) {
            assertTrue(s.riskWarning.isNotBlank())
        }
    }
}
