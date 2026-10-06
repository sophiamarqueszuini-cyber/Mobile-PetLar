package com.example.petshoptcc.data

import android.content.Context
import androidx.core.content.edit
import com.example.petshoptcc.R
import com.example.petshoptcc.model.Perfil
import com.example.petshoptcc.model.Usuario
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
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
    /** Sem internet, muitas tentativas etc.: [mensagem] é um id de string pronto para a tela. */
    data class Falha(val mensagem: Int) : ResultadoLogin
}

sealed interface ResultadoCadastro {
    data class Sucesso(val usuario: Usuario) : ResultadoCadastro
    data object EmailEmUso : ResultadoCadastro
    data class Falha(val mensagem: Int) : ResultadoCadastro
}

/**
 * Contas no Firebase, as mesmas do site: o Authentication guarda e-mail e senha e o
 * Firestore guarda o cadastro na coleção "usuarios" (campos deste [Usuario] + idPerfil).
 * O cadastro do usuário logado também fica salvo no aparelho, para as telas lerem sem esperar a rede.
 */
class UsuarioRepositorio(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("sessao", Context.MODE_PRIVATE)
    private val auth = FirebaseAuth.getInstance()
    private val usuarios = FirebaseFirestore.getInstance().collection(COLECAO)

    /** O tipo de acesso vem do perfil da conta, não de uma escolha na tela. */
    suspend fun entrar(email: String, senha: String): ResultadoLogin = try {
        val conta = auth.signInWithEmailAndPassword(email.trim(), senha).await().user!!
        val doc = carregarOuCriar(conta, null, null, null)
        doc.put("ultimoLogin", agora())
        usuarios.document(conta.uid).update("ultimoLogin", doc.getString("ultimoLogin")).await()
        guardarSessao(doc)
        ResultadoLogin.Sucesso(paraUsuario(doc), tipoDe(doc))
    } catch (e: Exception) {
        auth.signOut()
        if (e is FirebaseAuthInvalidCredentialsException || e is FirebaseAuthInvalidUserException) {
            ResultadoLogin.Invalido
        } else {
            ResultadoLogin.Falha(mensagemDeErro(e))
        }
    }

    /** Cadastro pelo app: sempre cria um cliente e já deixa logado. */
    suspend fun cadastrar(nome: String, email: String, telefone: String?, cpf: String?, senha: String): ResultadoCadastro = try {
        val conta = auth.createUserWithEmailAndPassword(email.trim(), senha).await().user!!
        conta.updateProfile(userProfileChangeRequest { displayName = nome }).await()
        val doc = carregarOuCriar(conta, nome, telefone, cpf)
        guardarSessao(doc)
        ResultadoCadastro.Sucesso(paraUsuario(doc))
    } catch (e: FirebaseAuthUserCollisionException) {
        ResultadoCadastro.EmailEmUso
    } catch (e: Exception) {
        ResultadoCadastro.Falha(mensagemDeErro(e))
    }

    /** O Firebase envia de verdade o e-mail com o link para criar uma nova senha. Devolve um erro só se não deu para enviar. */
    suspend fun recuperarSenha(email: String): Int? = try {
        auth.sendPasswordResetEmail(email.trim()).await()
        null
    } catch (e: FirebaseNetworkException) {
        R.string.erro_sem_internet
    } catch (e: FirebaseTooManyRequestsException) {
        R.string.erro_muitas_tentativas
    } catch (e: Exception) {
        null // Não revela se o e-mail tem conta
    }

    fun usuarioLogado(): Usuario? = sessao()?.let(::paraUsuario)

    fun tipoLogado(): TipoAcesso? = sessao()?.let(::tipoDe)

    /** Clientes cadastrados, do mais recente para o mais antigo (painel administrativo). */
    suspend fun clientes(): List<Usuario> = usuarios
        .whereEqualTo("idPerfil", TipoAcesso.CLIENTE.perfil.idPerfil)
        .get().await()
        .documents.mapNotNull { it.paraJson()?.let(::paraUsuario) }
        .sortedByDescending { it.dataCadastro }

    fun sair() {
        auth.signOut()
        prefs.edit { remove(CHAVE_SESSAO) }
    }

    /** A sessão salva só vale se a conta do Firebase ainda estiver logada (o login dura até tocar em Sair). */
    private fun sessao(): JSONObject? {
        val uid = auth.currentUser?.uid ?: return null
        return prefs.getString(CHAVE_SESSAO, null)?.let(::JSONObject)?.takeIf { it.optString("idUsuario") == uid }
    }

    private fun guardarSessao(doc: JSONObject) {
        prefs.edit { putString(CHAVE_SESSAO, doc.toString()) }
    }

    /** Lê o cadastro; contas criadas pelo console do Firebase ainda não têm documento, então ele é criado aqui (igual ao site). */
    private suspend fun carregarOuCriar(conta: FirebaseUser, nome: String?, telefone: String?, cpf: String?): JSONObject {
        val ref = usuarios.document(conta.uid)
        ref.get().await().paraJson()?.let { return it }

        val email = conta.email.orEmpty()
        val data = agora()
        val dados = hashMapOf<String, Any?>(
            "idUsuario" to conta.uid,
            "nome" to (nome ?: conta.displayName ?: email.substringBefore('@')),
            "email" to email,
            "telefone" to telefone,
            "cpf" to cpf,
            "dataNascimento" to null,
            "status" to "ATIVO",
            "emailVerificado" to conta.isEmailVerified,
            "ultimoLogin" to data,
            "dataCadastro" to data,
            "dataAtualizacao" to data,
            "idPerfil" to (if (email.equals(ADMIN_EMAIL, ignoreCase = true)) TipoAcesso.ADMINISTRATIVO else TipoAcesso.CLIENTE).perfil.idPerfil
        )
        ref.set(dados).await()
        return JSONObject(dados as Map<*, *>)
    }

    private fun DocumentSnapshot.paraJson(): JSONObject? = data?.let { JSONObject(it as Map<*, *>) }

    private fun tipoDe(j: JSONObject) = TipoAcesso.doPerfil(j.optLong("idPerfil", TipoAcesso.CLIENTE.perfil.idPerfil))

    private fun agora() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

    private fun mensagemDeErro(e: Exception): Int = when (e) {
        is FirebaseNetworkException -> R.string.erro_sem_internet
        is FirebaseTooManyRequestsException -> R.string.erro_muitas_tentativas
        is FirebaseAuthInvalidCredentialsException -> R.string.erro_email_invalido
        else -> R.string.erro_generico
    }

    private fun paraUsuario(j: JSONObject) = Usuario(
        idUsuario = j.getString("idUsuario"),
        nome = j.getString("nome"),
        email = j.getString("email"),
        telefone = j.textoOuNulo("telefone"),
        cpf = j.textoOuNulo("cpf"),
        dataNascimento = j.textoOuNulo("dataNascimento"),
        status = j.optString("status", "ATIVO"),
        emailVerificado = j.optBoolean("emailVerificado"),
        ultimoLogin = j.textoOuNulo("ultimoLogin"),
        dataCadastro = j.optString("dataCadastro"),
        dataAtualizacao = j.optString("dataAtualizacao")
    )

    private fun JSONObject.textoOuNulo(campo: String): String? =
        if (has(campo) && !isNull(campo)) getString(campo) else null

    companion object {
        /** Conta da equipe (criada no console do Firebase, nunca pelo app). */
        const val ADMIN_EMAIL = "admin@petlar.com"
        private const val COLECAO = "usuarios"
        private const val CHAVE_SESSAO = "usuario"
    }
}
