package main

import (
	"fmt"
	"html/template"
	"log"
	"net/http"
	"strconv"
)

type EmissionResults struct {
	KtvCoal, EtvCoal, KtvMazut, EtvMazut, KtvGas, EtvGas, Coal, Mazut, Gas string
}

// Константи
const (
	QCoal, ACoal, GvunCoal       = 20.47, 25.20, 1.5
	QMazut, WMazut, AMazut       = 40.40, 2.0, 0.15
	nzu, avunCoal, avunMazut     = 0.985, 0.8, 1.0
	ktvGas, etvGas               = 0.0, 0.0
)

func calculate(coal, mazut, gas float64) EmissionResults {
	// Розрахунок робочої теплоти згоряння мазуту
	qMazutRob := QMazut * (100 - WMazut - AMazut) / 100 - 0.025*WMazut

	// Вугілля
	ktvC := (1e6 / QCoal) * avunCoal * (ACoal / (100 - GvunCoal)) * (1 - nzu)
	etvC := 1e-6 * ktvC * QCoal * coal

	// Мазут
	ktvM := (1e6 / qMazutRob) * avunMazut * (AMazut / 100) * (1 - nzu)
	etvM := 1e-6 * ktvM * QMazut * mazut

	return EmissionResults{
		KtvCoal:  fmt.Sprintf("%.2f", ktvC),
		EtvCoal:  fmt.Sprintf("%.2f", etvC),
		KtvMazut: fmt.Sprintf("%.2f", ktvM),
		EtvMazut: fmt.Sprintf("%.2f", etvM),
		KtvGas:   fmt.Sprintf("%.0f", ktvGas),
		EtvGas:   fmt.Sprintf("%.0f", etvGas),
		Coal:     fmt.Sprintf("%.2f", coal),
		Mazut:    fmt.Sprintf("%.2f", mazut),
		Gas:      fmt.Sprintf("%.2f", gas),
	}
}

func handler(w http.ResponseWriter, r *http.Request) {
	tmpl := template.Must(template.New("page").Parse(`
<!DOCTYPE html>
<html lang="uk">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Калькулятор викидів</title>
<style>
body{font-family:sans-serif;margin:20px;background:#f0f0f0}
input,button{padding:5px;margin:5px} .results{margin-top:20px;padding:10px;background:#fff;border:1px solid #ccc}
</style>
</head>
<body>
<h2>Калькулятор валових викидів</h2>
<form method="POST">
<p>Вугілля (т): <input type="number" step="0.01" name="coal" value="{{.Coal}}"></p>
<p>Мазут (т): <input type="number" step="0.01" name="mazut" value="{{.Mazut}}"></p>
<p>Природний газ (тис.м³): <input type="number" step="0.01" name="gas" value="{{.Gas}}"></p>
<button type="submit">Розрахувати</button>
<button type="button" onclick="window.location='/'">Очистити</button>
</form>

{{if .KtvCoal}}
<div class="results">
<h3>Результати:</h3>
<p>Вугілля: kTV={{.KtvCoal}} г/ГДж, ETV={{.EtvCoal}} т</p>
<p>Мазут: kTV={{.KtvMazut}} г/ГДж, ETV={{.EtvMazut}} т</p>
<p>Газ: kTV={{.KtvGas}} г/ГДж, ETV={{.EtvGas}} т</p>
</div>
{{end}}
</body>
</html>
`))

	var data EmissionResults
	if r.Method == http.MethodPost {
		r.ParseForm()
		coal, _ := strconv.ParseFloat(r.FormValue("coal"), 64)
		mazut, _ := strconv.ParseFloat(r.FormValue("mazut"), 64)
		gas, _ := strconv.ParseFloat(r.FormValue("gas"), 64)
		data = calculate(coal, mazut, gas)
	}

	tmpl.Execute(w, data)
}

func main() {
	http.HandleFunc("/", handler)
	log.Println("Сервер запущено на http://localhost:8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}
