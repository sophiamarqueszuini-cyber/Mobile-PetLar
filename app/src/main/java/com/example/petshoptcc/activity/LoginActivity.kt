package com.example.petshoptcc.activity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import com.example.petshoptcc.R
import com.example.petshoptcc.data.UsuarioRepositorio
import com.example.petshoptcc.databinding.ActivityLoginBinding
import com.example.petshoptcc.util.configurarTela

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var repositorio: UsuarioRepositorio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repositorio = UsuarioRepositorio(this)
        if (repositorio.usuarioLogado() != null) {
            abrirInicio()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarTela(binding.main, incluirTeclado = true)

        binding.btnEntrar.setOnClickListener { entrar() }
        binding.edtSenha.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                entrar()
                true
            } else {
                false
            }
        }
        binding.btnCadastrar.setOnClickListener {
            startActivity(Intent(this, Cadastro::class.java))
        }
        binding.btnEsqueciSenha.setOnClickListener {
            startActivity(
                Intent(this, RecuperarSenha::class.java)
                    .putExtra(RecuperarSenha.EXTRA_EMAIL, binding.edtEmail.text?.toString()?.trim())
            )
        }
    }

    private fun entrar() {
        val email = binding.edtEmail.text?.toString()?.trim().orEmpty()
        val senha = binding.edtSenha.text?.toString().orEmpty()

        binding.layoutEmail.error = when {
            email.isEmpty() -> getString(R.string.erro_email_vazio)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> getString(R.string.erro_email_invalido)
            else -> null
        }
        binding.layoutSenha.error = if (senha.isEmpty()) getString(R.string.erro_senha_vazia) else null

        if (binding.layoutEmail.error != null || binding.layoutSenha.error != null) return

        if (repositorio.autenticar(email, senha) == null) {
            binding.layoutSenha.error = getString(R.string.erro_login)
            return
        }
        abrirInicio()
    }

    private fun abrirInicio() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
