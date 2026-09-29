package com.example.petshoptcc.model

data class Pet(
    val idPet: Long,
    val idCliente: Long,
    val idEspecie: Long,
    val idRaca: Long,
    val nome: String,
    val sexo: String,
    val dataNascimento: String?,
    val peso: Double?,
    val cor: String?,
    val castrado: Boolean,
    val observacoes: String?,
    val status: Boolean,
    val dataCadastro: String
)