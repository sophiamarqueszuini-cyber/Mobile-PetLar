package com.example.petshoptcc.model

data class Categoria(
    val idCategoria: Long,
    val nome: String,
    val descricao: String?,
    val status: Boolean
)