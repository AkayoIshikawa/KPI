package main

import (
	"html/template"
	"log"
	"net/http"
	"strconv"
)

type FuelData struct{ H, C, S, N, O, W, A float64 }
type OilData struct{ H, C, S, V, O, W, A, Q float64 }

type FuelResults struct {
	Kpc, Kpg               string
	Hc, Cc, Sc, Nc, Oc, Ac string
	Hg, Cg, Sg, Ng, Og     string
	Qph, Qch, Qgh          string
}

type OilResults struct {
	Hp, Cp, Sp, Vp, Op, Ap, Wp, Qp string
}

var tmpl = template.Must(template.New("main").Parse(`
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Калькулятор</title>
<style>
body{font-family:sans-serif;margin:0;padding:20px;background:#f5f5f5;}
.container{max-width:500px;margin:auto;background:white;padding:20px;border-radius:8px;}
h1{text-align:center;}
a{display:inline-block;margin:10px 0;text-decoration:none;color:#007bff;}
form{margin-top:20px;}
label{display:block;margin-top:10px;}
input{width:100%;padding:6px;margin-top:4px;}
button{margin-top:15px;padding:8px 12px;}
.results{margin-top:20px;padding:10px;background:#eee;border-radius:5px;}
</style>
</head>
<body>
<div class="container">
{{if eq .Page "home"}}
<h1>Калькулятори</h1>
<a href="/fuel">Калькулятор палива</a><br>
<a href="/oil">Калькулятор мазути</a>
{{else if eq .Page "fuel"}}
<h1>Калькулятор палива</h1>
<a href="/">← Назад</a>
<form method="POST">
<label>Hᵖ(%): <input name="h" type="number" step="any"></label>
<label>Cᵖ(%): <input name="c" type="number" step="any"></label>
<label>Sᵖ(%): <input name="s" type="number" step="any"></label>
<label>Nᵖ(%): <input name="n" type="number" step="any"></label>
<label>Oᵖ(%): <input name="o" type="number" step="any"></label>
<label>Wᵖ(%): <input name="w" type="number" step="any"></label>
<label>Aᵖ(%): <input name="a" type="number" step="any"></label>
<button type="submit">Розрахувати</button>
</form>
{{if .Fuel}}
<div class="results">
<p>Kpc: {{.Fuel.Kpc}}, Kpg: {{.Fuel.Kpg}}</p>
<p>Hc: {{.Fuel.Hc}}, Cc: {{.Fuel.Cc}}, Sc: {{.Fuel.Sc}}, Nc: {{.Fuel.Nc}}, Oc: {{.Fuel.Oc}}, Ac: {{.Fuel.Ac}}</p>
<p>Hg: {{.Fuel.Hg}}, Cg: {{.Fuel.Cg}}, Sg: {{.Fuel.Sg}}, Ng: {{.Fuel.Ng}}, Og: {{.Fuel.Og}}</p>
<p>Qph: {{.Fuel.Qph}}, Qch: {{.Fuel.Qch}}, Qgh: {{.Fuel.Qgh}}</p>
</div>
{{end}}
{{else if eq .Page "oil"}}
<h1>Калькулятор мазути</h1>
<a href="/">← Назад</a>
<form method="POST">
<label>Hᶢ(%): <input name="h" type="number" step="any"></label>
<label>Cᶢ(%): <input name="c" type="number" step="any"></label>
<label>Sᶢ(%): <input name="s" type="number" step="any"></label>
<label>Oᶢ(%): <input name="o" type="number" step="any"></label>
<label>Vᶢ(мг/кг): <input name="v" type="number" step="any"></label>
<label>Wᶢ(%): <input name="w" type="number" step="any"></label>
<label>Aᶢ(%): <input name="a" type="number" step="any"></label>
<label>Qᶢ(МДж/кг): <input name="q" type="number" step="any"></label>
<button type="submit">Розрахувати</button>
</form>
{{if .Oil}}
<div class="results">
<p>Hp: {{.Oil.Hp}}, Cp: {{.Oil.Cp}}, Sp: {{.Oil.Sp}}</p>
<p>Vp: {{.Oil.Vp}}, Op: {{.Oil.Op}}, Ap: {{.Oil.Ap}}, Wp: {{.Oil.Wp}}</p>
<p>Qp: {{.Oil.Qp}}</p>
</div>
{{end}}
{{end}}
</div>
</body>
</html>
`))

func main() {
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		tmpl.Execute(w, map[string]interface{}{"Page": "home"})
	})

	http.HandleFunc("/fuel", func(w http.ResponseWriter, r *http.Request) {
		var res FuelResults
		if r.Method == http.MethodPost {
			data := FuelData{
				H: parseFloat(r.FormValue("h")),
				C: parseFloat(r.FormValue("c")),
				S: parseFloat(r.FormValue("s")),
				N: parseFloat(r.FormValue("n")),
				O: parseFloat(r.FormValue("o")),
				W: parseFloat(r.FormValue("w")),
				A: parseFloat(r.FormValue("a")),
			}
			res = calculateFuel(data)
		}
		tmpl.Execute(w, map[string]interface{}{"Page": "fuel", "Fuel": res})
	})

	http.HandleFunc("/oil", func(w http.ResponseWriter, r *http.Request) {
		var res OilResults
		if r.Method == http.MethodPost {
			data := OilData{
				H: parseFloat(r.FormValue("h")),
				C: parseFloat(r.FormValue("c")),
				S: parseFloat(r.FormValue("s")),
				O: parseFloat(r.FormValue("o")),
				V: parseFloat(r.FormValue("v")),
				W: parseFloat(r.FormValue("w")),
				A: parseFloat(r.FormValue("a")),
				Q: parseFloat(r.FormValue("q")),
			}
			res = calculateOil(data)
		}
		tmpl.Execute(w, map[string]interface{}{"Page": "oil", "Oil": res})
	})

	log.Println("Сервер запущено на http://localhost:8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}

func parseFloat(s string) float64 {
	f, _ := strconv.ParseFloat(s, 64)
	return f
}

func formatFloat(f float64) string {
	return strconv.FormatFloat(f, 'f', 2, 64)
}

func calculateFuel(d FuelData) FuelResults {
	Kpc := 100 / (100 - d.W)
	Kpg := 100 / (100 - d.W - d.A)
	Hc, Cc, Sc, Nc, Oc, Ac := d.H*Kpc, d.C*Kpc, d.S*Kpc, d.N*Kpc, d.O*Kpc, d.A*Kpc
	Hg, Cg, Sg, Ng, Og := d.H*Kpg, d.C*Kpg, d.S*Kpg, d.N*Kpg, d.O*Kpg
	Qph := 339*d.C + 1030*d.H - 108.8*(d.O-d.S) - 25*d.W
	Qch := (Qph/1000 + 0.025*d.W) * Kpc
	Qgh := (Qph/1000 + 0.025*d.W) * Kpg
	return FuelResults{
		Kpc: formatFloat(Kpc), Kpg: formatFloat(Kpg),
		Hc: formatFloat(Hc), Cc: formatFloat(Cc), Sc: formatFloat(Sc),
		Nc: formatFloat(Nc), Oc: formatFloat(Oc), Ac: formatFloat(Ac),
		Hg: formatFloat(Hg), Cg: formatFloat(Cg), Sg: formatFloat(Sg),
		Ng: formatFloat(Ng), Og: formatFloat(Og),
		Qph: formatFloat(Qph), Qch: formatFloat(Qch), Qgh: formatFloat(Qgh),
	}
}

func calculateOil(d OilData) OilResults {
	Hp := d.H * (100 - d.W - d.A) / 100
	Cp := d.C * (100 - d.W - d.A) / 100
	Sp := d.S * (100 - d.W - d.A) / 100
	Op := d.O * (100 - d.W - d.A) / 100
	Ap := d.A * (100 - d.W) / 100
	Vp := d.V * (100 - d.W) / 100
	Qp := d.Q*(100-d.W-d.A)/100 - 0.025*d.W
	return OilResults{
		Hp: formatFloat(Hp), Cp: formatFloat(Cp), Sp: formatFloat(Sp),
		Op: formatFloat(Op), Ap: formatFloat(Ap), Wp: formatFloat(d.W),
		Vp: formatFloat(Vp), Qp: formatFloat(Qp),
	}
}
