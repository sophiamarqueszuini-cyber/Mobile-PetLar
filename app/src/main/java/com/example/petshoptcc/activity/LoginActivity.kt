package com.example.petshoptcc.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import com.example.petshoptcc.R
import com.example.petshoptcc.data.DadosExemplo
import com.example.petshoptcc.data.ResultadoLogin
import com.example.petshoptcc.data.TipoAcesso
import com.example.petshoptcc.data.UsuarioRepositorio
import com.example.petshoptcc.databinding.ActivityLoginBinding
import com.example.petshoptcc.util.configurarTela

/** Login único: o perfil da conta decide se abre a loja (cliente) ou o painel da equipe (administrativo). */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var repositorio: UsuarioRepositorio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repositorio = UsuarioRepositorio(this)
        DadosExemplo.carregarSeNecessario(this)
        repositorio.tipoLogado()?.let {
            abrirArea(it)
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

        when (val resultado = repositorio.entrar(email, senha)) {
            is ResultadoLogin.Sucesso -> abrirArea(resultado.tipo)
            ResultadoLogin.Invalido -> binding.layoutSenha.error = getString(R.string.erro_login)
        }
    }

    private fun abrirArea(tipo: TipoAcesso) {
        startActivity(intentDaArea(this, tipo))
        finish()
    }

    companion object {
        /** Tela inicial de cada acesso. */
        fun intentDaArea(context: Context, tipo: TipoAcesso): Intent = Intent(
            context,
            if (tipo == TipoAcesso.ADMINISTRATIVO) PainelAdmin::class.java else MainActivity::class.java
        )
    }
}
