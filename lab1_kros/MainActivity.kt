package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Composable
fun App() {
    MaterialTheme {
        var page by remember { mutableStateOf("home") }

        when (page) {
            "home" -> HomeScreen(
                onFuelClick = { page = "fuel" },
                onOilClick = { page = "oil" }
            )

            "fuel" -> FuelScreen(onBack = { page = "home" })

            "oil" -> OilScreen(onBack = { page = "home" })
        }
    }
}

@Composable
fun HomeScreen(onFuelClick: () -> Unit, onOilClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Калькулятори", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onFuelClick, modifier = Modifier.fillMaxWidth()) {
            Text("Калькулятор палива")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = onOilClick, modifier = Modifier.fillMaxWidth()) {
            Text("Калькулятор мазути")
        }
    }
}

@Composable
fun FuelScreen(onBack: () -> Unit) {
    var h by remember { mutableStateOf("") }
    var c by remember { mutableStateOf("") }
    var s by remember { mutableStateOf("") }
    var n by remember { mutableStateOf("") }
    var o by remember { mutableStateOf("") }
    var w by remember { mutableStateOf("") }
    var a by remember { mutableStateOf("") }

    var result by remember { mutableStateOf<FuelResults?>(null) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text("Калькулятор палива", style = MaterialTheme.typography.titleLarge)

        TextButton(onClick = onBack) { Text("← Назад") }

        Input("Hᵖ(%)", h) { h = it }
        Input("Cᵖ(%)", c) { c = it }
        Input("Sᵖ(%)", s) { s = it }
        Input("Nᵖ(%)", n) { n = it }
        Input("Oᵖ(%)", o) { o = it }
        Input("Wᵖ(%)", w) { w = it }
        Input("Aᵖ(%)", a) { a = it }

        Button(onClick = {
            val data = FuelData(
                h.toDoubleOrNull() ?: 0.0,
                c.toDoubleOrNull() ?: 0.0,
                s.toDoubleOrNull() ?: 0.0,
                n.toDoubleOrNull() ?: 0.0,
                o.toDoubleOrNull() ?: 0.0,
                w.toDoubleOrNull() ?: 0.0,
                a.toDoubleOrNull() ?: 0.0
            )
            result = calculateFuel(data)
        }) {
            Text("Розрахувати")
        }

        result?.let { r ->
            Spacer(modifier = Modifier.height(20.dp))

            Text("Kpc: ${r.Kpc}, Kpg: ${r.Kpg}")
            Text("Hc: ${r.Hc}, Cc: ${r.Cc}, Sc: ${r.Sc}")
            Text("Nc: ${r.Nc}, Oc: ${r.Oc}, Ac: ${r.Ac}")
            Text("Hg: ${r.Hg}, Cg: ${r.Cg}, Sg: ${r.Sg}")
            Text("Ng: ${r.Ng}, Og: ${r.Og}")
            Text("Qph: ${r.Qph}, Qch: ${r.Qch}, Qgh: ${r.Qgh}")
        }
    }
}

@Composable
fun OilScreen(onBack: () -> Unit) {
    var h by remember { mutableStateOf("") }
    var c by remember { mutableStateOf("") }
    var s by remember { mutableStateOf("") }
    var o by remember { mutableStateOf("") }
    var v by remember { mutableStateOf("") }
    var w by remember { mutableStateOf("") }
    var a by remember { mutableStateOf("") }
    var q by remember { mutableStateOf("") }

    var result by remember { mutableStateOf<OilResults?>(null) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text("Калькулятор мазути", style = MaterialTheme.typography.titleLarge)

        TextButton(onClick = onBack) { Text("← Назад") }

        Input("Hᶢ(%)", h) { h = it }
        Input("Cᶢ(%)", c) { c = it }
        Input("Sᶢ(%)", s) { s = it }
        Input("Oᶢ(%)", o) { o = it }
        Input("Vᶢ(мг/кг)", v) { v = it }
        Input("Wᶢ(%)", w) { w = it }
        Input("Aᶢ(%)", a) { a = it }
        Input("Qᶢ(МДж/кг)", q) { q = it }

        Button(onClick = {
            val data = OilData(
                h.toDoubleOrNull() ?: 0.0,
                c.toDoubleOrNull() ?: 0.0,
                s.toDoubleOrNull() ?: 0.0,
                v.toDoubleOrNull() ?: 0.0,
                o.toDoubleOrNull() ?: 0.0,
                w.toDoubleOrNull() ?: 0.0,
                a.toDoubleOrNull() ?: 0.0,
                q.toDoubleOrNull() ?: 0.0
            )
            result = calculateOil(data)
        }) {
            Text("Розрахувати")
        }

        result?.let { r ->
            Spacer(modifier = Modifier.height(20.dp))

            Text("Hp: ${r.Hp}, Cp: ${r.Cp}, Sp: ${r.Sp}")
            Text("Vp: ${r.Vp}, Op: ${r.Op}")
            Text("Ap: ${r.Ap}, Wp: ${r.Wp}")
            Text("Qp: ${r.Qp}")
        }
    }
}

@Composable
fun Input(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

/* -------------------- DATA + LOGIC -------------------- */

data class FuelData(
    val H: Double, val C: Double, val S: Double,
    val N: Double, val O: Double, val W: Double, val A: Double
)

data class OilData(
    val H: Double, val C: Double, val S: Double,
    val V: Double, val O: Double, val W: Double, val A: Double, val Q: Double
)

data class FuelResults(
    val Kpc: String, val Kpg: String,
    val Hc: String, val Cc: String, val Sc: String,
    val Nc: String, val Oc: String, val Ac: String,
    val Hg: String, val Cg: String, val Sg: String,
    val Ng: String, val Og: String,
    val Qph: String, val Qch: String, val Qgh: String
)

data class OilResults(
    val Hp: String, val Cp: String, val Sp: String,
    val Vp: String, val Op: String, val Ap: String,
    val Wp: String, val Qp: String
)

fun format(f: Double) = "%.2f".format(f)

fun calculateFuel(d: FuelData): FuelResults {
    val Kpc = 100 / (100 - d.W)
    val Kpg = 100 / (100 - d.W - d.A)

    val Qph = 339 * d.C + 1030 * d.H - 108.8 * (d.O - d.S) - 25 * d.W
    val Qch = (Qph / 1000 + 0.025 * d.W) * Kpc
    val Qgh = (Qph / 1000 + 0.025 * d.W) * Kpg

    return FuelResults(
        format(Kpc), format(Kpg),
        format(d.H * Kpc), format(d.C * Kpc), format(d.S * Kpc),
        format(d.N * Kpc), format(d.O * Kpc), format(d.A * Kpc),
        format(d.H * Kpg), format(d.C * Kpg), format(d.S * Kpg),
        format(d.N * Kpg), format(d.O * Kpg),
        format(Qph), format(Qch), format(Qgh)
    )
}

fun calculateOil(d: OilData): OilResults {
    return OilResults(
        format(d.H * (100 - d.W - d.A) / 100),
        format(d.C * (100 - d.W - d.A) / 100),
        format(d.S * (100 - d.W - d.A) / 100),
        format(d.V * (100 - d.W) / 100),
        format(d.O * (100 - d.W - d.A) / 100),
        format(d.A * (100 - d.W) / 100),
        format(d.W),
        format(d.Q * (100 - d.W - d.A) / 100 - 0.025 * d.W)
    )
}
