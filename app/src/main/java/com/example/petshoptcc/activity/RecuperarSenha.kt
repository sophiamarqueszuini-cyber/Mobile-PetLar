package com.example.petshoptcc.activity

import android.os.Bundle
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import com.example.petshoptcc.R
import com.example.petshoptcc.databinding.ActivityRecuperarSenhaBinding
import com.example.petshoptcc.util.configurarTela
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RecuperarSenha : AppCompatActivity() {

    private lateinit var binding: ActivityRecuperarSenhaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecuperarSenhaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarTela(binding.main, incluirTeclado = true)

        binding.edtEmail.setText(intent.getStringExtra(EXTRA_EMAIL))
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnEnviar.setOnClickListener { enviar() }
        binding.edtEmail.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                enviar()
                true
            } else {
                false
            }
        }
    }

    private fun enviar() {
        val email = binding.edtEmail.text?.toString()?.trim().orEmpty()
        binding.layoutEmail.error = when {
            email.isEmpty() -> getString(R.string.erro_email_vazio)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> getString(R.string.erro_email_invalido)
            else -> null
        }
        if (binding.layoutEmail.error != null) return

        // TODO: pedir ao backend o envio do e-mail de redefinição quando a API estiver disponível
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.recuperar_dialogo_titulo)
            .setMessage(getString(R.string.recuperar_dialogo_mensagem, email))
            .setPositiveButton(R.string.ok) { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    companion object {
        const val EXTRA_EMAIL = "email"
    }
}
