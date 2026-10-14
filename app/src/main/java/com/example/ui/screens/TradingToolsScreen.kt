package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TradeJournalEntity
import com.example.trading.TradingEngine
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradingToolsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Forex Risk Calc", "Quotex & Pocket Opt", "Trade Journal")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Trading & Risk Specialist", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("فاریکس، لاٹ سائز اور ٹریڈنگ جرنل", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("trading_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 12.sp, maxLines = 1) }
                    )
                }
            }

            when (selectedTab) {
                0 -> ForexCalculatorView(viewModel)
                1 -> BinaryOptionsEducationView()
                2 -> TradeJournalView(viewModel)
            }
        }
    }
}

@Composable
fun ForexCalculatorView(viewModel: MainViewModel) {
    val context = LocalContext.current
    var pair by remember { mutableStateOf("EUR/USD") }
    var balanceText by remember { mutableStateOf("1000") }
    var riskPercentText by remember { mutableStateOf("2.0") }
    var entryPriceText by remember { mutableStateOf("1.0850") }
    var stopLossPriceText by remember { mutableStateOf("1.0820") }
    var takeProfitPriceText by remember { mutableStateOf("1.0940") }

    val forexResult by viewModel.forexResult.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Forex Position Size & Lot Calculator",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Professional risk management formula: Balance × Risk% / (SL Pips × Pip Value)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                // Currency pair chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("EUR/USD", "GBP/USD", "USD/JPY", "XAU/USD").forEach { p ->
                        FilterChip(
                            selected = pair == p,
                            onClick = {
                                pair = p
                                if (p == "USD/JPY") {
                                    entryPriceText = "152.50"
                                    stopLossPriceText = "152.10"
                                    takeProfitPriceText = "153.70"
                                } else if (p == "XAU/USD") {
                                    entryPriceText = "2650.0"
                                    stopLossPriceText = "2642.0"
                                    takeProfitPriceText = "2674.0"
                                } else {
                                    entryPriceText = "1.0850"
                                    stopLossPriceText = "1.0820"
                                    takeProfitPriceText = "1.0940"
                                }
                            },
                            label = { Text(p, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = balanceText,
                        onValueChange = { balanceText = it },
                        label = { Text("Balance ($)", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f).testTag("calc_balance_input")
                    )
                    OutlinedTextField(
                        value = riskPercentText,
                        onValueChange = { riskPercentText = it },
                        label = { Text("Risk %", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f).testTag("calc_risk_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = entryPriceText,
                        onValueChange = { entryPriceText = it },
                        label = { Text("Entry Price", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f).testTag("calc_entry_input")
                    )
                    OutlinedTextField(
                        value = stopLossPriceText,
                        onValueChange = { stopLossPriceText = it },
                        label = { Text("Stop Loss", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f).testTag("calc_sl_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = takeProfitPriceText,
                    onValueChange = { takeProfitPriceText = it },
                    label = { Text("Take Profit Price", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("calc_tp_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val bal = balanceText.toDoubleOrNull() ?: 1000.0
                        val risk = riskPercentText.toDoubleOrNull() ?: 2.0
                        val entry = entryPriceText.toDoubleOrNull() ?: 1.0850
                        val sl = stopLossPriceText.toDoubleOrNull() ?: 1.0820
                        val tp = takeProfitPriceText.toDoubleOrNull() ?: 1.0940
                        viewModel.calculateForex(pair, bal, risk, entry, sl, tp)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("calculate_lot_button")
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Calculate Risk & Lot Size", fontWeight = FontWeight.Bold)
                }
            }
        }

        forexResult?.let { result ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Calculation Outcome",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = result.pair,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Recommended Lot Size:", fontSize = 11.sp)
                            Text("${String.format(Locale.US, "%.2f", result.lotSize)} Lots", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Max Dollar Risk:", fontSize = 11.sp)
                            Text("$${String.format(Locale.US, "%.2f", result.dollarRisk)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Stop Loss: ${String.format(Locale.US, "%.1f", result.stopLossPips)} pips", fontSize = 12.sp)
                        Text("Target: ${String.format(Locale.US, "%.1f", result.takeProfitPips)} pips", fontSize = 12.sp)
                        Text("R:R: 1:${String.format(Locale.US, "%.2f", result.riskRewardRatio)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = result.summaryUrdu,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FilledTonalButton(
                        onClick = {
                            val entry = entryPriceText.toDoubleOrNull() ?: 1.0
                            val sl = stopLossPriceText.toDoubleOrNull() ?: 1.0
                            val tp = takeProfitPriceText.toDoubleOrNull() ?: 1.0
                            val direction = if (tp > entry) "BUY" else "SELL"
                            viewModel.logTrade(
                                pair = result.pair,
                                direction = direction,
                                entryPrice = entry,
                                stopLoss = sl,
                                takeProfit = tp,
                                lotSize = result.lotSize,
                                riskAmountUsd = result.dollarRisk,
                                status = "OPEN",
                                platform = "Forex MT5/MT4",
                                strategyNotes = "Calculated risk 1:${String.format(Locale.US, "%.1f", result.riskRewardRatio)}"
                            )
                            Toast.makeText(context, "Trade logged to Journal!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("save_to_journal_button")
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Trade to Journal")
                    }
                }
            }
        }
    }
}

@Composable
fun BinaryOptionsEducationView() {
    val setups = remember { TradingEngine.getQuotexPocketOptionSetups() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // High Risk Warning Banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFFEF2F2),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(14.dp)) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Risk Warning",
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Quotex & Pocket Option Strict Disclaimer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF991B1B)
                    )
                    Text(
                        text = "Binary options carry severe financial risk with fixed-payout asymmetry (loss is 100%, payout is ~80-90%). Master Agent does NOT invent automated bot signals, hack scripts, or guarantee profits. Only permitted price-action setups for Demo practice are outlined below.",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFF7F1D1D),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Permitted Technical Price-Action Setups",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Text(
            text = "Master Agent rules for disciplined demo practice on Quotex / Pocket Option",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        setups.forEach { setup ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = setup.setupName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = setup.timeframe,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Trigger: ${setup.triggerCondition}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Execution Checklist:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    setup.confirmationRules.forEach { rule ->
                        Row(modifier = Modifier.padding(top = 2.dp)) {
                            Text("• ", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text(rule, fontSize = 11.sp, lineHeight = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Discipline note: ${setup.riskWarning}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun TradeJournalView(viewModel: MainViewModel) {
    val trades by viewModel.trades.collectAsState()

    if (trades.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.EventNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No trades logged yet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Calculate your Forex position size and tap 'Save Trade to Journal' to keep a disciplined track of your backtesting.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Logged Trades", fontSize = 11.sp)
                            Text("${trades.size} Records", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Active Open", fontSize = 11.sp)
                            Text("${trades.count { it.status == "OPEN" }} Trades", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            items(trades, key = { it.id }) { trade ->
                TradeJournalItem(trade = trade, onDelete = { viewModel.deleteTrade(trade) })
            }
        }
    }
}

@Composable
fun TradeJournalItem(
    trade: TradeJournalEntity,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(trade.timestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (trade.direction == "BUY") Color(0xFF10B981) else Color(0xFFEF4444)
                    ) {
                        Text(
                            text = trade.direction,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = trade.pair, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Entry: ${trade.entryPrice}", fontSize = 12.sp)
                Text("SL: ${trade.stopLoss}", fontSize = 12.sp)
                Text("TP: ${trade.takeProfit}", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Lot Size: ${trade.lotSize}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("Risk: $${trade.riskAmountUsd}", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                Text(dateStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }

            if (trade.strategyNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Notes: ${trade.strategyNotes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
            }
        }
    }
}
