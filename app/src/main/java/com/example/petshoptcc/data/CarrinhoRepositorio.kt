package com.example.petshoptcc.data

import android.content.Context
import androidx.core.content.edit
import org.json.JSONObject

class ItemCarrinho(val produto: ProdutoBoutique, val quantidade: Int) {
    val subtotal: Double get() = produto.produto.preco * quantidade
}

/**
 * Carrinho salvo no aparelho, com a mesma lógica do carrinho do site
 * (localStorage "petlarCarrinho"): id do produto -> quantidade.
 */
class CarrinhoRepositorio(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("carrinho", Context.MODE_PRIVATE)

    fun itens(): List<ItemCarrinho> {
        val json = lerJson()
        return json.keys().asSequence().mapNotNull { id ->
            Catalogo.produto(id.toLong())?.let { ItemCarrinho(it, json.getInt(id)) }
        }.sortedBy { it.produto.produto.idProduto }.toList()
    }

    fun totalItens(): Int {
        val json = lerJson()
        return json.keys().asSequence().sumOf { json.getInt(it) }
    }

    fun adicionar(idProduto: Long) {
        val json = lerJson()
        val chave = idProduto.toString()
        json.put(chave, json.optInt(chave, 0) + 1)
        salvar(json)
    }

    fun alterarQuantidade(idProduto: Long, quantidade: Int) {
        val json = lerJson()
        json.put(idProduto.toString(), quantidade.coerceAtLeast(1))
        salvar(json)
    }

    fun remover(idProduto: Long) {
        val json = lerJson()
        json.remove(idProduto.toString())
        salvar(json)
    }

    fun limpar() {
        prefs.edit { remove(CHAVE) }
    }

    private fun lerJson() = JSONObject(prefs.getString(CHAVE, null) ?: "{}")

    private fun salvar(json: JSONObject) {
        prefs.edit { putString(CHAVE, json.toString()) }
    }

    private companion object {
        const val CHAVE = "petlarCarrinho"
    }
}
