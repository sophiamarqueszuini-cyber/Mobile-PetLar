package com.example.petshoptcc.model

data class Agendamento(
    val idAgendamento: Long,
    val idCliente: Long,
    val idPet: Long,
    val idServico: Long,
    val idFuncionario: Long,
    val dataHora: String,
    val valor: Double,
    val status: String,
    val observacoes: String?
)