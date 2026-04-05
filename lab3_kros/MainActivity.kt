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
import kotlin.math.exp
import kotlin.math.sqrt

class SolarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SolarApp()
        }
    }
}

@Composable
fun SolarApp() {
    MaterialTheme {
        SolarScreen()
    }
}

@Composable
fun SolarScreen() {

    var pc by remember { mutableStateOf("") }
    var sigma1 by remember { mutableStateOf("") }
    var sigma2 by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    var result by remember { mutableStateOf<CalculationResults?>(null) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Text("Калькулятор прибутку сонячних станцій",
            style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Input("Pc (МВт)", pc) { pc = it }
        Input("Sigma1 (МВт)", sigma1) { sigma1 = it }
        Input("Sigma2 (МВт)", sigma2) { sigma2 = it }
        Input("Ціна (грн/кВт*год)", price) { price = it }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = {
            result = calculate(
                pc.toDoubleOrNull() ?: 0.0,
                sigma1.toDoubleOrNull() ?: 0.0,
                sigma2.toDoubleOrNull() ?: 0.0,
                price.toDoubleOrNull() ?: 0.0
            )
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Розрахувати")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            pc = ""
            sigma1 = ""
            sigma2 = ""
            price = ""
            result = null
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Очистити")
        }

        result?.let { r ->
            Spacer(modifier = Modifier.height(20.dp))

            Text("До вдосконалення:", style = MaterialTheme.typography.titleMedium)

            Text("Pd: ${r.Pd}")
            Text("DeltaW1: ${r.DeltaW1Per}%")
            Text("W1: ${r.W1}")
            Text("D1: ${r.D1}%")
            Text("W2: ${r.W2}")
            Text("P1: ${r.P1}")
            Text("Sh1: ${r.Sh1}")
            Text("Loss: ${r.Loss}")

            Spacer(modifier = Modifier.height(12.dp))

            Text("Після вдосконалення:", style = MaterialTheme.typography.titleMedium)

            Text("DeltaW2: ${r.DeltaW2Per}%")
            Text("W3: ${r.W3}")
            Text("D2: ${r.D2}%")
            Text("W4: ${r.W4}")
            Text("P2: ${r.P2}")
            Text("Sh2: ${r.Sh2}")
            Text("Прибуток: ${r.P}")
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

/* ---------------- ЛОГИКА ИЗ GO ---------------- */

data class CalculationResults(
    val Pd: String,
    val DeltaW1Per: String,
    val W1: String,
    val D1: String,
    val W2: String,
    val P1: String,
    val Sh1: String,
    val Loss: String,
    val DeltaW2Per: String,
    val W3: String,
    val D2: String,
    val W4: String,
    val P2: String,
    val Sh2: String,
    val P: String
)

fun format(v: Double, dec: Int) = "%.${dec}f".format(v)

fun calculatePd(sigma: Double): Double {
    if (sigma <= 0) return 0.0
    val expDegree = 0.0625 / (2 * sigma * sigma)
    return exp(expDegree) / (sigma * sqrt(2 * Math.PI))
}

fun calculate(pc: Double, sigma1: Double, sigma2: Double, price: Double): CalculationResults {

    val pd = calculatePd(sigma1)

    val deltaW1 = pd * 0.5
    val deltaW1Per = deltaW1 * 100
    val w1 = pc * 24 * deltaW1
    val d1 = (1 - deltaW1) * 100
    val w2 = pc * 24 * (1 - deltaW1)
    val p1 = w1 * price
    val sh1 = w2 * price
    val loss = sh1 - p1

    val deltaW2 = 0.68
    val deltaW2Per = deltaW2 * 100
    val w3 = pc * 24 * deltaW2
    val d2 = (1 - deltaW2) * 100
    val w4 = pc * 24 * (1 - deltaW2)
    val p2 = w3 * price
    val sh2 = w4 * price
    val profit = p2 - sh2

    return CalculationResults(
        Pd = format(pd, 2),
        DeltaW1Per = format(deltaW1Per, 0),
        W1 = format(w1, 2),
        D1 = format(d1, 0),
        W2 = format(w2, 2),
        P1 = format(p1, 2),
        Sh1 = format(sh1, 2),
        Loss = format(loss, 2),
        DeltaW2Per = format(deltaW2Per, 0),
        W3 = format(w3, 2),
        D2 = format(d2, 0),
        W4 = format(w4, 2),
        P2 = format(p2, 2),
        Sh2 = format(sh2, 2),
        P = format(profit, 2)
    )
}
