package com.example.petshoptcc.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.petshoptcc.R
import com.example.petshoptcc.data.PedidoRegistrado
import com.example.petshoptcc.data.PedidoRepositorio
import com.example.petshoptcc.data.TipoAcesso
import com.example.petshoptcc.data.UsuarioRepositorio
import com.example.petshoptcc.databinding.ActivityPainelAdminBinding
import com.example.petshoptcc.databinding.ItemContatoBinding
import com.example.petshoptcc.databinding.ItemIndicadorBinding
import com.example.petshoptcc.databinding.ItemPedidoAdminBinding
import com.example.petshoptcc.ui.adicionarNaGrade
import com.example.petshoptcc.util.configurarTela
import com.example.petshoptcc.util.dataBr
import com.example.petshoptcc.util.emReais
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/** Área da equipe: indicadores, pedidos (com troca de status) e clientes cadastrados. */
class PainelAdmin : AppCompatActivity() {

    private lateinit var binding: ActivityPainelAdminBinding
    private lateinit var usuarios: UsuarioRepositorio
    private lateinit var pedidos: PedidoRepositorio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usuarios = UsuarioRepositorio(this)
        val admin = usuarios.usuarioLogado()
        if (admin == null || usuarios.tipoLogado() != TipoAcesso.ADMINISTRATIVO) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityPainelAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarTela(binding.main)
        pedidos = PedidoRepositorio(this)

        binding.txtSaudacao.text = getString(R.string.admin_saudacao, admin.nome)
        binding.btnSair.setOnClickListener {
            usuarios.sair()
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized) atualizar()
    }

    private fun atualizar() {
        val todosPedidos = pedidos.todos()
        val clientes = usuarios.clientes()
        montarIndicadores(todosPedidos, clientes.size)
        montarPedidos(todosPedidos)
        montarClientes()
    }

    private fun montarIndicadores(lista: List<PedidoRegistrado>, totalClientes: Int) {
        // Pedidos cancelados não entram no faturamento
        val validos = lista.filter { it.pedido.status != STATUS_CANCELADO }
        val faturamento = validos.sumOf { it.pedido.total }
        val ticket = if (validos.isEmpty()) 0.0 else faturamento / validos.size
        val indicadores = listOf(
            lista.size.toString() to R.string.admin_ind_pedidos,
            faturamento.emReais() to R.string.admin_ind_faturamento,
            totalClientes.toString() to R.string.admin_ind_clientes,
            ticket.emReais() to R.string.admin_ind_ticket
        )
        binding.gridIndicadores.removeAllViews()
        indicadores.forEach { (valor, rotulo) ->
            val cartao = ItemIndicadorBinding.inflate(layoutInflater, binding.gridIndicadores, false)
            cartao.txtValor.text = valor
            cartao.txtRotulo.setText(rotulo)
            binding.gridIndicadores.adicionarNaGrade(cartao.root)
        }
    }

    private fun montarPedidos(lista: List<PedidoRegistrado>) {
        binding.listaPedidos.removeAllViews()
        binding.txtSemPedidos.isVisible = lista.isEmpty()
        lista.forEach { registro ->
            val p = registro.pedido
            val cartao = ItemPedidoAdminBinding.inflate(layoutInflater, binding.listaPedidos, true)
            cartao.txtNumero.text = getString(R.string.admin_pedido_numero, p.idPedido)
            cartao.txtStatus.text = p.status
            cartao.txtCliente.text = getString(R.string.admin_pedido_cliente, registro.nomeCliente, registro.emailCliente)
            cartao.txtDetalhes.text = resources.getQuantityString(
                R.plurals.admin_pedido_detalhes, registro.quantidadeItens,
                p.dataPedido.dataBr(), registro.quantidadeItens, registro.formaPagamento
            )
            cartao.txtTotal.text = p.total.emReais()
            cartao.btnStatus.setOnClickListener { escolherStatus(registro) }
        }
    }

    private fun escolherStatus(registro: PedidoRegistrado) {
        val opcoes = PedidoRepositorio.STATUS
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.admin_status_titulo, registro.pedido.idPedido))
            .setSingleChoiceItems(opcoes.toTypedArray(), opcoes.indexOf(registro.pedido.status)) { dialogo, i ->
                pedidos.alterarStatus(registro.pedido.idPedido, opcoes[i])
                dialogo.dismiss()
                atualizar()
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    private fun montarClientes() {
        val clientes = usuarios.clientes()
        binding.listaClientes.removeAllViews()
        binding.cartaoClientes.isVisible = clientes.isNotEmpty()
        binding.txtSemClientes.isVisible = clientes.isEmpty()
        clientes.forEach { cliente ->
            val linha = ItemContatoBinding.inflate(layoutInflater, binding.listaClientes, true)
            linha.txtCanal.text = cliente.nome
            linha.txtValor.text = getString(R.string.admin_cliente_desde, cliente.email, cliente.dataCadastro.dataBr(comHora = false))
            val negrito = linha.txtValor.typeface
            linha.txtValor.typeface = linha.txtCanal.typeface
            linha.txtCanal.typeface = negrito
            linha.txtCanal.isAllCaps = false
            linha.txtSeta.isVisible = false
        }
    }

    private companion object {
        val STATUS_CANCELADO = PedidoRepositorio.STATUS.last()
    }
}
