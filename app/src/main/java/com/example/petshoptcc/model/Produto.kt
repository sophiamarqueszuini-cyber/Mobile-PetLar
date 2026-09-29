package com.example.petshoptcc.model

data class Produto(
    val idProduto: Long,
    val idCategoria: Long,
    val idMarca: Long,
    val nome: String,
    val codigoBarras: String?,
    val sku: String,
    val descricao: String?,
    val preco: Double,
    val custo: Double,
    val peso: Double?,
    val unidadeMedida: String,
    val status: Boolean,
    val dataCadastro: String,
    val dataAtualizacao: String
)