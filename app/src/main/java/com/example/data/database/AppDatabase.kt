package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [User::class, Bolao::class, Jogo::class, Palpite::class, BolaoUser::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun bolaoDao(): BolaoDao
    abstract fun bolaoUserDao(): BolaoUserDao
    abstract fun jogoDao(): JogoDao
    abstract fun palpiteDao(): PalpiteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cravacopa_database"
                )
                .addCallback(DatabaseCallback())
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        // Callback to pre-populate database on creation
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database)
                    }
                }
            }

            private suspend fun populateDatabase(db: AppDatabase) {
                val userDao = db.userDao()
                val bolaoDao = db.bolaoDao()
                val bolaoUserDao = db.bolaoUserDao()
                val jogoDao = db.jogoDao()
                val palpiteDao = db.palpiteDao()

                // 1. Insert Core Mock Users
                val userRafael = User(
                    id = "u_rafael",
                    email = "rafael.s@email.com",
                    nomeDeVidente = "Rafael S.",
                    avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&h=150&q=80",
                    pontosTotais = 1280
                )
                val userJuliana = User(
                    id = "u_juliana",
                    email = "juliana.m@email.com",
                    nomeDeVidente = "Juliana M.",
                    avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&h=150&q=80",
                    pontosTotais = 1150
                )
                val userLucas = User(
                    id = "u_lucas",
                    email = "lucas.c@email.com",
                    nomeDeVidente = "O Vidente",
                    avatarUrl = "https://images.unsplash.com/photo-1599566150163-29194dcaad36?auto=format&fit=crop&w=150&h=150&q=80",
                    pontosTotais = 1070
                )
                val userPedro = User(
                    id = "u_pedro",
                    email = "pedro.h@email.com",
                    nomeDeVidente = "Pedro H.",
                    avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&h=150&q=80",
                    pontosTotais = 960
                )
                val userAmanda = User(
                    id = "u_amanda",
                    email = "amanda.r@email.com",
                    nomeDeVidente = "Amanda R.",
                    avatarUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=150&h=150&q=80",
                    pontosTotais = 880
                )
                val userThiago = User(
                    id = "u_thiago",
                    email = "thiago.g@email.com",
                    nomeDeVidente = "Thiago G.",
                    avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&h=150&q=80",
                    pontosTotais = 790
                )
                val userBeatriz = User(
                    id = "u_beatriz",
                    email = "beatriz.l@email.com",
                    nomeDeVidente = "Beatriz L.",
                    avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=150&h=150&q=80",
                    pontosTotais = 650
                )
                val userFelipe = User(
                    id = "u_felipe",
                    email = "felipe.a@email.com",
                    nomeDeVidente = "Felipe A.",
                    avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=150&h=150&q=80",
                    pontosTotais = 610
                )
                val userLucasC = User(
                    id = "u_lucasc",
                    email = "lucas.c.real@email.com",
                    nomeDeVidente = "Lucas C.",
                    avatarUrl = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&w=150&h=150&q=80",
                    pontosTotais = 590
                )

                userDao.insertUser(userRafael)
                userDao.insertUser(userJuliana)
                userDao.insertUser(userLucas)
                userDao.insertUser(userPedro)
                userDao.insertUser(userAmanda)
                userDao.insertUser(userThiago)
                userDao.insertUser(userBeatriz)
                userDao.insertUser(userFelipe)
                userDao.insertUser(userLucasC)

                // 2. Insert Mock Bolões (Sweepstakes)
                val bolaoResenha = Bolao(
                    id = "b_resenha",
                    nome = "Resenha FC",
                    codigoConvite = "RF2026",
                    adminId = "u_lucas",
                    bannerColor = "#059669" // Green accent representation
                )
                val bolaoFamilia = Bolao(
                    id = "b_familia",
                    nome = "Família & Amigos",
                    codigoConvite = "FA850",
                    adminId = "u_juliana",
                    bannerColor = "#B91C1C" // Red accent representation
                )
                val bolaoGalera = Bolao(
                    id = "b_galera",
                    nome = "Galera do Trabalho",
                    codigoConvite = "GT1280",
                    adminId = "u_rafael",
                    bannerColor = "#1D4ED8" // Blue accent representation
                )
                val bolaoBoleiros = Bolao(
                    id = "b_boleiros",
                    nome = "Boleiros de Plantão",
                    codigoConvite = "BP750",
                    adminId = "u_pedro",
                    bannerColor = "#4D7C0F" // Darker Green accent representation
                )

                bolaoDao.insertBolao(bolaoResenha)
                bolaoDao.insertBolao(bolaoFamilia)
                bolaoDao.insertBolao(bolaoGalera)
                bolaoDao.insertBolao(bolaoBoleiros)

                // 3. User association with Bolões (Scores in each Bolão)
                // Resenha FC
                bolaoUserDao.insertBolaoUser(BolaoUser("b_resenha", "u_rafael", 1070))
                bolaoUserDao.insertBolaoUser(BolaoUser("b_resenha", "u_juliana", 920))
                bolaoUserDao.insertBolaoUser(BolaoUser("b_resenha", "u_lucas", 1180)) // Leader
                bolaoUserDao.insertBolaoUser(BolaoUser("b_resenha", "u_pedro", 840))
                bolaoUserDao.insertBolaoUser(BolaoUser("b_resenha", "u_amanda", 750))

                // Família & Amigos
                bolaoUserDao.insertBolaoUser(BolaoUser("b_familia", "u_rafael", 820))
                bolaoUserDao.insertBolaoUser(BolaoUser("b_familia", "u_juliana", 980)) // Leader
                bolaoUserDao.insertBolaoUser(BolaoUser("b_familia", "u_lucas", 850))

                // Galera do Trabalho
                bolaoUserDao.insertBolaoUser(BolaoUser("b_galera", "u_rafael", 1280)) // Leader
                bolaoUserDao.insertBolaoUser(BolaoUser("b_galera", "u_juliana", 1010))
                bolaoUserDao.insertBolaoUser(BolaoUser("b_galera", "u_lucas", 1120))
                bolaoUserDao.insertBolaoUser(BolaoUser("b_galera", "u_pedro", 950))

                // Boleiros de Plantão
                bolaoUserDao.insertBolaoUser(BolaoUser("b_boleiros", "u_pedro", 750)) // Leader
                bolaoUserDao.insertBolaoUser(BolaoUser("b_boleiros", "u_amanda", 610))

                // 4. Matches (using current local time 2026-06-08 as ref)
                // June 12, 2026 20:00 UTC = 1781294400000L
                val jogos = listOf(
                    Jogo(
                        id = "j_1",
                        timeCasa = "EUA",
                        timeFora = "CANADÁ",
                        escudoCasa = "🇺🇸",
                        escudoFora = "🇨🇦",
                        dataHoraStr = "12/06 às 20:00 (MetLife Stadium)",
                        timestamp = 1781294400000L,
                        status = "AGENDADO"
                    ),
                    Jogo(
                        id = "j_2",
                        timeCasa = "MÉXICO",
                        timeFora = "IRÃ",
                        escudoCasa = "🇲🇽",
                        escudoFora = "🇮🇷",
                        dataHoraStr = "13/06 às 16:00 (AT&T Stadium)",
                        timestamp = 1781373600000L,
                        status = "AGENDADO"
                    ),
                    Jogo(
                        id = "j_3",
                        timeCasa = "BRASIL",
                        timeFora = "MARROCOS",
                        escudoCasa = "🇧🇷",
                        escudoFora = "🇲🇦",
                        dataHoraStr = "14/06 às 16:00 (Hard Rock Stadium)",
                        timestamp = 1781452800000L,
                        status = "AGENDADO"
                    ),
                    Jogo(
                        id = "j_4",
                        timeCasa = "ARGENTINA",
                        timeFora = "FRANÇA",
                        escudoCasa = "🇦🇷",
                        escudoFora = "🇫🇷",
                        dataHoraStr = "15/06 - 15:00",
                        timestamp = 1781535600000L,
                        status = "AGENDADO"
                    ),
                    Jogo(
                        id = "j_5",
                        timeCasa = "ALEMANHA",
                        timeFora = "JAPÃO",
                        escudoCasa = "🇩🇪",
                        escudoFora = "🇯🇵",
                        dataHoraStr = "16/06 - 14:00",
                        timestamp = 1781618400000L,
                        status = "AGENDADO"
                    ),
                    Jogo(
                        id = "j_6",
                        timeCasa = "ESPANHA",
                        timeFora = "COSTA RICA",
                        escudoCasa = "🇪🇸",
                        escudoFora = "🇨🇷",
                        dataHoraStr = "17/06 - 13:00",
                        timestamp = 1781701200000L,
                        status = "AGENDADO"
                    )
                )

                jogoDao.insertJogos(jogos)

                // 5. Some historical palpites to match the visual list
                palpiteDao.insertPalpite(Palpite("p_1", "u_lucas", "j_1", "b_resenha", 2, 1, false, 0))
                palpiteDao.insertPalpite(Palpite("p_2", "u_lucas", "j_2", "b_resenha", 1, 0, true, 0))
                palpiteDao.insertPalpite(Palpite("p_3", "u_juliana", "j_1", "b_resenha", 1, 1, false, 0))
            }
        }
    }
}
