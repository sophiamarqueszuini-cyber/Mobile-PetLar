package com.example.petshoptcc.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.petshoptcc.R
import com.example.petshoptcc.data.PedidoRegistrado
import com.example.petshoptcc.data.PedidoRepositorio
import com.example.petshoptcc.data.TipoAcesso
import com.example.petshoptcc.data.UsuarioRepositorio
import com.example.petshoptcc.databinding.ActivityPainelAdminBinding
import com.example.petshoptcc.databinding.ItemClienteAdminBinding
import com.example.petshoptcc.databinding.ItemIndicadorBinding
import com.example.petshoptcc.databinding.ItemPedidoAdminBinding
import com.example.petshoptcc.databinding.ItemProdutoPedidoBinding
import com.example.petshoptcc.model.Usuario
import com.example.petshoptcc.ui.adicionarNaGrade
import com.example.petshoptcc.util.configurarTela
import com.example.petshoptcc.util.dataBr
import com.example.petshoptcc.util.emReais
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

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
        pedidos = PedidoRepositorio()

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

    /** Lê pedidos e clientes do Firestore (os mesmos do painel do site). */
    private fun atualizar() {
        lifecycleScope.launch {
            try {
                // coroutineScope: se uma das buscas falhar (sem permissão, sem internet), o erro cai no catch
                // em vez de derrubar o app
                val (todosPedidos, clientes) = coroutineScope {
                    val p = async { pedidos.todos() }
                    val c = async { usuarios.clientes() }
                    p.await() to c.await()
                }
                montarIndicadores(todosPedidos, clientes.size)
                montarPedidos(todosPedidos)
                montarClientes(clientes)
            } catch (e: Exception) {
                Toast.makeText(this@PainelAdmin, R.string.admin_erro_carregar, Toast.LENGTH_LONG).show()
            }
        }
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
            val cartao = ItemPedidoAdminBinding.inflate(layoutInflater, binding.listaPedidos, false)
            cartao.txtNumero.text = getString(R.string.admin_pedido_numero, p.idPedido)
            cartao.txtStatus.text = p.status
            pintarStatus(cartao, p.status)
            cartao.txtCliente.text = getString(R.string.admin_pedido_cliente, registro.nomeCliente, registro.emailCliente)
            cartao.txtDetalhes.text = resources.getQuantityString(
                R.plurals.admin_pedido_detalhes, registro.quantidadeItens,
                p.dataPedido.dataBr(), registro.quantidadeItens, registro.formaPagamento
            )
            cartao.listaItens.isVisible = registro.itens.isNotEmpty()
            registro.itens.forEach { item ->
                val linha = ItemProdutoPedidoBinding.inflate(layoutInflater, cartao.listaItens, true)
                linha.txtProduto.text = getString(R.string.admin_pedido_item, item.quantidade, item.nome)
                linha.txtSubtotal.text = item.subtotal.emReais()
            }
            cartao.txtTotal.text = p.total.emReais()
            cartao.btnStatus.setOnClickListener { escolherStatus(registro) }
            binding.listaPedidos.adicionarNaGrade(cartao.root)
        }
    }

    /** Selo bege; vermelho para cancelado e verde para entregue, como no site. */
    private fun pintarStatus(cartao: ItemPedidoAdminBinding, status: String) {
        val (fundo, texto) = when (status) {
            STATUS_CANCELADO -> R.color.status_cancelado_fundo to R.color.status_cancelado_texto
            STATUS_ENTREGUE -> R.color.status_entregue_fundo to R.color.status_entregue_texto
            else -> R.color.status_fundo to R.color.status_texto
        }
        cartao.txtStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, fundo))
        cartao.txtStatus.setTextColor(ContextCompat.getColor(this, texto))
    }

    private fun escolherStatus(registro: PedidoRegistrado) {
        val opcoes = PedidoRepositorio.STATUS
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.admin_status_titulo, registro.pedido.idPedido))
            .setSingleChoiceItems(opcoes.toTypedArray(), opcoes.indexOf(registro.pedido.status)) { dialogo, i ->
                dialogo.dismiss()
                lifecycleScope.launch {
                    try {
                        pedidos.alterarStatus(registro.ref, opcoes[i])
                        atualizar()
                    } catch (e: Exception) {
                        Toast.makeText(this@PainelAdmin, R.string.admin_erro_status, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    private fun montarClientes(clientes: List<Usuario>) {
        binding.listaClientes.removeAllViews()
        binding.txtSemClientes.isVisible = clientes.isEmpty()
        clientes.forEach { cliente ->
            val linha = ItemClienteAdminBinding.inflate(layoutInflater, binding.listaClientes, true)
            linha.txtNome.text = cliente.nome
            linha.txtDetalhes.text = getString(R.string.admin_cliente_desde, cliente.email, cliente.dataCadastro.dataBr(comHora = false))
        }
    }

    private companion object {
        val STATUS_CANCELADO = PedidoRepositorio.STATUS.last()
        const val STATUS_ENTREGUE = "Entregue"
    }
}
