package com.example.petshoptcc.model

data class Pagamento(
    val idPagamento: Long,
    val idPedido: Long,
    val formaPagamento: String,
    val valor: Double,
    val dataPagamento: String,
    val status: String,
    val codigoTransacao: String?
)