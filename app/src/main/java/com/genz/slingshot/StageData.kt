package com.genz.slingshot

import androidx.compose.ui.graphics.Color

data class StageConfig(
    val id: Int,
    val city: String,
    val subtitle: String,
    val skyTop: Color,
    val skyBottom: Color,
    val neonAccent: Color,
    val slogan: String,
    val roster: List<ProjectileType>,
    val blocks: List<BlockEntity>,
    val enemies: List<EnemyEntity>
)

object StageRepository {
    const val GROUND_Y = 780f
    const val SLING_X = 250f
    const val SLING_Y = 640f

    fun getStage(index: Int): StageConfig {
        return when (index) {
            0 -> createParisStage()
            1 -> createMadridStage()
            2 -> createBerlinStage()
            else -> createBrusselsStage()
        }
    }

    private fun createParisStage(): StageConfig {
        val baseX = 950f
        val g = GROUND_Y
        return StageConfig(
            id = 0,
            city = "STAGE 1: PARIS",
            subtitle = "Place de la Greve — The Paperwork Barricade",
            skyTop = Color(0xFF12101B),
            skyBottom = Color(0xFF2B192E),
            neonAccent = Color(0xFFFF0055),
            slogan = "49.3 REJECTED // PENSION REFORM BLOCKADE",
            roster = listOf(
                ProjectileType.DOOMSCROLLER,
                ProjectileType.VIRAL_THREAD,
                ProjectileType.DOOMSCROLLER,
                ProjectileType.BASS_DROP
            ),
            blocks = listOf(
                BlockEntity(baseX, g - 60f, 36f, 120f, material = MaterialType.RED_TAPE),
                BlockEntity(baseX + 160f, g - 60f, 36f, 120f, material = MaterialType.RED_TAPE),
                BlockEntity(baseX + 320f, g - 60f, 36f, 120f, material = MaterialType.RED_TAPE),
                BlockEntity(baseX + 80f, g - 135f, 190f, 26f, material = MaterialType.RED_TAPE),
                BlockEntity(baseX + 240f, g - 135f, 190f, 26f, material = MaterialType.RED_TAPE),
                BlockEntity(baseX + 60f, g - 205f, 32f, 110f, material = MaterialType.CORP_GLASS),
                BlockEntity(baseX + 260f, g - 205f, 32f, 110f, material = MaterialType.CORP_GLASS),
                BlockEntity(baseX + 160f, g - 275f, 240f, 26f, material = MaterialType.RED_TAPE)
            ),
            enemies = listOf(
                EnemyEntity(baseX + 80f, g - 25f, type = EnemyType.JUNIOR_BUREAUCRAT),
                EnemyEntity(baseX + 240f, g - 25f, type = EnemyType.JUNIOR_BUREAUCRAT),
                EnemyEntity(baseX + 160f, g - 175f, type = EnemyType.SPIN_DOCTOR)
            )
        )
    }

    private fun createMadridStage(): StageConfig {
        val baseX = 980f
        val g = GROUND_Y
        return StageConfig(
            id = 1,
            city = "STAGE 2: MADRID",
            subtitle = "The Speculator High-Rise — 3,000 EUR/mo Studio",
            skyTop = Color(0xFF0E1726),
            skyBottom = Color(0xFF2E2218),
            neonAccent = Color(0xFF00F0FF),
            slogan = "HOUSING IS NOT A HEDGE FUND",
            roster = listOf(
                ProjectileType.VIRAL_THREAD,
                ProjectileType.VIRAL_THREAD,
                ProjectileType.WHISTLEBLOWER,
                ProjectileType.BASS_DROP
            ),
            blocks = listOf(
                BlockEntity(baseX, g - 65f, 30f, 130f, material = MaterialType.CORP_GLASS),
                BlockEntity(baseX + 140f, g - 65f, 30f, 130f, material = MaterialType.CORP_GLASS),
                BlockEntity(baseX + 280f, g - 65f, 30f, 130f, material = MaterialType.CORP_GLASS),
                BlockEntity(baseX + 140f, g - 145f, 320f, 24f, material = MaterialType.RED_TAPE),
                BlockEntity(baseX + 25f, g - 215f, 28f, 115f, material = MaterialType.CORP_GLASS),
                BlockEntity(baseX + 255f, g - 215f, 28f, 115f, material = MaterialType.CORP_GLASS),
                BlockEntity(baseX + 140f, g - 285f, 270f, 24f, material = MaterialType.RED_TAPE),
                BlockEntity(baseX + 70f, g - 345f, 26f, 95f, material = MaterialType.CORP_GLASS),
                BlockEntity(baseX + 210f, g - 345f, 26f, 95f, material = MaterialType.CORP_GLASS),
                BlockEntity(baseX + 140f, g - 405f, 180f, 24f, material = MaterialType.CONCRETE)
            ),
            enemies = listOf(
                EnemyEntity(baseX + 70f, g - 25f, type = EnemyType.JUNIOR_BUREAUCRAT),
                EnemyEntity(baseX + 210f, g - 25f, type = EnemyType.JUNIOR_BUREAUCRAT),
                EnemyEntity(baseX + 140f, g - 180f, type = EnemyType.SPIN_DOCTOR),
                EnemyEntity(baseX + 140f, g - 320f, type = EnemyType.SPIN_DOCTOR)
            )
        )
    }

    private fun createBerlinStage(): StageConfig {
        val baseX = 920f
        val g = GROUND_Y
        return StageConfig(
            id = 2,
            city = "STAGE 3: BERLIN",
            subtitle = "Techno-Ministerium — The Austerity Bunker",
            skyTop = Color(0xFF0A0F0D),
            skyBottom = Color(0xFF16261E),
            neonAccent = Color(0xFFB8FF01),
            slogan = "BASS AGAINST BUREAUCRACY // SAVE THE CLUBS",
            roster = listOf(
                ProjectileType.BASS_DROP,
                ProjectileType.WHISTLEBLOWER,
                ProjectileType.BASS_DROP,
                ProjectileType.DOOMSCROLLER
            ),
            blocks = listOf(
                BlockEntity(baseX, g - 75f, 48f, 150f, material = MaterialType.CONCRETE),
                BlockEntity(baseX + 120f, g - 60f, 42f, 120f, material = MaterialType.CONCRETE),
                BlockEntity(baseX + 290f, g - 60f, 42f, 120f, material = MaterialType.CONCRETE),
                BlockEntity(baseX + 410f, g - 60f, 42f, 120f, material = MaterialType.RED_TAPE),
                BlockEntity(baseX + 205f, g - 135f, 220f, 34f, material = MaterialType.CONCRETE),
                BlockEntity(baseX + 350f, g - 135f, 150f, 28f, material = MaterialType.RED_TAPE),
                BlockEntity(baseX + 150f, g - 205f, 32f, 105f, material = MaterialType.CORP_GLASS),
                BlockEntity(baseX + 260f, g - 205f, 32f, 105f, material = MaterialType.CORP_GLASS),
                BlockEntity(baseX + 205f, g - 270f, 160f, 28f, material = MaterialType.CONCRETE)
            ),
            enemies = listOf(
                EnemyEntity(baseX + 205f, g - 28f, type = EnemyType.CAREER_MINISTER),
                EnemyEntity(baseX + 350f, g - 25f, type = EnemyType.JUNIOR_BUREAUCRAT),
                EnemyEntity(baseX + 205f, g - 180f, type = EnemyType.SPIN_DOCTOR)
            )
        )
    }

    private fun createBrusselsStage(): StageConfig {
        val baseX = 890f
        val g = GROUND_Y
        return StageConfig(
            id = 3,
            city = "STAGE 4: BRUSSELS",
            subtitle = "The Glass Citadel — Lobbyist Boss Fortress",
            skyTop = Color(0xFF090B14),
            skyBottom = Color(0xFF1A102B),
            neonAccent = Color(0xFFFFEA00),
            slogan = "FINAL STAGE // TRANSPARENCY NOW",
            roster = listOf(
                ProjectileType.VIRAL_THREAD,
                ProjectileType.BASS_DROP,
                ProjectileType.WHISTLEBLOWER,
                ProjectileType.DOOMSCROLLER,
                ProjectileType.BASS_DROP
            ),
            blocks = listOf(
                BlockEntity(baseX, g - 70f, 44f, 140f, material = MaterialType.CONCRETE),
                BlockEntity(baseX + 110f, g - 65f, 36f, 130f, material = MaterialType.CORP_GLASS),
                BlockEntity(baseX + 250f, g - 65f, 44f, 130f, material = MaterialType.CONCRETE),
                BlockEntity(baseX + 390f, g - 65f, 36f, 130f, material = MaterialType.CORP_GLASS),
                BlockEntity(baseX + 180f, g - 145f, 180f, 28f, material = MaterialType.RED_TAPE),
                BlockEntity(baseX + 320f, g - 145f, 180f, 28f, material = MaterialType.RED_TAPE),
                BlockEntity(baseX + 140f, g - 220f, 36f, 120f, material = MaterialType.CONCRETE),
                BlockEntity(baseX + 360f, g - 220f, 36f, 120f, material = MaterialType.CONCRETE),
                BlockEntity(baseX + 250f, g - 295f, 260f, 30f, material = MaterialType.CONCRETE),
                BlockEntity(baseX + 200f, g - 355f, 28f, 90f, material = MaterialType.CORP_GLASS),
                BlockEntity(baseX + 300f, g - 355f, 28f, 90f, material = MaterialType.CORP_GLASS),
                BlockEntity(baseX + 250f, g - 415f, 150f, 26f, material = MaterialType.RED_TAPE)
            ),
            enemies = listOf(
                EnemyEntity(baseX + 180f, g - 25f, type = EnemyType.SPIN_DOCTOR),
                EnemyEntity(baseX + 320f, g - 25f, type = EnemyType.SPIN_DOCTOR),
                EnemyEntity(baseX + 250f, g - 185f, type = EnemyType.CAREER_MINISTER),
                EnemyEntity(baseX + 250f, g - 330f, type = EnemyType.JUNIOR_BUREAUCRAT)
            )
        )
    }
}
