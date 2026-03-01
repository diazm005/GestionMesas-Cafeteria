package com.example.cafeteriaproyecto

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.cafeteriaproyecto.data.AppDatabaseHelper
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MesasActivity : AppCompatActivity() {


    lateinit var db: AppDatabaseHelper
    lateinit var layoutMesas: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mesas)

        db          = AppDatabaseHelper(this)
        layoutMesas  = findViewById(R.id.layoutMesas)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cargarMesas()
    }

    private fun cargarMesas() {
        layoutMesas.removeAllViews()
        val mesas = db.obtenerMesas()

        mesas.forEach { mesa ->
            val idMesa  = mesa["id_mesa"] as Int
            val numero  = mesa["numero"] as Int
            val estado  = mesa["estado"] as String
            val libre   = estado == "libre"

            val boton = Button(this).apply {
                text      = "$numero - ${if (libre) "Libre" else "Ocupada"}"
                textSize  = 16f
                setTextColor(0xFFFFFFFF.toInt())
                setBackgroundColor(if (libre) 0xFF026209.toInt() else 0xFF610909.toInt())

                val params = LinearLayout.LayoutParams(
                    dpToPx(180),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dpToPx(10)
                    gravity = android.view.Gravity.CENTER_HORIZONTAL
                }
                layoutParams = params
            }

            boton.setOnClickListener {
                if (libre) {
                    // Mesa libre = crear nuevo pedido
                    val fecha    = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val idPedido = db.crearPedido(idMesa = idMesa, idUsuario = 1, fecha = fecha)
                    db.actualizarMesa(idMesa = idMesa, estado = "ocupada", idPedido = idPedido)

                    val intent = Intent(this, PedidoActivity::class.java)
                    intent.putExtra("id_pedido", idPedido)
                    intent.putExtra("id_mesa", idMesa)
                    startActivity(intent)

                } else {
                    val pedido = db.obtenerPedidoActivoPorMesa(idMesa)
                    if (pedido != null) {
                        val idPedido = pedido["id_pedido"] as Int
                        val intent   = Intent(this, PedidoActivity::class.java)
                        intent.putExtra("id_pedido", idPedido)
                        intent.putExtra("id_mesa", idMesa)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "No se encontro pedido activo", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            layoutMesas.addView(boton)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarMesas()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}