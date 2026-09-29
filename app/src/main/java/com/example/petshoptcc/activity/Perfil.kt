package com.example.petshoptcc.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.petshoptcc.R
import com.example.petshoptcc.data.UsuarioRepositorio
import com.example.petshoptcc.databinding.ActivityPerfilBinding
import com.example.petshoptcc.databinding.ItemContatoBinding
import com.example.petshoptcc.util.configurarTela

class Perfil : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private lateinit var repositorio: UsuarioRepositorio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarTela(binding.main)
        repositorio = UsuarioRepositorio(this)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.linhaEditar.preencher(getString(R.string.perfil_editar), null) {
            startActivity(Intent(this, EditarPerfil::class.java))
        }
        binding.linhaEnderecos.preencher(getString(R.string.perfil_enderecos), null) {
            startActivity(Intent(this, Enderecos::class.java))
        }
        binding.btnSair.setOnClickListener {
            repositorio.sair()
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        val usuario = repositorio.usuarioLogado() ?: return finish()
        val naoInformado = getString(R.string.nao_informado)
        binding.txtIniciais.text = usuario.nome.split(' ')
            .filter { it.isNotBlank() }
            .let { listOfNotNull(it.firstOrNull(), it.drop(1).lastOrNull()) }
            .joinToString("") { it.first().uppercase() }
        binding.txtNome.text = usuario.nome
        binding.txtEmail.text = usuario.email
        binding.linhaTelefone.preencher(getString(R.string.campo_telefone), usuario.telefone ?: naoInformado)
        binding.linhaCpf.preencher(getString(R.string.campo_cpf), usuario.cpf ?: naoInformado)
    }

    /** Reaproveita a linha "CANAL / valor →" do site; sem ação, a seta some. */
    private fun ItemContatoBinding.preencher(rotulo: String, valor: String?, acao: (() -> Unit)? = null) {
        txtCanal.text = if (valor == null) rotulo else getString(R.string.contato_canal, rotulo)
        txtValor.text = valor
        txtValor.visibility = if (valor == null) View.GONE else View.VISIBLE
        txtSeta.visibility = if (acao == null) View.GONE else View.VISIBLE
        root.isClickable = acao != null
        if (acao != null) root.setOnClickListener { acao() }
    }
}
