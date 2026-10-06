package com.example.petshoptcc.activity

import android.os.Bundle
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.petshoptcc.R
import com.example.petshoptcc.data.UsuarioRepositorio
import com.example.petshoptcc.databinding.ActivityRecuperarSenhaBinding
import com.example.petshoptcc.util.configurarTela
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

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
        if (!binding.btnEnviar.isEnabled) return // já está enviando (ex.: Enter + toque no botão)
        val email = binding.edtEmail.text?.toString()?.trim().orEmpty()
        binding.layoutEmail.error = when {
            email.isEmpty() -> getString(R.string.erro_email_vazio)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> getString(R.string.erro_email_invalido)
            else -> null
        }
        if (binding.layoutEmail.error != null) return

        binding.btnEnviar.isEnabled = false
        binding.btnEnviar.setText(R.string.aguarde)
        lifecycleScope.launch {
            val erro = UsuarioRepositorio(this@RecuperarSenha).recuperarSenha(email)
            binding.btnEnviar.isEnabled = true
            binding.btnEnviar.setText(R.string.recuperar_botao)
            if (erro != null) {
                binding.layoutEmail.error = getString(erro)
                return@launch
            }
            MaterialAlertDialogBuilder(this@RecuperarSenha)
                .setTitle(R.string.recuperar_dialogo_titulo)
                .setMessage(getString(R.string.recuperar_dialogo_mensagem, email))
                .setPositiveButton(R.string.ok) { _, _ -> finish() }
                .setCancelable(false)
                .show()
        }
    }

    companion object {
        const val EXTRA_EMAIL = "email"
    }
}
