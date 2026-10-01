package com.example.petshoptcc.data

import android.content.Context
import androidx.core.content.edit
import com.example.petshoptcc.model.Pedido
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToLong

/**
 * Clientes e pedidos de demonstração para o painel administrativo não abrir vazio.
 * São gravados uma única vez por aparelho; as datas são relativas ao dia da
 * instalação para os pedidos parecerem recentes. Todos os clientes usam a senha [SENHA].
 */
object DadosExemplo {

    const val SENHA = "cliente123"

    private class ClienteExemplo(
        val id: Long, val nome: String, val email: String,
        val telefone: String?, val cpf: String?, val diasDesdeCadastro: Int
    )

    /** [itens]: id do produto no [Catalogo] para quantidade. [frete] e [pagamento] seguem as opções do carrinho. */
    private class PedidoExemplo(
        val numero: Long, val idCliente: Long, val diasAtras: Int, val hora: Int,
        val itens: Map<Long, Int>, val frete: Double, val pagamento: Int, val cupom: Boolean, val status: Int
    )

    private val clientes = listOf(
        ClienteExemplo(1001, "Mariana Costa", "mariana.costa@email.com", "11987654321", "39053344705", 60),
        ClienteExemplo(1002, "Rafael Almeida", "rafael.almeida@email.com", "11991234567", null, 48),
        ClienteExemplo(1003, "Juliana Ferreira", "juliana.ferreira@email.com", "11976543210", "52998224725", 35),
        ClienteExemplo(1004, "Lucas Oliveira", "lucas.oliveira@email.com", null, null, 21),
        ClienteExemplo(1005, "Beatriz Santos", "beatriz.santos@email.com", "11965432109", null, 12),
        ClienteExemplo(1006, "Pedro Henrique Lima", "pedro.lima@email.com", "11954321098", "11144477735", 4)
    )

    // Mesmos valores e títulos das opções do carrinho
    private const val RETIRADA = 0.0
    private const val ENTREGA_PADRAO = 15.90
    private const val ENTREGA_EXPRESSA = 24.90
    private val formasPagamento = listOf("⚡ PIX", "💳 Cartão de Crédito", "📄 Boleto Bancário")
    private const val PIX = 0
    private const val CARTAO = 1
    private const val BOLETO = 2

    // Índices de PedidoRepositorio.STATUS
    private const val CONFIRMADO = 0
    private const val SEPARACAO = 1
    private const val ENVIADO = 2
    private const val ENTREGUE = 3
    private const val CANCELADO = 4

    private val pedidos = listOf(
        PedidoExemplo(482913, 1001, 55, 10, mapOf(1L to 2, 3L to 1), ENTREGA_PADRAO, PIX, false, ENTREGUE),
        PedidoExemplo(517204, 1002, 40, 16, mapOf(2L to 1), RETIRADA, CARTAO, false, ENTREGUE),
        PedidoExemplo(538761, 1003, 30, 9, mapOf(5L to 1, 6L to 1), ENTREGA_EXPRESSA, CARTAO, true, ENTREGUE),
        PedidoExemplo(604158, 1001, 18, 19, mapOf(4L to 2, 7L to 1), ENTREGA_PADRAO, BOLETO, false, CANCELADO),
        PedidoExemplo(629347, 1004, 9, 14, mapOf(1L to 1, 8L to 1), ENTREGA_PADRAO, PIX, true, ENVIADO),
        PedidoExemplo(655820, 1005, 5, 11, mapOf(2L to 1, 3L to 2), ENTREGA_EXPRESSA, CARTAO, false, ENVIADO),
        PedidoExemplo(671093, 1003, 2, 17, mapOf(6L to 2, 8L to 1), RETIRADA, PIX, false, SEPARACAO),
        PedidoExemplo(698412, 1006, 0, 9, mapOf(1L to 1, 4L to 1, 7L to 1), ENTREGA_PADRAO, CARTAO, true, CONFIRMADO)
    )

    /** Grava os exemplos na primeira vez que o app abre; depois não faz nada. */
    fun carregarSeNecessario(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences("dados_exemplo", Context.MODE_PRIVATE)
        if (prefs.getBoolean(CHAVE_CARREGADO, false)) return

        val usuarios = UsuarioRepositorio(context)
        val cadastrados = clientes.mapNotNull { c ->
            usuarios.cadastrarExemplo(c.id, c.nome, c.email, c.telefone, c.cpf, SENHA, data(c.diasDesdeCadastro, 8))
        }.associateBy { it.idUsuario }

        // Pedido só entra se o cliente foi criado agora (e-mail ainda não existia no aparelho)
        PedidoRepositorio(context).adicionarExemplos(pedidos.mapNotNull { p ->
            val cliente = cadastrados[p.idCliente] ?: return@mapNotNull null
            val subtotal = p.itens.entries.sumOf { (id, qtd) -> Catalogo.produtos.first { it.produto.idProduto == id }.produto.preco * qtd }
            val descontoCupom = if (p.cupom) subtotal * 0.10 else 0.0
            val descontoPix = if (p.pagamento == PIX) (subtotal - descontoCupom) * 0.05 else 0.0
            val desconto = centavos(descontoCupom + descontoPix)
            PedidoRegistrado(
                pedido = Pedido(
                    idPedido = p.numero,
                    idCliente = cliente.idUsuario,
                    idEndereco = 0,
                    dataPedido = data(p.diasAtras, p.hora),
                    subtotal = subtotal,
                    desconto = desconto,
                    frete = p.frete,
                    total = centavos(subtotal - desconto + p.frete),
                    status = PedidoRepositorio.STATUS[p.status]
                ),
                nomeCliente = cliente.nome,
                emailCliente = cliente.email,
                formaPagamento = formasPagamento[p.pagamento],
                quantidadeItens = p.itens.values.sum()
            )
        })

        prefs.edit { putBoolean(CHAVE_CARREGADO, true) }
    }

    private fun data(diasAtras: Int, hora: Int): String {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -diasAtras)
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, (diasAtras * 7 + hora * 3) % 60)
            set(Calendar.SECOND, 0)
        }
        val agora = Calendar.getInstance()
        if (cal.after(agora)) cal.timeInMillis = agora.timeInMillis - 30 * 60 * 1000
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(cal.time)
    }

    private fun centavos(valor: Double) = (valor * 100).roundToLong() / 100.0

    private const val CHAVE_CARREGADO = "carregado"
}
