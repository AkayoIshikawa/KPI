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

class EmissionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EmissionApp()
        }
    }
}

@Composable
fun EmissionApp() {
    MaterialTheme {
        EmissionScreen()
    }
}

@Composable
fun EmissionScreen() {

    var coal by remember { mutableStateOf("") }
    var mazut by remember { mutableStateOf("") }
    var gas by remember { mutableStateOf("") }

    var result by remember { mutableStateOf<EmissionResults?>(null) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Text("Калькулятор валових викидів", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Input("Вугілля (т)", coal) { coal = it }
        Input("Мазут (т)", mazut) { mazut = it }
        Input("Природний газ (тис.м³)", gas) { gas = it }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = {
            result = calculateEmission(
                coal.toDoubleOrNull() ?: 0.0,
                mazut.toDoubleOrNull() ?: 0.0,
                gas.toDoubleOrNull() ?: 0.0
            )
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Розрахувати")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            coal = ""
            mazut = ""
            gas = ""
            result = null
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Очистити")
        }

        result?.let { r ->
            Spacer(modifier = Modifier.height(20.dp))

            Text("Результати:", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(10.dp))

            Text("Вугілля: kTV=${r.KtvCoal} г/ГДж, ETV=${r.EtvCoal} т")
            Text("Мазут: kTV=${r.KtvMazut} г/ГДж, ETV=${r.EtvMazut} т")
            Text("Газ: kTV=${r.KtvGas} г/ГДж, ETV=${r.EtvGas} т")
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

/* ---------------- ЛОГИКА 1 В 1 ИЗ GO ---------------- */

data class EmissionResults(
    val KtvCoal: String,
    val EtvCoal: String,
    val KtvMazut: String,
    val EtvMazut: String,
    val KtvGas: String,
    val EtvGas: String,
    val Coal: String,
    val Mazut: String,
    val Gas: String
)

/* Константы */
const val QCoal = 20.47
const val ACoal = 25.20
const val GvunCoal = 1.5

const val QMazut = 40.40
const val WMazut = 2.0
const val AMazut = 0.15

const val nzu = 0.985
const val avunCoal = 0.8
const val avunMazut = 1.0

const val ktvGas = 0.0
const val etvGas = 0.0

fun format2(v: Double) = "%.2f".format(v)

fun calculateEmission(coal: Double, mazut: Double, gas: Double): EmissionResults {

    val qMazutRob = QMazut * (100 - WMazut - AMazut) / 100 - 0.025 * WMazut

    val ktvC = (1e6 / QCoal) * avunCoal * (ACoal / (100 - GvunCoal)) * (1 - nzu)
    val etvC = 1e-6 * ktvC * QCoal * coal

    val ktvM = (1e6 / qMazutRob) * avunMazut * (AMazut / 100) * (1 - nzu)
    val etvM = 1e-6 * ktvM * QMazut * mazut

    return EmissionResults(
        KtvCoal = format2(ktvC),
        EtvCoal = format2(etvC),
        KtvMazut = format2(ktvM),
        EtvMazut = format2(etvM),
        KtvGas = "%.0f".format(ktvGas),
        EtvGas = "%.0f".format(etvGas),
        Coal = format2(coal),
        Mazut = format2(mazut),
        Gas = format2(gas)
    )
}
