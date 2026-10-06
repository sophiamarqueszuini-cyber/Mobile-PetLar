package com.example.petshoptcc.data

import android.content.Context
import androidx.core.content.edit
import com.example.petshoptcc.model.Pedido
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Produto comprado, com nome e preço do momento da compra. */
class ItemPedido(val nome: String, val quantidade: Int, val precoUnitario: Double) {
    val subtotal: Double get() = precoUnitario * quantidade
}

/** Pedido com os dados que o painel administrativo mostra junto (cliente, pagamento, itens). */
class PedidoRegistrado(
    val pedido: Pedido,
    val nomeCliente: String,
    val emailCliente: String,
    val formaPagamento: String,
    val quantidadeItens: Int,
    val itens: List<ItemPedido>
)

/** Pedidos finalizados no carrinho, salvos no aparelho enquanto não há backend. */
class PedidoRepositorio(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("pedidos", Context.MODE_PRIVATE)

    fun registrar(
        numero: Long,
        idCliente: Long,
        nomeCliente: String,
        emailCliente: String,
        subtotal: Double,
        desconto: Double,
        frete: Double,
        total: Double,
        formaPagamento: String,
        itens: List<ItemPedido>
    ) {
        val pedido = Pedido(
            idPedido = numero,
            idCliente = idCliente,
            idEndereco = 0, // retirada/entrega ainda sem cadastro de endereço
            dataPedido = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()),
            subtotal = subtotal,
            desconto = desconto,
            frete = frete,
            total = total,
            status = STATUS.first()
        )
        salvar(listOf(PedidoRegistrado(pedido, nomeCliente, emailCliente, formaPagamento, itens.sumOf { it.quantidade }, itens)) + todos())
    }

    /** Todos os pedidos, do mais recente para o mais antigo. */
    fun todos(): List<PedidoRegistrado> {
        val lista = JSONArray(prefs.getString(CHAVE, null) ?: "[]")
        return (0 until lista.length()).map { paraPedido(lista.getJSONObject(it)) }
    }

    /** Pedidos de demonstração (ver [DadosExemplo]), mantendo a lista do mais recente para o mais antigo. */
    fun adicionarExemplos(exemplos: List<PedidoRegistrado>) {
        salvar((todos() + exemplos).sortedByDescending { it.pedido.dataPedido })
    }

    fun alterarStatus(idPedido: Long, status: String) {
        salvar(todos().map {
            if (it.pedido.idPedido == idPedido) {
                PedidoRegistrado(it.pedido.copy(status = status), it.nomeCliente, it.emailCliente, it.formaPagamento, it.quantidadeItens, it.itens)
            } else it
        })
    }

    private fun salvar(pedidos: List<PedidoRegistrado>) {
        val lista = JSONArray()
        pedidos.forEach { lista.put(paraJson(it)) }
        prefs.edit { putString(CHAVE, lista.toString()) }
    }

    private fun paraJson(r: PedidoRegistrado) = JSONObject().apply {
        put("idPedido", r.pedido.idPedido)
        put("idCliente", r.pedido.idCliente)
        put("idEndereco", r.pedido.idEndereco)
        put("dataPedido", r.pedido.dataPedido)
        put("subtotal", r.pedido.subtotal)
        put("desconto", r.pedido.desconto)
        put("frete", r.pedido.frete)
        put("total", r.pedido.total)
        put("status", r.pedido.status)
        put("nomeCliente", r.nomeCliente)
        put("emailCliente", r.emailCliente)
        put("formaPagamento", r.formaPagamento)
        put("quantidadeItens", r.quantidadeItens)
        put("itens", JSONArray().apply {
            r.itens.forEach { item ->
                put(JSONObject().apply {
                    put("nome", item.nome)
                    put("quantidade", item.quantidade)
                    put("precoUnitario", item.precoUnitario)
                })
            }
        })
    }

    private fun paraPedido(j: JSONObject) = PedidoRegistrado(
        pedido = Pedido(
            idPedido = j.getLong("idPedido"),
            idCliente = j.getLong("idCliente"),
            idEndereco = j.getLong("idEndereco"),
            dataPedido = j.getString("dataPedido"),
            subtotal = j.getDouble("subtotal"),
            desconto = j.getDouble("desconto"),
            frete = j.getDouble("frete"),
            total = j.getDouble("total"),
            status = j.getString("status")
        ),
        nomeCliente = j.getString("nomeCliente"),
        emailCliente = j.getString("emailCliente"),
        formaPagamento = j.getString("formaPagamento"),
        quantidadeItens = j.getInt("quantidadeItens"),
        // Pedidos gravados antes desta versão não têm a lista de itens
        itens = j.optJSONArray("itens")?.let { lista ->
            (0 until lista.length()).map {
                val item = lista.getJSONObject(it)
                ItemPedido(item.getString("nome"), item.getInt("quantidade"), item.getDouble("precoUnitario"))
            }
        }.orEmpty()
    )

    companion object {
        /** Etapas do pedido, na ordem. */
        val STATUS = listOf("Pagamento confirmado", "Em separação", "Enviado", "Entregue", "Cancelado")
        private const val CHAVE = "pedidos"
    }
}
