package com.example.petshoptcc.activity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.petshoptcc.R
import com.example.petshoptcc.data.TipoAcesso
import com.example.petshoptcc.data.UsuarioRepositorio
import com.example.petshoptcc.databinding.ActivityCadastroBinding
import com.example.petshoptcc.util.configurarTela

class Cadastro : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding
    private lateinit var repositorio: UsuarioRepositorio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarTela(binding.main, incluirTeclado = true)
        repositorio = UsuarioRepositorio(this)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnEntrar.setOnClickListener { finish() }
        binding.btnCadastrar.setOnClickListener { cadastrar() }
        binding.edtConfirmarSenha.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                cadastrar()
                true
            } else {
                false
            }
        }
    }

    private fun cadastrar() {
        val nome = binding.edtNome.text?.toString()?.trim().orEmpty()
        val email = binding.edtEmail.text?.toString()?.trim().orEmpty()
        val telefone = binding.edtTelefone.text?.toString().orEmpty().filter(Char::isDigit)
        val cpf = binding.edtCpf.text?.toString().orEmpty().filter(Char::isDigit)
        val senha = binding.edtSenha.text?.toString().orEmpty()
        val confirmarSenha = binding.edtConfirmarSenha.text?.toString().orEmpty()

        binding.layoutNome.error = if (nome.length < 3) getString(R.string.erro_nome) else null
        binding.layoutEmail.error = when {
            email.isEmpty() -> getString(R.string.erro_email_vazio)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> getString(R.string.erro_email_invalido)
            repositorio.emailCadastrado(email) -> getString(R.string.erro_email_em_uso)
            else -> null
        }
        binding.layoutTelefone.error =
            if (telefone.isNotEmpty() && telefone.length !in 10..11) getString(R.string.erro_telefone) else null
        binding.layoutCpf.error =
            if (cpf.isNotEmpty() && cpf.length != 11) getString(R.string.erro_cpf) else null
        binding.layoutSenha.error = when {
            senha.isEmpty() -> getString(R.string.erro_senha_vazia)
            senha.length < 6 -> getString(R.string.erro_senha_curta)
            else -> null
        }
        binding.layoutConfirmarSenha.error =
            if (confirmarSenha != senha) getString(R.string.erro_senhas_diferentes) else null

        val campos = listOf(
            binding.layoutNome, binding.layoutEmail, binding.layoutTelefone,
            binding.layoutCpf, binding.layoutSenha, binding.layoutConfirmarSenha
        )
        if (campos.any { it.error != null }) return

        repositorio.cadastrar(
            nome = nome,
            email = email,
            telefone = telefone.ifEmpty { null },
            cpf = cpf.ifEmpty { null },
            senha = senha
        )
        repositorio.entrar(email, senha, TipoAcesso.CLIENTE)
        Toast.makeText(this, R.string.cadastro_sucesso, Toast.LENGTH_SHORT).show()
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }
}
