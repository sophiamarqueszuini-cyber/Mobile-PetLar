package com.example.petshoptcc.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.NumberFormat
import java.util.Locale

private val formatoReais: NumberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))

/** 189.0 -> "R$ 189,00" */
fun Double.emReais(): String = formatoReais.format(this)

/** "2026-09-28 14:05:00" (como é salvo) -> "28/09/2026 14:05"; [comHora] = false -> "28/09/2026". */
fun String.dataBr(comHora: Boolean = true): String {
    val data = substringBefore(' ').split('-')
    if (data.size != 3) return this
    val dia = "${data[2]}/${data[1]}/${data[0]}"
    return if (comHora) "$dia ${substringAfter(' ').take(5)}".trim() else dia
}

/**
 * Desenha a tela atrás das barras do sistema e aplica o espaçamento
 * necessário em [raiz]. [telaEscura] deixa os ícones da barra de status claros.
 */
fun AppCompatActivity.configurarTela(raiz: View, telaEscura: Boolean = false, incluirTeclado: Boolean = false) {
    val estilo = if (telaEscura) {
        SystemBarStyle.dark(Color.TRANSPARENT)
    } else {
        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    }
    enableEdgeToEdge(statusBarStyle = estilo, navigationBarStyle = estilo)
    val tipos = WindowInsetsCompat.Type.systemBars() or
        (if (incluirTeclado) WindowInsetsCompat.Type.ime() else 0)
    ViewCompat.setOnApplyWindowInsetsListener(raiz) { v, insets ->
        val bars = insets.getInsets(tipos)
        v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
        insets
    }
}

/** Abre links externos (WhatsApp, e-mail, Instagram...). */
fun Context.abrirLink(link: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, link.toUri()))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, link, Toast.LENGTH_LONG).show()
    }
}
