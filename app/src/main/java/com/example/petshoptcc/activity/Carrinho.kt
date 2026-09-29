package com.example.petshoptcc.activity

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.example.petshoptcc.R
import com.example.petshoptcc.data.CarrinhoRepositorio
import com.example.petshoptcc.data.PedidoRepositorio
import com.example.petshoptcc.data.UsuarioRepositorio
import com.example.petshoptcc.databinding.ActivityCarrinhoBinding
import com.example.petshoptcc.databinding.ItemCarrinhoBinding
import com.example.petshoptcc.databinding.ItemLinhaValorBinding
import com.example.petshoptcc.databinding.ItemOpcaoBinding
import com.example.petshoptcc.util.configurarTela
import com.example.petshoptcc.util.emReais
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.random.Random

/** Carrinho com as mesmas regras do carrinho.html do site. */
class Carrinho : AppCompatActivity() {

    private class Opcao(val titulo: String, val detalhe: String, val valor: Double = 0.0)

    private val opcoesFrete = listOf(
        Opcao("Retirar na Loja PetLar (Pinheiros)", "Pronto em 45 min", 0.0),
        Opcao("Entrega Padrão PetLar", "2 a 3 dias úteis", 15.90),
        Opcao("Entrega Expressa PetLar Já", "Hoje mesmo", 24.90)
    )

    private val formasPagamento = listOf(
        Opcao("⚡ PIX", "5% de desconto automático"),
        Opcao("💳 Cartão de Crédito", "Até 6x sem juros"),
        Opcao("📄 Boleto Bancário", "Vencimento em 1 dia útil"),
        Opcao("📱 Carteiras Digitais", "Apple Pay / Google Pay")
    )

    private lateinit var binding: ActivityCarrinhoBinding
    private lateinit var carrinho: CarrinhoRepositorio

    private var frete = 0.0
    private var cupomAplicado = false
    private var pagamento = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarrinhoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarTela(binding.main, incluirTeclado = true)
        carrinho = CarrinhoRepositorio(this)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnExplorar.setOnClickListener { abrirBoutique() }
        binding.btnContinuar.setOnClickListener { abrirBoutique() }
        binding.btnEsvaziar.setOnClickListener { confirmarEsvaziar() }
        binding.btnAssinar.setOnClickListener {
            Toast.makeText(this, R.string.carrinho_assinatura_ok, Toast.LENGTH_SHORT).show()
        }
        binding.btnConsultar.setOnClickListener { consultarFrete() }
        binding.btnAplicarCupom.setOnClickListener { aplicarCupom() }
        binding.btnFinalizar.setOnClickListener { finalizarPedido() }
        binding.btnVoltarInicio.setOnClickListener { finish() }
        binding.edtCep.aoConfirmar { consultarFrete() }
        binding.edtCupom.aoConfirmar { aplicarCupom() }
        listOf(binding.btnContinuar, binding.btnEsvaziar).forEach {
            it.paintFlags = it.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }

        binding.linhaSubtotal.txtRotulo.setText(R.string.carrinho_subtotal)
        binding.linhaFrete.txtRotulo.setText(R.string.carrinho_frete)
        binding.linhaCupom.txtRotulo.setText(R.string.carrinho_desconto_cupom)
        binding.linhaPix.txtRotulo.setText(R.string.carrinho_desconto_pix)
        listOf(binding.linhaCupom, binding.linhaPix).forEach { it.pintar(R.color.petlar_sucesso, negrito = false) }

        montarOpcoes(binding.listaPagamento, formasPagamento, 0) { i ->
            pagamento = i
            atualizarResumo()
        }
        carregarCarrinho()
    }

    override fun onResume() {
        super.onResume()
        if (!binding.cartaoSucesso.isVisible) carregarCarrinho()
    }

    private fun carregarCarrinho() {
        val itens = carrinho.itens()
        binding.cartaoVazio.isVisible = itens.isEmpty()
        binding.conteudo.isVisible = itens.isNotEmpty()
        binding.listaItens.removeAllViews()

        itens.forEach { item ->
            val linha = ItemCarrinhoBinding.inflate(layoutInflater, binding.listaItens, true)
            val id = item.produto.produto.idProduto
            linha.imgProduto.setImageResource(item.produto.imagem)
            linha.txtNome.text = item.produto.produto.nome
            linha.txtPrecoUnitario.text = getString(R.string.carrinho_cada, item.produto.produto.preco.emReais())
            linha.txtQuantidade.text = item.quantidade.toString()
            linha.txtSubtotal.text = item.subtotal.emReais()
            linha.btnMenos.isEnabled = item.quantidade > 1
            linha.btnMenos.alpha = if (item.quantidade > 1) 1f else 0.35f
            linha.btnMenos.setOnClickListener { carrinho.alterarQuantidade(id, item.quantidade - 1); carregarCarrinho() }
            linha.btnMais.setOnClickListener { carrinho.alterarQuantidade(id, item.quantidade + 1); carregarCarrinho() }
            linha.btnRemover.setOnClickListener { carrinho.remover(id); carregarCarrinho() }
        }
        atualizarResumo()
    }

    private class Resumo(val subtotal: Double, val descontoCupom: Double, val descontoPix: Double, val frete: Double) {
        val total: Double get() = (subtotal - descontoCupom - descontoPix + frete).coerceAtLeast(0.0)
    }

    private fun calcularResumo(): Resumo {
        val subtotal = carrinho.itens().sumOf { it.subtotal }
        val descontoCupom = if (cupomAplicado) subtotal * 0.10 else 0.0
        val descontoPix = if (pagamento == 0) (subtotal - descontoCupom) * 0.05 else 0.0
        return Resumo(subtotal, descontoCupom, descontoPix, frete)
    }

    private fun atualizarResumo() {
        val resumo = calcularResumo()
        val subtotal = resumo.subtotal
        val descontoCupom = resumo.descontoCupom
        val descontoPix = resumo.descontoPix
        val total = resumo.total

        binding.linhaSubtotal.txtValor.text = subtotal.emReais()
        if (frete == 0.0) {
            binding.linhaFrete.txtValor.setText(R.string.carrinho_gratis)
            binding.linhaFrete.pintar(R.color.petlar_sucesso, negrito = true)
        } else {
            binding.linhaFrete.txtValor.text = frete.emReais()
            binding.linhaFrete.pintar(R.color.petlar_texto_secundario, negrito = false)
        }
        binding.linhaCupom.root.isVisible = descontoCupom > 0
        binding.linhaCupom.txtValor.text = getString(R.string.menos_valor, descontoCupom.emReais())
        binding.linhaPix.root.isVisible = descontoPix > 0
        binding.linhaPix.txtValor.text = getString(R.string.menos_valor, descontoPix.emReais())
        binding.txtTotal.text = total.emReais()
        binding.txtParcelas.text = getString(R.string.carrinho_parcelas, (total / 6).emReais())
    }

    private fun consultarFrete() {
        val cep = binding.edtCep.text?.toString().orEmpty().filter(Char::isDigit)
        if (cep.length != 8) {
            Toast.makeText(this, R.string.carrinho_cep_invalido, Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.listaFrete.childCount == 0) {
            montarOpcoes(binding.listaFrete, opcoesFrete, 0) { i ->
                frete = opcoesFrete[i].valor
                atualizarResumo()
            }
        }
        binding.listaFrete.isVisible = true
    }

    private fun aplicarCupom() {
        val cupom = binding.edtCupom.text?.toString()?.trim()?.uppercase().orEmpty()
        cupomAplicado = cupom == CUPOM
        val mensagem = if (cupomAplicado) R.string.carrinho_cupom_ok else R.string.carrinho_cupom_invalido
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
        atualizarResumo()
    }

    private fun finalizarPedido() {
        val resumo = calcularResumo()
        val numero = Random.nextInt(100000, 1000000)
        val usuario = UsuarioRepositorio(this).usuarioLogado()
        // TODO: enviar o pedido ao backend quando a API estiver disponível
        PedidoRepositorio(this).registrar(
            numero = numero.toLong(),
            idCliente = usuario?.idUsuario ?: 0,
            nomeCliente = usuario?.nome.orEmpty(),
            emailCliente = usuario?.email.orEmpty(),
            subtotal = resumo.subtotal,
            desconto = resumo.descontoCupom + resumo.descontoPix,
            frete = resumo.frete,
            total = resumo.total,
            formaPagamento = formasPagamento[pagamento].titulo,
            quantidadeItens = carrinho.totalItens()
        )
        carrinho.limpar()
        binding.txtPedidoNumero.text = getString(R.string.pedido_sucesso_texto, numero)
        binding.txtPedidoPagamento.text = getString(R.string.pedido_forma_pagamento, formasPagamento[pagamento].titulo)
        binding.txtPedidoTotal.text = getString(R.string.pedido_total, resumo.total.emReais())
        binding.conteudo.isVisible = false
        binding.cartaoVazio.isVisible = false
        binding.cartaoSucesso.isVisible = true
        binding.rolagem.scrollTo(0, 0)
    }

    private fun confirmarEsvaziar() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.carrinho_esvaziar_pergunta)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.carrinho_esvaziar_sim) { _, _ ->
                carrinho.limpar()
                carregarCarrinho()
            }
            .show()
    }

    private fun abrirBoutique() {
        startActivity(Intent(this, Produtos::class.java))
        finish()
    }

    /** Lista de opções com seleção única, destacando a escolhida em dourado como no site. */
    private fun montarOpcoes(lista: LinearLayout, opcoes: List<Opcao>, inicial: Int, aoSelecionar: (Int) -> Unit) {
        val cartoes = opcoes.map { opcao ->
            ItemOpcaoBinding.inflate(layoutInflater, lista, true).apply {
                txtTitulo.text = opcao.titulo
                txtDetalhe.text = opcao.detalhe
                txtValor.isVisible = lista == binding.listaFrete
                if (opcao.valor == 0.0) {
                    txtValor.setText(R.string.carrinho_gratis)
                    txtValor.setTextColor(ContextCompat.getColor(this@Carrinho, R.color.petlar_sucesso))
                } else {
                    txtValor.text = opcao.valor.emReais()
                }
            }
        }
        fun selecionar(indice: Int) {
            cartoes.forEachIndexed { i, cartao ->
                val marcado = i == indice
                cartao.radio.isChecked = marcado
                cartao.root.strokeColor = ContextCompat.getColor(
                    this, if (marcado) R.color.petlar_dourado else R.color.petlar_borda
                )
                cartao.root.setCardBackgroundColor(
                    ContextCompat.getColor(this, if (marcado) R.color.petlar_bege_selecao else R.color.white)
                )
            }
        }
        cartoes.forEachIndexed { i, cartao ->
            cartao.root.setOnClickListener {
                selecionar(i)
                aoSelecionar(i)
            }
        }
        selecionar(inicial)
    }

    private fun ItemLinhaValorBinding.pintar(cor: Int, negrito: Boolean) {
        val c = ContextCompat.getColor(this@Carrinho, cor)
        listOf(txtRotulo, txtValor).forEach {
            it.setTextColor(c)
            it.typeface = ResourcesCompat.getFont(
                this@Carrinho, if (negrito) R.font.zilla_slab_semibold else R.font.zilla_slab_regular
            )
        }
    }

    private fun TextView.aoConfirmar(acao: () -> Unit) {
        setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                acao()
                true
            } else {
                false
            }
        }
    }

    private companion object {
        const val CUPOM = "PETLAR10"
    }
}
