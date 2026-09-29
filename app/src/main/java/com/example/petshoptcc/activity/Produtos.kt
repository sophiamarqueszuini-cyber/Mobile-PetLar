package com.example.petshoptcc.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.petshoptcc.R
import com.example.petshoptcc.data.CarrinhoRepositorio
import com.example.petshoptcc.data.Catalogo
import com.example.petshoptcc.databinding.ActivityProdutosBinding
import com.example.petshoptcc.ui.adicionarNaGrade
import com.example.petshoptcc.ui.atualizar
import com.example.petshoptcc.ui.criarCartaoProduto
import com.example.petshoptcc.util.configurarTela

/** Boutique completa: todos os produtos do site em grade (2 colunas na vertical, 4 na horizontal). */
class Produtos : AppCompatActivity() {

    private lateinit var binding: ActivityProdutosBinding
    private lateinit var carrinho: CarrinhoRepositorio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProdutosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarTela(binding.main, telaEscura = true)
        carrinho = CarrinhoRepositorio(this)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.carrinho.btnCarrinho.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.petlar_escuro_texto))
        binding.carrinho.btnCarrinho.setOnClickListener {
            startActivity(Intent(this, Carrinho::class.java))
        }

        Catalogo.produtos.forEach { item ->
            val cartao = layoutInflater.criarCartaoProduto(binding.gridProdutos, item, carrinho) {
                binding.carrinho.atualizar(carrinho)
            }
            binding.gridProdutos.adicionarNaGrade(
                cartao, espacoAbaixo = resources.getDimensionPixelSize(R.dimen.espaco_entre_linhas)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        binding.carrinho.atualizar(carrinho)
    }
}
