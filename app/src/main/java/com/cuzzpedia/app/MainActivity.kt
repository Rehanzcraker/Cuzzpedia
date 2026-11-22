package com.cuzzpedia.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONObject
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.io.OutputStreamWriter
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val purple = Color.rgb(104, 67, 220)
    private val dark = Color.rgb(35, 29, 55)
    private val worker = Executors.newSingleThreadExecutor()
    private var token = ""
    private var apiBase = "https://YOUR-DOMAIN.com/api"
    private lateinit var root: LinearLayout
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showAuth()
    }

    private fun baseLayout(title: String) {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 24, 22, 20)
            setBackgroundColor(Color.rgb(247, 246, 252))
        }
        val head = TextView(this).apply {
            text = title
            textSize = 25f
            setTextColor(dark)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 8, 0, 18)
        }
        root.addView(head)
        status = TextView(this).apply { setTextColor(Color.DKGRAY); textSize = 13f }
        setContentView(root)
    }

    private fun field(hint: String, password: Boolean = false): EditText {
        return EditText(this).apply {
            this.hint = hint
            textSize = 15f
            setSingleLine(true)
            if (password) inputType = 129
            setPadding(16, 8, 16, 8)
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE); cornerRadius = 18f
                setStroke(1, Color.rgb(225, 221, 239))
            }
        }
    }

    private fun button(label: String, action: () -> Unit): Button {
        return Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 15f
            setTextColor(Color.WHITE)
            backgroundTintList = android.content.res.ColorStateList.valueOf(purple)
            setOnClickListener { action() }
        }
    }

    private fun showAuth() {
        baseLayout("CUZZPEDIA")
        root.addView(TextView(this).apply {
            text = "SMM PANEL  •  PPOB\nSatu aplikasi untuk kebutuhan digital kamu."
            textSize = 16f; setTextColor(dark); setPadding(0, 0, 0, 22)
        })
        val email = field("Email / username")
        val pass = field("Password", true)
        root.addView(email); root.addView(pass)
        root.addView(button("MASUK") {
            request("POST", "login", JSONObject().put("identity", email.text.toString()).put("password", pass.text.toString())) { j ->
                token = j.optString("token")
                getDashboard()
            }
        })
        root.addView(button("DAFTAR AKUN") {
            showRegister()
        })
        root.addView(status)
        root.addView(TextView(this).apply {
            text = "Pengaturan awal: ubah alamat API di MainActivity.kt sebelum digunakan."
            textSize = 12f; setTextColor(Color.GRAY); setPadding(0, 18, 0, 0)
        })
    }

    private fun showRegister() {
        baseLayout("Buat akun")
        val name = field("Nama lengkap")
        val username = field("Username")
        val email = field("Email")
        val pass = field("Password minimal 8 karakter", true)
        listOf(name, username, email, pass).forEach { root.addView(it) }
        root.addView(button("DAFTAR") {
            request("POST", "register", JSONObject()
                .put("name", name.text.toString()).put("username", username.text.toString())
                .put("email", email.text.toString()).put("password", pass.text.toString())) {
                showAuth()
                Toast.makeText(this, "Pendaftaran berhasil, silakan masuk", Toast.LENGTH_LONG).show()
            }
        })
        root.addView(button("KEMBALI") { showAuth() })
        root.addView(status)
    }

    private fun getDashboard() {
        request("GET", "me", null) { me ->
            baseLayout("CUZZPEDIA")
            val balance = me.optJSONObject("user")?.optString("balance", "0") ?: "0"
            root.addView(TextView(this).apply {
                text = "Halo, ${me.optJSONObject("user")?.optString("name") ?: "Member"}\nSaldo: Rp $balance"
                textSize = 18f; typeface = Typeface.DEFAULT_BOLD; setTextColor(dark)
                setPadding(0, 0, 0, 18)
            })
            root.addView(button("DEPOSIT SALDO") { showDeposit() })
            root.addView(button("BELI LAYANAN SMM") { showProducts("smm") })
            root.addView(button("PPOB & TOP UP GAME") { showProducts("ppob") })
            root.addView(button("RIWAYAT PESANAN") { showOrders() })
            root.addView(button("MUAT ULANG") { getDashboard() })
            root.addView(button("KELUAR") { token = ""; showAuth() })
            root.addView(status)
        }
    }

    private fun showDeposit() {
        baseLayout("Deposit saldo")
        val amount = field("Nominal deposit (minimal Rp5.000)")
        val note = field("Catatan / nama pengirim")
        root.addView(amount); root.addView(note)
        root.addView(TextView(this).apply {
            text = "Transfer ke rekening / QRIS yang ditampilkan admin. Setelah mengirim, unggah bukti melalui konfirmasi admin."
            textSize = 14f; setTextColor(dark); setPadding(0, 12, 0, 12)
        })
        root.addView(button("BUAT PERMINTAAN DEPOSIT") {
            val value = amount.text.toString().toLongOrNull()
            if (value == null || value < 5000) {
                Toast.makeText(this, "Minimal deposit Rp5.000", Toast.LENGTH_SHORT).show()
            } else request("POST", "deposit", JSONObject().put("amount", value).put("note", note.text.toString())) {
                Toast.makeText(this, "Permintaan deposit dibuat. Hubungi admin untuk verifikasi bukti.", Toast.LENGTH_LONG).show()
                getDashboard()
            }
        })
        root.addView(button("KEMBALI") { getDashboard() })
        root.addView(status)
    }

    private fun showProducts(type: String) {
        request("GET", "products?type=$type", null) { result ->
            baseLayout(if (type == "smm") "Layanan SMM" else "Produk PPOB")
            val arr = result.optJSONArray("products") ?: JSONArray()
            if (arr.length() == 0) root.addView(TextView(this).apply { text = "Belum ada produk. Admin perlu menambahkan produk di backend."; setTextColor(dark) })
            for (i in 0 until arr.length()) {
                val p = arr.getJSONObject(i)
                val id = p.optInt("id")
                val title = p.optString("name")
                val price = p.optString("price")
                root.addView(TextView(this).apply {
                    text = "$title\nRp $price"
                    textSize = 16f; setTextColor(dark); setPadding(12, 16, 12, 4)
                })
                root.addView(button("PESAN") { showOrderForm(id, title) })
            }
            root.addView(button("KEMBALI") { getDashboard() })
            root.addView(status)
        }
    }

    private fun showOrderForm(productId: Int, productName: String) {
        baseLayout("Pesan layanan")
        root.addView(TextView(this).apply { text = productName; textSize = 18f; setTextColor(dark) })
        val target = field("Link akun / nomor tujuan")
        val quantity = field("Jumlah / nominal")
        root.addView(target); root.addView(quantity)
        root.addView(button("KIRIM PESANAN") {
            val q = quantity.text.toString().toLongOrNull() ?: 1L
            request("POST", "orders", JSONObject().put("product_id", productId)
                .put("target", target.text.toString()).put("quantity", q)) {
                Toast.makeText(this, "Pesanan berhasil dikirim", Toast.LENGTH_LONG).show()
                showOrders()
            }
        })
        root.addView(button("BATAL") { getDashboard() })
        root.addView(status)
    }

    private fun showOrders() {
        request("GET", "orders", null) { result ->
            baseLayout("Riwayat pesanan")
            val arr = result.optJSONArray("orders") ?: JSONArray()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                root.addView(TextView(this).apply {
                    text = "#${o.optInt("id")} • ${o.optString("product_name")}\nStatus: ${o.optString("status")} • Rp ${o.optString("total")}"
                    textSize = 14f; setTextColor(dark); setPadding(0, 12, 0, 12)
                })
            }
            if (arr.length() == 0) root.addView(TextView(this).apply { text = "Belum ada pesanan"; setTextColor(dark) })
            root.addView(button("KEMBALI") { getDashboard() })
            root.addView(status)
        }
    }

    private fun request(method: String, endpoint: String, body: JSONObject?, success: (JSONObject) -> Unit) {
        status.text = "Memproses..."
        worker.execute {
            try {
                val url = URL("$apiBase/$endpoint")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = method
                    connectTimeout = 15000; readTimeout = 15000
                    setRequestProperty("Accept", "application/json")
                    if (token.isNotEmpty()) setRequestProperty("Authorization", "Bearer $token")
                    if (body != null) {
                        doOutput = true
                        setRequestProperty("Content-Type", "application/json; charset=utf-8")
                        OutputStreamWriter(outputStream, Charsets.UTF_8).use { it.write(body.toString()) }
                    }
                }
                val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
                val response = stream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                if (conn.responseCode !in 200..299 || !json.optBoolean("success", false)) {
                    throw Exception(json.optString("message", "Permintaan gagal"))
                }
                runOnUiThread { success(json) }
                conn.disconnect()
            } catch (e: Exception) {
                runOnUiThread {
                    status.text = "Gagal: ${e.message ?: "Periksa koneksi dan alamat API"}"
                    Toast.makeText(this, status.text, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
