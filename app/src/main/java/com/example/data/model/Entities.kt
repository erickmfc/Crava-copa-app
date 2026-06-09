package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val email: String,
    val nomeDeVidente: String,
    val avatarUrl: String,
    val pontosTotais: Int = 0
) : Serializable

@Entity(tableName = "boloes")
data class Bolao(
    @PrimaryKey val id: String,
    val nome: String,
    val codigoConvite: String,
    val adminId: String,
    val bannerColor: String = "#162238" // Default background hex for Bolão
) : Serializable

@Entity(tableName = "jogos")
data class Jogo(
    @PrimaryKey val id: String,
    val timeCasa: String,
    val timeFora: String,
    val escudoCasa: String, // Team color hex or flag emoji or image key
    val escudoFora: String,
    val dataHoraStr: String, // Readable date, e.g. "12/06 - 20:00"
    val timestamp: Long,      // Milli timestamp for counting down and sorting
    val placarCasa: Int? = null,
    val placarFora: Int? = null,
    val status: String = "AGENDADO" // "AGENDADO", "EM_ANDAMENTO", "FINALIZADO"
) : Serializable

@Entity(tableName = "palpites")
data class Palpite(
    @PrimaryKey val id: String,
    val userId: String,
    val jogoId: String,
    val bolaoId: String,
    var palpiteCasa: Int,
    var palpiteFora: Int,
    val usouZica: Boolean = false,
    val pontosGanhos: Int = 0
) : Serializable

@Entity(tableName = "bolao_users", primaryKeys = ["bolaoId", "userId"])
data class BolaoUser(
    val bolaoId: String,
    val userId: String,
    val pontosNoBolao: Int = 0
) : Serializable
