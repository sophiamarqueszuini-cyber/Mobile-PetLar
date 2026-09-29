package com.example.petshoptcc.data

import androidx.annotation.DrawableRes
import com.example.petshoptcc.R
import com.example.petshoptcc.model.Categoria
import com.example.petshoptcc.model.Produto
import com.example.petshoptcc.model.Servico

/** Produto da boutique com os dados de vitrine (categoria e foto). */
class ProdutoBoutique(val produto: Produto, val categoria: Categoria, @param:DrawableRes val imagem: Int)

/** Serviço da seção "Cuidados" com a foto exibida no cartão. */
class CuidadoPetLar(val servico: Servico, @param:DrawableRes val imagem: Int)

/** Conteúdo em cartão com foto, título e descrição (Espaço do Tutor). */
class Experiencia(val titulo: String, val descricao: String, @param:DrawableRes val imagem: Int)

/** Destaques da seção "Nosso Propósito". */
class Proposito(val titulo: String, val descricao: String)

/** Canais da seção "Contato & Redes". */
class Contato(val canal: String, val valor: String, val link: String)

/**
 * Mesmo conteúdo do site PetLar Sanctuary. Enquanto não há API,
 * os dados ficam aqui; depois é só substituir pela resposta do servidor.
 */
object Catalogo {

    const val WHATSAPP = "https://wa.me/5511988772211"
    const val WHATSAPP_AGENDAR =
        "$WHATSAPP?text=Ol%C3%A1!%20Gostaria%20de%20agendar%20um%20hor%C3%A1rio%20para%20os%20servi%C3%A7os%20do%20meu%20pet."
    const val ENDERECO = "Rua Acolhida, 128 — Pinheiros\nSão Paulo - SP, Brasil"

    private val categorias = listOf(
        Categoria(1, "Nutrição", null, true),
        Categoria(2, "Conforto", null, true),
        Categoria(3, "Alimentação", null, true),
        Categoria(4, "Higiene", null, true),
        Categoria(5, "Tratamento", null, true),
        Categoria(6, "Pêlos", null, true),
        Categoria(7, "Acessório", null, true),
        Categoria(8, "Fragrância", null, true)
    )

    val produtos = listOf(
        boutique(1, 1, "Ração Holística Equilíbrio Natural", 189.0, R.drawable.img_boutique_01),
        boutique(2, 2, "Manta de Linho para Descanso", 240.0, R.drawable.img_boutique_02),
        boutique(3, 3, "Comedouro em Cerâmica", 120.0, R.drawable.img_boutique_03),
        boutique(4, 4, "Shampoo Botânico Hipoalergênico", 78.0, R.drawable.img_boutique_04),
        boutique(5, 5, "Kit Máscara de Hidratação Pelagem", 350.0, R.drawable.img_boutique_05),
        boutique(6, 6, "Sérum Desembaraçador de Brilho", 85.0, R.drawable.img_boutique_06),
        boutique(7, 7, "Escova de Cerdas Macias para Pelagem", 70.0, R.drawable.img_boutique_07),
        boutique(8, 8, "Colônia Suave com Extratos Naturais", 95.0, R.drawable.img_boutique_08)
    )

    val cuidados = listOf(
        CuidadoPetLar(
            Servico(1, "Banho Low Stress & Ozônio", "Água ozonizada com ação bactericida, cicatrizante e combate a odores, sem uso de gaiolas.", 110.0, 60, true),
            R.drawable.img_cuidados_01
        ),
        CuidadoPetLar(
            Servico(2, "Aromaterapia & Cromoterapia", "Protocolos de redução de ansiedade através de essências botânicas e estímulos visuais calmantes.", 90.0, 45, true),
            R.drawable.img_cuidados_02
        ),
        CuidadoPetLar(
            Servico(3, "Tosa & Trimming de Raça", "Corte artesanal especializado e acabamento de alto padrão focado na anatomia e pelagem do pet.", 150.0, 90, true),
            R.drawable.img_cuidados_03
        ),
        CuidadoPetLar(
            Servico(4, "Hidratação Profunda", "Reposição hídrica com máscaras ricas em óleos essenciais para brilho intenso e maciez.", 75.0, 45, true),
            R.drawable.img_cuidados_04
        ),
        CuidadoPetLar(
            Servico(5, "Spa de Patinhas & Focinho", "Esfoliação suave e nutrição profunda com bálsamos protetores para coxins e trufas ressecadas.", 65.0, 30, true),
            R.drawable.img_cuidados_05
        ),
        CuidadoPetLar(
            Servico(6, "Day Spa Sanctuary", "Experiência imersiva completa com banho terapêutico, massagem relaxante, cuidados dentários e estética geral.", 260.0, 240, true),
            R.drawable.img_cuidados_06
        )
    )

    val propositos = listOf(
        Proposito("Atendimento exclusivo", "Ambientes planejados com hora marcada para o bem-estar absoluto."),
        Proposito("Transparência total", "Processos claros focados na segurança e conforto."),
        Proposito("Design funcional", "Harmonia perfeita entre a sua casa e seu pet."),
        Proposito("Saúde integrativa", "Cuidado holístico que abraça corpo e mente.")
    )

    val espacoTutor = listOf(
        Experiencia("Spa Exclusivo", "Protocolos de autocuidado, rituais de relaxamento e tratamentos de bem-estar projetados para renovar suas energias.", R.drawable.img_tutor_01),
        Experiencia("Café de Especialidade", "Bebidas selecionadas, baristas e infusões orgânicas em um espaço pensado para conversas leves e pausas revigorantes.", R.drawable.img_tutor_02),
        Experiencia("Massagem Terapêutica", "Sessões expressas ou completas de massagem para aliviar a tensão do dia a dia enquanto aguarda seu pet.", R.drawable.img_tutor_03)
    )

    val contatos = listOf(
        Contato("E-mail", "petlarsanctuary@gmail.com", "mailto:petlarsanctuary@gmail.com"),
        Contato("WhatsApp", "+55 (11) 98877-2211", WHATSAPP),
        Contato("Instagram", "@petlar_sanctuary", "https://instagram.com/petlar_sanctuary"),
        Contato("LinkedIn", "Petlar Sanctuary", "https://www.linkedin.com/in/petlar-sanctuary-64292842a/")
    )

    fun produto(id: Long): ProdutoBoutique? = produtos.find { it.produto.idProduto == id }

    private fun boutique(id: Long, idCategoria: Long, nome: String, preco: Double, @DrawableRes imagem: Int) =
        ProdutoBoutique(
            produto = Produto(
                idProduto = id,
                idCategoria = idCategoria,
                idMarca = 1,
                nome = nome,
                codigoBarras = null,
                sku = "PETLAR-%03d".format(id),
                descricao = null,
                preco = preco,
                custo = 0.0,
                peso = null,
                unidadeMedida = "UN",
                status = true,
                dataCadastro = "2024-01-01 00:00:00",
                dataAtualizacao = "2024-01-01 00:00:00"
            ),
            categoria = categorias.first { it.idCategoria == idCategoria },
            imagem = imagem
        )
}
