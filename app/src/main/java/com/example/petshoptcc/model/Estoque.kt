package com.example.petshoptcc.model

data class Estoque(
    val idEstoque: Long,
    val idProduto: Long,
    val quantidade: Int,
    val estoqueMinimo: Int,
    val estoqueMaximo: Int,
    val dataAtualizacao: String
)