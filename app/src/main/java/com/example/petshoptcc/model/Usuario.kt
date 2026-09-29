package com.example.petshoptcc.model

data class Usuario(
    val idUsuario: Long,
    val nome: String,
    val email: String,
    val telefone: String?,
    val cpf: String?,
    val senhaHash: String,
    val dataNascimento: String?,
    val status: String,
    val emailVerificado: Boolean,
    val ultimoLogin: String?,
    val dataCadastro: String,
    val dataAtualizacao: String
)