package com.example.petshoptcc.data

import com.example.petshoptcc.model.Pedido
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Produto comprado, com nome e preço do momento da compra. */
class ItemPedido(val nome: String, val quantidade: Int, val precoUnitario: Double) {
    val subtotal: Double get() = precoUnitario * quantidade
}

/** Pedido com os dados que o painel administrativo mostra junto (cliente, pagamento, itens). [ref] é o id do documento no Firestore. */
class PedidoRegistrado(
    val ref: String,
    val pedido: Pedido,
    val nomeCliente: String,
    val emailCliente: String,
    val formaPagamento: String,
    val quantidadeItens: Int,
    val itens: List<ItemPedido>
)

/**
 * Pedidos finalizados no carrinho, na coleção "pedidos" do Firestore: os mesmos do site,
 * então o painel administrativo mostra as compras feitas no app e no site.
 */
class PedidoRepositorio {

    private val pedidos = FirebaseFirestore.getInstance().collection(COLECAO)

    suspend fun registrar(
        numero: Long,
        idCliente: String,
        nomeCliente: String,
        emailCliente: String,
        subtotal: Double,
        desconto: Double,
        frete: Double,
        total: Double,
        formaPagamento: String,
        itens: List<ItemPedido>
    ) {
        val dados = hashMapOf(
            "idPedido" to numero,
            "idCliente" to idCliente,
            "idEndereco" to 0L, // retirada/entrega ainda sem cadastro de endereço
            "dataPedido" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()),
            "subtotal" to subtotal,
            "desconto" to desconto,
            "frete" to frete,
            "total" to total,
            "status" to STATUS.first(),
            "nomeCliente" to nomeCliente,
            "emailCliente" to emailCliente,
            "formaPagamento" to formaPagamento,
            "quantidadeItens" to itens.sumOf { it.quantidade },
            "itens" to itens.map {
                mapOf("nome" to it.nome, "quantidade" to it.quantidade, "precoUnitario" to it.precoUnitario)
            }
        )
        pedidos.add(dados).await()
    }

    /** Todos os pedidos, do mais recente para o mais antigo (só a equipe tem permissão). */
    suspend fun todos(): List<PedidoRegistrado> = pedidos
        .orderBy("dataPedido", Query.Direction.DESCENDING)
        .get().await()
        .documents.map(::paraPedido)

    suspend fun alterarStatus(ref: String, status: String) {
        pedidos.document(ref).update("status", status).await()
    }

    /** Números do Firestore podem vir como Long ou Double (o site grava 120 como inteiro). */
    private fun DocumentSnapshot.numero(campo: String): Double = (get(campo) as? Number)?.toDouble() ?: 0.0

    private fun paraPedido(d: DocumentSnapshot) = PedidoRegistrado(
        ref = d.id,
        pedido = Pedido(
            idPedido = d.numero("idPedido").toLong(),
            idCliente = d.getString("idCliente").orEmpty(),
            idEndereco = d.numero("idEndereco").toLong(),
            dataPedido = d.getString("dataPedido").orEmpty(),
            subtotal = d.numero("subtotal"),
            desconto = d.numero("desconto"),
            frete = d.numero("frete"),
            total = d.numero("total"),
            status = d.getString("status") ?: STATUS.first()
        ),
        nomeCliente = d.getString("nomeCliente").orEmpty(),
        emailCliente = d.getString("emailCliente").orEmpty(),
        formaPagamento = d.getString("formaPagamento").orEmpty(),
        quantidadeItens = d.numero("quantidadeItens").toInt(),
        itens = (d.get("itens") as? List<*>).orEmpty().mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            ItemPedido(
                nome = m["nome"] as? String ?: return@mapNotNull null,
                quantidade = (m["quantidade"] as? Number)?.toInt() ?: 1,
                precoUnitario = (m["precoUnitario"] as? Number)?.toDouble() ?: 0.0
            )
        }
    )

    companion object {
        /** Etapas do pedido, na ordem (as mesmas do site e das regras do Firestore). */
        val STATUS = listOf("Pagamento confirmado", "Em separação", "Enviado", "Entregue", "Cancelado")
        private const val COLECAO = "pedidos"
    }
}
