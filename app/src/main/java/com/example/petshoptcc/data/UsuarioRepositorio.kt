package com.example.petshoptcc.data

import android.content.Context
import androidx.core.content.edit
import com.example.petshoptcc.model.Perfil
import com.example.petshoptcc.model.Usuario
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Os dois acessos do app, ligados aos perfis do banco (tabela Perfil). */
enum class TipoAcesso(val perfil: Perfil) {
    CLIENTE(Perfil(1, "Cliente", "Tutores que compram e agendam serviços", true)),
    ADMINISTRATIVO(Perfil(2, "Administrativo", "Equipe PetLar: pedidos e clientes", true));

    companion object {
        fun doPerfil(idPerfil: Long) = entries.firstOrNull { it.perfil.idPerfil == idPerfil } ?: CLIENTE
    }
}

sealed interface ResultadoLogin {
    data class Sucesso(val usuario: Usuario, val tipo: TipoAcesso) : ResultadoLogin
    /** E-mail ou senha errados. */
    data object Invalido : ResultadoLogin
}

/**
 * Guarda os usuários no próprio aparelho (SharedPreferences) enquanto o app
 * ainda não tem backend. Quando a API existir, basta trocar a implementação
 * destes métodos pelas chamadas ao servidor.
 */
class UsuarioRepositorio(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("usuarios", Context.MODE_PRIVATE)

    init {
        // Sem backend não há como cadastrar a equipe, então o app cria uma conta
        // administrativa padrão. Troque a senha quando houver servidor.
        if (!emailCadastrado(ADMIN_EMAIL)) {
            criar("Administração PetLar", ADMIN_EMAIL, null, null, ADMIN_SENHA, TipoAcesso.ADMINISTRATIVO)
        }
    }

    fun emailCadastrado(email: String): Boolean = prefs.contains(chave(email))

    /** Cadastro pelo app: sempre cria um cliente. */
    fun cadastrar(nome: String, email: String, telefone: String?, cpf: String?, senha: String): Boolean {
        if (emailCadastrado(email)) return false
        criar(nome, email, telefone, cpf, senha, TipoAcesso.CLIENTE)
        return true
    }

    /** Cliente de demonstração (ver [DadosExemplo]): igual ao cadastro, mas com a data informada. */
    fun cadastrarExemplo(
        id: Long, nome: String, email: String, telefone: String?, cpf: String?, senha: String, dataCadastro: String
    ): Usuario? {
        if (emailCadastrado(email)) return null
        criar(nome, email, telefone, cpf, senha, TipoAcesso.CLIENTE, dataCadastro, id)
        return buscarJson(email)?.let(::paraUsuario)
    }

    /** O tipo de acesso vem do perfil da conta, não de uma escolha na tela. */
    fun entrar(email: String, senha: String): ResultadoLogin {
        val json = buscarJson(email) ?: return ResultadoLogin.Invalido
        val usuario = paraUsuario(json)
        if (usuario.senhaHash != hash(senha)) return ResultadoLogin.Invalido
        val tipoDaConta = TipoAcesso.doPerfil(json.optLong("idPerfil", TipoAcesso.CLIENTE.perfil.idPerfil))

        val atualizado = usuario.copy(ultimoLogin = agora())
        salvar(atualizado, tipoDaConta)
        prefs.edit { putString(CHAVE_SESSAO, atualizado.email) }
        return ResultadoLogin.Sucesso(atualizado, tipoDaConta)
    }

    fun usuarioLogado(): Usuario? = prefs.getString(CHAVE_SESSAO, null)?.let { buscarJson(it) }?.let(::paraUsuario)

    fun tipoLogado(): TipoAcesso? = prefs.getString(CHAVE_SESSAO, null)?.let { buscarJson(it) }
        ?.let { TipoAcesso.doPerfil(it.optLong("idPerfil", TipoAcesso.CLIENTE.perfil.idPerfil)) }

    /** Clientes cadastrados, do mais recente para o mais antigo (painel administrativo). */
    fun clientes(): List<Usuario> = prefs.all
        .filterKeys { it.startsWith(PREFIXO) }
        .values.mapNotNull { (it as? String)?.let(::JSONObject) }
        .filter { TipoAcesso.doPerfil(it.optLong("idPerfil", 1)) == TipoAcesso.CLIENTE }
        .map(::paraUsuario)
        .sortedByDescending { it.dataCadastro }

    fun sair() {
        prefs.edit { remove(CHAVE_SESSAO) }
    }

    private fun criar(
        nome: String, email: String, telefone: String?, cpf: String?, senha: String, tipo: TipoAcesso,
        agora: String = agora(), id: Long = System.currentTimeMillis()
    ) {
        salvar(
            Usuario(
                idUsuario = id,
                nome = nome,
                email = email.trim().lowercase(),
                telefone = telefone,
                cpf = cpf,
                senhaHash = hash(senha),
                dataNascimento = null,
                status = "ATIVO",
                emailVerificado = false,
                ultimoLogin = null,
                dataCadastro = agora,
                dataAtualizacao = agora
            ),
            tipo
        )
    }

    private fun buscarJson(email: String): JSONObject? = prefs.getString(chave(email), null)?.let(::JSONObject)

    private fun salvar(usuario: Usuario, tipo: TipoAcesso) {
        val json = paraJson(usuario).put("idPerfil", tipo.perfil.idPerfil)
        prefs.edit { putString(chave(usuario.email), json.toString()) }
    }

    private fun chave(email: String) = PREFIXO + email.trim().lowercase()

    private fun agora() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

    private fun hash(senha: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(senha.toByteArray())
            .joinToString("") { "%02x".format(it) }

    private fun paraJson(u: Usuario) = JSONObject().apply {
        put("idUsuario", u.idUsuario)
        put("nome", u.nome)
        put("email", u.email)
        put("telefone", u.telefone ?: JSONObject.NULL)
        put("cpf", u.cpf ?: JSONObject.NULL)
        put("senhaHash", u.senhaHash)
        put("dataNascimento", u.dataNascimento ?: JSONObject.NULL)
        put("status", u.status)
        put("emailVerificado", u.emailVerificado)
        put("ultimoLogin", u.ultimoLogin ?: JSONObject.NULL)
        put("dataCadastro", u.dataCadastro)
        put("dataAtualizacao", u.dataAtualizacao)
    }

    private fun paraUsuario(j: JSONObject) = Usuario(
        idUsuario = j.getLong("idUsuario"),
        nome = j.getString("nome"),
        email = j.getString("email"),
        telefone = j.textoOuNulo("telefone"),
        cpf = j.textoOuNulo("cpf"),
        senhaHash = j.getString("senhaHash"),
        dataNascimento = j.textoOuNulo("dataNascimento"),
        status = j.getString("status"),
        emailVerificado = j.getBoolean("emailVerificado"),
        ultimoLogin = j.textoOuNulo("ultimoLogin"),
        dataCadastro = j.getString("dataCadastro"),
        dataAtualizacao = j.getString("dataAtualizacao")
    )

    private fun JSONObject.textoOuNulo(campo: String): String? =
        if (has(campo) && !isNull(campo)) getString(campo) else null

    companion object {
        const val ADMIN_EMAIL = "admin@petlar.com"
        const val ADMIN_SENHA = "admin123"
        private const val CHAVE_SESSAO = "sessao_email"
        private const val PREFIXO = "usuario_"
    }
}
