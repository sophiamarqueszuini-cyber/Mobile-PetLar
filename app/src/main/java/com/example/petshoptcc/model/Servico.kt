package com.example.petshoptcc.model

data class Servico(
    val idServico: Long,
    val nome: String,
    val descricao: String?,
    val preco: Double,
    val duracao: Int,
    val status: Boolean
)