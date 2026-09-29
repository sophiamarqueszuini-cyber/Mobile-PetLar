package com.example.petshoptcc.model

data class Carrinho(
    val idCarrinho: Long,
    val idCliente: Long,
    val dataCriacao: String,
    val dataAtualizacao: String,
    val status: String
)