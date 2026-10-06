package com.example.petshoptcc.model

data class Pedido(
    val idPedido: Long,
    val idCliente: String,
    val idEndereco: Long,
    val dataPedido: String,
    val subtotal: Double,
    val desconto: Double,
    val frete: Double,
    val total: Double,
    val status: String
)