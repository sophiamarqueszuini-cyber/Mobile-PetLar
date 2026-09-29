package com.example.petshoptcc.activity

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.petshoptcc.R
import com.example.petshoptcc.data.CarrinhoRepositorio
import com.example.petshoptcc.data.Catalogo
import com.example.petshoptcc.data.UsuarioRepositorio
import com.example.petshoptcc.databinding.ActivityMainBinding
import com.example.petshoptcc.databinding.ItemContatoBinding
import com.example.petshoptcc.databinding.ItemContatoCartaoBinding
import com.example.petshoptcc.databinding.ItemMenuLinkBinding
import com.example.petshoptcc.databinding.ItemPropositoBinding
import com.example.petshoptcc.ui.adicionarNaGrade
import com.example.petshoptcc.ui.atualizar
import com.example.petshoptcc.ui.criarCartaoGaleria
import com.example.petshoptcc.ui.criarCartaoProduto
import com.example.petshoptcc.util.abrirLink
import com.example.petshoptcc.util.configurarTela
import com.example.petshoptcc.util.emReais

/** Página inicial com as mesmas seções do site PetLar Sanctuary. */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var carrinho: CarrinhoRepositorio
    private val espacoEntreLinhas by lazy { resources.getDimensionPixelSize(R.dimen.espaco_entre_linhas) }

    /** Links da navbar e a seção de cada um (cada link existe na linha do logo e na segunda linha). */
    private val linksMenu = mutableListOf<Pair<TextView, View>>()
    private val espacoLink by lazy { resources.getDimensionPixelSize(R.dimen.espaco_link_menu) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val usuario = UsuarioRepositorio(this).usuarioLogado()
        if (usuario == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarTela(binding.main)
        carrinho = CarrinhoRepositorio(this)

        binding.txtSaudacao.text = getString(R.string.inicio_saudacao, usuario.nome.substringBefore(' '))
        binding.imgLogo.setOnClickListener { binding.rolagem.smoothScrollTo(0, 0) }
        binding.carrinho.btnCarrinho.setOnClickListener { startActivity(Intent(this, Carrinho::class.java)) }
        binding.btnPerfil.setOnClickListener { startActivity(Intent(this, Perfil::class.java)) }
        binding.btnReservar.setOnClickListener { abrirLink(Catalogo.WHATSAPP_AGENDAR) }
        binding.btnVerBoutique.setOnClickListener { startActivity(Intent(this, Produtos::class.java)) }
        binding.cenaCartao.setOnClickListener { virarCartao() }

        montarNavbar()
        montarPropositos()
        montarCuidados()
        montarBoutique()
        montarEspacoTutor()
        montarContatos()
    }

    override fun onResume() {
        super.onResume()
        binding.carrinho.atualizar(carrinho)
    }

    /** Navbar do site: Nosso Propósito, Cuidados, Boutique, Espaço Tutor e Contato. */
    private fun montarNavbar() {
        val secoes = listOf(
            R.string.nav_proposito to binding.secaoProposito,
            R.string.nav_cuidados to binding.secaoCuidados,
            R.string.nav_boutique to binding.secaoBoutique,
            R.string.nav_tutor to binding.secaoTutor,
            R.string.nav_contato to binding.secaoContato
        )
        val idsSegundaLinha = mutableListOf<Int>()
        secoes.forEach { (titulo, secao) ->
            // Tela larga: links ao lado do logo, como no site
            criarLink(binding.menuEmLinha, titulo, secao)
            // Tela vertical: links na segunda linha da navbar, organizados pelo Flow
            // (o id precisa existir antes de entrar no ConstraintLayout para o Flow achar o link)
            criarLink(binding.menuSegundaLinha, titulo, secao) {
                id = View.generateViewId()
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                minHeight = resources.getDimensionPixelSize(R.dimen.altura_link_menu)
                minimumHeight = minHeight
                setPadding(espacoLink, 0, espacoLink, 0)
                idsSegundaLinha += id
            }
        }
        binding.fluxoMenu.referencedIds = idsSegundaLinha.toIntArray()
        binding.rolagem.setOnScrollChangeListener { _, _, y, _, _ -> destacarSecao(y) }
        binding.rolagem.post { destacarSecao(binding.rolagem.scrollY) }
    }

    private fun criarLink(destino: ViewGroup, titulo: Int, secao: View, ajustar: TextView.() -> Unit = {}) {
        val link = ItemMenuLinkBinding.inflate(layoutInflater, destino, false).root
        link.setText(titulo)
        link.setOnClickListener { binding.rolagem.smoothScrollTo(0, secao.top) }
        link.ajustar()
        destino.addView(link)
        linksMenu += link to secao
    }

    /** Deixa em marrom o link da seção que está na tela, como o hover do site. */
    private fun destacarSecao(y: Int) {
        val limite = y + binding.rolagem.height / 3
        val atual = linksMenu.map { it.second }.distinct().lastOrNull { it.top <= limite }
        linksMenu.forEach { (link, secao) -> link.isSelected = secao == atual }
    }

    private fun montarPropositos() {
        Catalogo.propositos.forEach { item ->
            val cartao = ItemPropositoBinding.inflate(layoutInflater, binding.gridPropositos, false)
            cartao.txtTitulo.text = item.titulo
            cartao.txtDescricao.text = item.descricao
            binding.gridPropositos.adicionarNaGrade(cartao.root)
        }
    }

    private fun montarCuidados() {
        Catalogo.cuidados.forEach { item ->
            val cartao = layoutInflater.criarCartaoGaleria(
                binding.listaCuidados,
                imagem = item.imagem,
                titulo = item.servico.nome,
                descricao = item.servico.descricao,
                preco = getString(R.string.cuidados_preco, item.servico.preco.emReais())
            )
            binding.listaCuidados.adicionarNaGrade(cartao, espacoAbaixo = espacoEntreLinhas)
        }
    }

    private fun montarBoutique() {
        Catalogo.produtos.forEach { item ->
            val cartao = layoutInflater.criarCartaoProduto(binding.listaBoutique, item, carrinho) {
                binding.carrinho.atualizar(carrinho)
            }
            binding.listaBoutique.adicionarNaGrade(cartao, espacoAbaixo = espacoEntreLinhas)
        }
    }

    private fun montarEspacoTutor() {
        Catalogo.espacoTutor.forEach { item ->
            val cartao = layoutInflater.criarCartaoGaleria(
                binding.listaTutor, item.imagem, item.titulo, item.descricao
            )
            binding.listaTutor.adicionarNaGrade(cartao, espacoAbaixo = espacoEntreLinhas)
        }
    }

    private fun montarContatos() {
        Catalogo.contatos.forEach { contato ->
            val linha = ItemContatoBinding.inflate(layoutInflater, binding.listaContatos, true)
            linha.txtCanal.text = getString(R.string.contato_canal, contato.canal)
            linha.txtValor.text = contato.valor
            linha.root.setOnClickListener { abrirLink(contato.link) }
        }

        // Verso do cartão digital
        val itensCartao = listOf(getString(R.string.cartao_endereco) to Catalogo.ENDERECO) +
            Catalogo.contatos.take(3).map { it.canal to it.valor }
        itensCartao.forEach { (rotulo, valor) ->
            val item = ItemContatoCartaoBinding.inflate(layoutInflater, binding.listaContatosCartao, true)
            item.txtRotulo.text = rotulo
            item.txtValor.text = valor
        }

        binding.txtRodapeContatos.text = Catalogo.contatos.joinToString("\n") { it.valor }
        binding.txtRodapeEndereco.text = Catalogo.ENDERECO
    }

    /** Gira o cartão como o .card-scene.flipped do site. */
    private fun virarCartao() {
        val cena = binding.cenaCartao
        val mostrarVerso = !binding.cartaoVerso.isVisible
        cena.cameraDistance = 8000 * resources.displayMetrics.density
        cena.animate().rotationY(90f).setDuration(180).withEndAction {
            binding.cartaoFrente.isVisible = !mostrarVerso
            binding.cartaoVerso.isVisible = mostrarVerso
            cena.rotationY = -90f
            cena.animate().rotationY(0f).setDuration(180).start()
        }.start()
    }
}
