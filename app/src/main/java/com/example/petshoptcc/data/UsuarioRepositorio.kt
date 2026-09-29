package com.example.petshoptcc.data

import android.content.Context
import androidx.core.content.edit
import com.example.petshoptcc.model.Usuario
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Guarda os usuários no próprio aparelho (SharedPreferences) enquanto o app
 * ainda não tem backend. Quando a API existir, basta trocar a implementação
 * destes métodos pelas chamadas ao servidor.
 */
class UsuarioRepositorio(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("usuarios", Context.MODE_PRIVATE)

    fun emailCadastrado(email: String): Boolean = prefs.contains(chave(email))

    fun cadastrar(nome: String, email: String, telefone: String?, cpf: String?, senha: String): Boolean {
        if (emailCadastrado(email)) return false
        val agora = agora()
        salvar(
            Usuario(
                idUsuario = System.currentTimeMillis(),
                nome = nome,
                email = email.lowercase(),
                telefone = telefone,
                cpf = cpf,
                senhaHash = hash(senha),
                dataNascimento = null,
                status = "ATIVO",
                emailVerificado = false,
                ultimoLogin = null,
                dataCadastro = agora,
                dataAtualizacao = agora
            )
        )
        return true
    }

    fun autenticar(email: String, senha: String): Usuario? {
        val usuario = buscar(email) ?: return null
        if (usuario.senhaHash != hash(senha)) return null
        val atualizado = usuario.copy(ultimoLogin = agora())
        salvar(atualizado)
        prefs.edit { putString(CHAVE_SESSAO, atualizado.email) }
        return atualizado
    }

    fun usuarioLogado(): Usuario? = prefs.getString(CHAVE_SESSAO, null)?.let { buscar(it) }

    fun sair() {
        prefs.edit { remove(CHAVE_SESSAO) }
    }

    private fun buscar(email: String): Usuario? =
        prefs.getString(chave(email), null)?.let { paraUsuario(JSONObject(it)) }

    private fun salvar(usuario: Usuario) {
        prefs.edit { putString(chave(usuario.email), paraJson(usuario).toString()) }
    }

    private fun chave(email: String) = "usuario_" + email.trim().lowercase()

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

    private companion object {
        const val CHAVE_SESSAO = "sessao_email"
    }
}
