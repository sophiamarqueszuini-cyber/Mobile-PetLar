package com.example.petshoptcc.model

data class Usuario(
    /** uid da conta no Firebase Authentication (o mesmo do site). */
    val idUsuario: String,
    val nome: String,
    val email: String,
    val telefone: String?,
    val cpf: String?,
    val dataNascimento: String?,
    val status: String,
    val emailVerificado: Boolean,
    val ultimoLogin: String?,
    val dataCadastro: String,
    val dataAtualizacao: String
)