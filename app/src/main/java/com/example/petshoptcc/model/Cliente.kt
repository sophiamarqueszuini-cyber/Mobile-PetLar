package com.example.petshoptcc.model

data class Cliente(
    val idCliente: Long,
    val idUsuario: Long,
    val observacoes: String?,
    val status: String,
    val dataCadastro: String
)