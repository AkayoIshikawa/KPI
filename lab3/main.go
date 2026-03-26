package main

import (
	"fmt"
	"html/template"
	"math"
	"net/http"
	"strconv"
)

type CalculationResults struct {
	Pd, DeltaW1Per, W1, D1, W2, P1, Sh1, Loss       string
	DeltaW2Per, W3, D2, W4, P2, Sh2, P             string
}

type PageData struct {
	Results *CalculationResults
	Pc, Sigma1, Sigma2, Price string
}

func calculatePd(sigma float64) float64 {
	if sigma <= 0 {
		return 0
	}
	expDegree := 0.0625 / (2 * sigma * sigma)
	return math.Exp(expDegree) / (sigma * math.Sqrt(2*math.Pi))
}

func calculate(pc, sigma1, sigma2, price float64) *CalculationResults {
	pd := calculatePd(sigma1)
	deltaW1 := pd * 0.5
	deltaW1Per := deltaW1 * 100
	w1 := pc * 24 * deltaW1
	d1 := (1 - deltaW1) * 100
	w2 := pc * 24 * (1 - deltaW1)
	p1 := w1 * price
	sh1 := w2 * price
	loss := sh1 - p1

	deltaW2 := 0.68
	deltaW2Per := deltaW2 * 100
	w3 := pc * 24 * deltaW2
	d2 := (1 - deltaW2) * 100
	w4 := pc * 24 * (1 - deltaW2)
	p2 := w3 * price
	sh2 := w4 * price
	profit := p2 - sh2

	f := func(v float64, dec int) string { return strconv.FormatFloat(v, 'f', dec, 64) }

	return &CalculationResults{
		Pd: f(pd, 2), DeltaW1Per: f(deltaW1Per, 0), W1: f(w1, 2), D1: f(d1, 0), W2: f(w2, 2),
		P1: f(p1, 2), Sh1: f(sh1, 2), Loss: f(loss, 2),
		DeltaW2Per: f(deltaW2Per, 0), W3: f(w3, 2), D2: f(d2, 0), W4: f(w4, 2),
		P2: f(p2, 2), Sh2: f(sh2, 2), P: f(profit, 2),
	}
}

func handler(w http.ResponseWriter, r *http.Request) {
	tmpl := template.Must(template.New("page").Parse(`
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Калькулятор прибутку сонячних станцій</title>
<style>
body{font-family:sans-serif;margin:20px} input,button{margin:5px;padding:5px} .result{margin-top:15px;padding:10px;border:1px solid #ccc;background:#f9f9f9}
</style>
</head>
<body>
<h2>Калькулятор прибутку сонячних станцій</h2>
<form method="POST">
Введіть Pc (МВт): <input name="pc" type="number" step="any" value="{{.Pc}}" required><br>
Sigma1 (МВт): <input name="sigma1" type="number" step="any" value="{{.Sigma1}}" required><br>
Sigma2 (МВт): <input name="sigma2" type="number" step="any" value="{{.Sigma2}}" required><br>
Ціна (грн/кВт*год): <input name="price" type="number" step="any" value="{{.Price}}" required><br>
<button type="submit">Розрахувати</button>
<button type="button" onclick="window.location='/'">Очистити</button>
</form>

{{if .Results}}
<div class="result">
<h3>До вдосконалення:</h3>
Pd: {{.Results.Pd}}, DeltaW1: {{.Results.DeltaW1Per}}%, W1: {{.Results.W1}}, D1: {{.Results.D1}}%, W2: {{.Results.W2}}<br>
P1: {{.Results.P1}}, Sh1: {{.Results.Sh1}}, Loss: {{.Results.Loss}}<br>
<h3>Після вдосконалення:</h3>
DeltaW2: {{.Results.DeltaW2Per}}%, W3: {{.Results.W3}}, D2: {{.Results.D2}}%, W4: {{.Results.W4}}<br>
P2: {{.Results.P2}}, Sh2: {{.Results.Sh2}}, Прибуток: {{.Results.P}}
</div>
{{end}}
</body>
</html>
`))

	data := PageData{}
	if r.Method == http.MethodPost {
		r.ParseForm()
		pc, _ := strconv.ParseFloat(r.FormValue("pc"), 64)
		sigma1, _ := strconv.ParseFloat(r.FormValue("sigma1"), 64)
		sigma2, _ := strconv.ParseFloat(r.FormValue("sigma2"), 64)
		price, _ := strconv.ParseFloat(r.FormValue("price"), 64)
		data.Results = calculate(pc, sigma1, sigma2, price)
		data.Pc = r.FormValue("pc")
		data.Sigma1 = r.FormValue("sigma1")
		data.Sigma2 = r.FormValue("sigma2")
		data.Price = r.FormValue("price")
	}

	tmpl.Execute(w, data)
}

func main() {
	http.HandleFunc("/", handler)
	fmt.Println("Сервер запущено на http://localhost:8080")
	http.ListenAndServe(":8080", nil)
}
