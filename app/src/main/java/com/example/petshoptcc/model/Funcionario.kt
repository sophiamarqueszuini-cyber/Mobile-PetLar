package com.example.petshoptcc.model

data class Funcionario(
    val idFuncionario: Long,
    val idUsuario: Long?,
    val nome: String,
    val cpf: String,
    val telefone: String,
    val cargo: String,
    val dataAdmissao: String,
    val status: Boolean
)