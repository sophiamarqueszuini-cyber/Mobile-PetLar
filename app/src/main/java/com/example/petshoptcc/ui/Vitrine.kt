package com.example.petshoptcc.ui

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.petshoptcc.R
import com.example.petshoptcc.data.CarrinhoRepositorio
import com.example.petshoptcc.data.ProdutoBoutique
import com.example.petshoptcc.databinding.IncludeBotaoCarrinhoBinding
import com.example.petshoptcc.databinding.ItemCartaoGaleriaBinding
import com.example.petshoptcc.databinding.ItemProdutoBoutiqueBinding
import com.example.petshoptcc.util.emReais

/** Cartão .gallery-card com foto, título, descrição e preço opcional. */
fun LayoutInflater.criarCartaoGaleria(
    pai: ViewGroup,
    imagem: Int,
    titulo: String,
    descricao: String?,
    preco: String? = null
): View {
    val cartao = ItemCartaoGaleriaBinding.inflate(this, pai, false)
    cartao.imgFoto.setImageResource(imagem)
    cartao.txtTitulo.text = titulo
    cartao.txtDescricao.text = descricao
    cartao.txtPreco.isVisible = preco != null
    cartao.txtPreco.text = preco
    return cartao.root
}

/**
 * Produto da boutique. O botão repete o efeito do site:
 * vira "ADICIONADO!" em marrom por 1,5 s.
 */
fun LayoutInflater.criarCartaoProduto(
    pai: ViewGroup,
    item: ProdutoBoutique,
    carrinho: CarrinhoRepositorio,
    aoAdicionar: () -> Unit
): View {
    val cartao = ItemProdutoBoutiqueBinding.inflate(this, pai, false)
    cartao.imgProduto.setImageResource(item.imagem)
    cartao.imgProduto.contentDescription = item.produto.nome
    cartao.txtCategoria.text = item.categoria.nome
    cartao.txtPreco.text = item.produto.preco.emReais()
    cartao.txtNome.text = item.produto.nome

    val botao = cartao.btnAdicionar
    val corOriginal = botao.backgroundTintList
    val textoOriginal = botao.text
    val corOriginalTexto = botao.textColors
    botao.setOnClickListener {
        carrinho.adicionar(item.produto.idProduto)
        aoAdicionar()
        botao.text = context.getString(R.string.boutique_adicionado)
        botao.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.petlar_marrom))
        botao.setTextColor(ContextCompat.getColor(context, R.color.white))
        botao.postDelayed({
            botao.text = textoOriginal
            botao.backgroundTintList = corOriginal
            botao.setTextColor(corOriginalTexto)
        }, 1500)
    }
    return cartao.root
}

/**
 * Coloca um cartão na grade ocupando a largura da coluna. O número de colunas
 * vem de res/values(-land)/integers.xml, então a grade se adapta à orientação.
 * Cartões da mesma linha ficam com a mesma altura.
 */
fun GridLayout.adicionarNaGrade(cartao: View, espacoAbaixo: Int = 0) {
    val espaco = resources.getDimensionPixelSize(R.dimen.espaco_grade)
    cartao.layoutParams = GridLayout.LayoutParams(
        GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL),
        GridLayout.spec(GridLayout.UNDEFINED, 1f)
    ).apply {
        width = 0
        setMargins(espaco, espaco, espaco, espaco + espacoAbaixo)
    }
    addView(cartao)
}

/** Atualiza o selo com a quantidade de itens do carrinho. */
fun IncludeBotaoCarrinhoBinding.atualizar(carrinho: CarrinhoRepositorio) {
    val total = carrinho.totalItens()
    txtQuantidade.isVisible = total > 0
    txtQuantidade.text = if (total > 99) "99+" else total.toString()
}
