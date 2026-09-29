package com.example.petshoptcc.model

data class Endereco(
    val idEndereco: Long,
    val idCliente: Long,
    val cep: String,
    val logradouro: String,
    val numero: String,
    val complemento: String?,
    val bairro: String,
    val cidade: String,
    val estado: String,
    val principal: Boolean,
    val status: Boolean
)