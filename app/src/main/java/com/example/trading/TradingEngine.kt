package com.example.trading

import java.util.Locale
import kotlin.math.abs

data class ForexRiskResult(
    val pair: String,
    val balance: Double,
    val riskPercent: Double,
    val dollarRisk: Double,
    val stopLossPips: Double,
    val takeProfitPips: Double,
    val lotSize: Double,
    val riskRewardRatio: Double,
    val potentialProfitUsd: Double,
    val summaryUrdu: String,
    val summaryEnglish: String
)

data class BinaryOptionEducation(
    val platform: String,
    val timeframe: String,
    val setupName: String,
    val triggerCondition: String,
    val confirmationRules: List<String>,
    val riskWarning: String
)

object TradingEngine {

    fun calculateForexRisk(
        pair: String,
        balance: Double,
        riskPercent: Double,
        entryPrice: Double,
        stopLossPrice: Double,
        takeProfitPrice: Double
    ): ForexRiskResult {
        val cleanPair = pair.uppercase().replace("/", "").trim()
        val isJpy = cleanPair.endsWith("JPY")
        val isGold = cleanPair.contains("XAU")

        val pipSize = when {
            isGold -> 0.10
            isJpy -> 0.01
            else -> 0.0001
        }

        val pipValueStandardLot = when {
            isGold -> 10.0 // 100 oz per lot, $0.10 is $10
            isJpy -> 9.0 // approximate JPY pip value
            else -> 10.0 // standard lot for EURUSD etc
        }

        val slDistance = abs(entryPrice - stopLossPrice)
        val tpDistance = abs(takeProfitPrice - entryPrice)

        val slPips = if (slDistance > 0) slDistance / pipSize else 1.0
        val tpPips = if (tpDistance > 0) tpDistance / pipSize else 1.0

        val dollarRisk = balance * (riskPercent / 100.0)
        val rawLot = dollarRisk / (slPips * pipValueStandardLot)
        // round lot to 2 decimals (micro lot minimum 0.01)
        val lotSize = kotlin.math.max(0.01, (rawLot * 100).toInt() / 100.0)

        val rrRatio = if (slPips > 0) tpPips / slPips else 0.0
        val potentialProfitUsd = (tpPips * pipValueStandardLot * lotSize)

        val summaryUrdu = "Bhai, $pair ke liye aapka account balance $$balance hai aur aapka risk $riskPercent% ($${String.format(Locale.US, "%.2f", dollarRisk)}) hai. " +
                "Stop loss $${String.format(Locale.US, "%.1f", slPips)} pips par hai. Recommended Lot Size: ${String.format(Locale.US, "%.2f", lotSize)}. " +
                "Risk-to-Reward ratio 1:${String.format(Locale.US, "%.2f", rrRatio)} ban raha hai."

        val summaryEnglish = "Forex Risk Sizing: For $pair with $$balance balance & $riskPercent% risk ($${String.format(Locale.US, "%.2f", dollarRisk)}), " +
                "SL is ${String.format(Locale.US, "%.1f", slPips)} pips. Recommended Lot Size: ${String.format(Locale.US, "%.2f", lotSize)} with R:R of 1:${String.format(Locale.US, "%.2f", rrRatio)}."

        return ForexRiskResult(
            pair = pair,
            balance = balance,
            riskPercent = riskPercent,
            dollarRisk = dollarRisk,
            stopLossPips = slPips,
            takeProfitPips = tpPips,
            lotSize = lotSize,
            riskRewardRatio = rrRatio,
            potentialProfitUsd = potentialProfitUsd,
            summaryUrdu = summaryUrdu,
            summaryEnglish = summaryEnglish
        )
    }

    fun getQuotexPocketOptionSetups(): List<BinaryOptionEducation> {
        return listOf(
            BinaryOptionEducation(
                platform = "Quotex / Pocket Option (Demo/Education)",
                timeframe = "M1 or M5",
                setupName = "Support/Resistance Rejection + Pin Bar",
                triggerCondition = "Price reaches a major daily or H1 key horizontal level, rejects with a long wick candle.",
                confirmationRules = listOf(
                    "Wait for candle close outside or at key level (never enter before close).",
                    "RSI must be in overbought (>70 for Call/Sell reversal) or oversold (<30 for Put/Buy reversal).",
                    "Trend filter: Only trade in the direction of the higher timeframe 50 EMA.",
                    "Expiry rule: Match expiry strictly to 2 to 3 candles (e.g. 2-3 mins on M1)."
                ),
                riskWarning = "Binary options carry high mathematical house edge. Use strictly in Demo mode for pattern practice. Master Agent never supplies automated bot signals."
            ),
            BinaryOptionEducation(
                platform = "Quotex / Pocket Option (Demo/Education)",
                timeframe = "M5",
                setupName = "Engulfing Break & Retest",
                triggerCondition = "Strong momentum candle breaks out of consolidation, next candle retests broken level with small body.",
                confirmationRules = listOf(
                    "Breakout candle must have large body with above-average volume.",
                    "Retest candle must fail to penetrate back into previous consolidation range.",
                    "Trade with the ongoing trend direction only.",
                    "Maximum 1-2% account exposure per demo test trade."
                ),
                riskWarning = "No setup has 100% win rate. Strict discipline and demo backtesting are mandatory before any live consideration."
            )
        )
    }
}
