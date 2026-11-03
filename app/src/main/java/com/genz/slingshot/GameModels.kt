package com.genz.slingshot

import androidx.compose.ui.graphics.Color

enum class ProjectileType(
    val title: String,
    val abilityName: String,
    val color: Color,
    val radius: Float,
    val mass: Float
) {
    DOOMSCROLLER("THE DOOMSCROLLER", "FLASH-BURST DASH", Color(0xFFFF0055), 22f, 1.4f),
    VIRAL_THREAD("THE VIRAL THREAD", "3X ALGO SPLIT", Color(0xFF00F0FF), 18f, 1.0f),
    BASS_DROP("THE BASS DROP", "140dB SUB SHOCKWAVE", Color(0xFFB8FF01), 26f, 2.2f),
    WHISTLEBLOWER("THE WHISTLEBLOWER", "FOIA DATA DUMP", Color(0xFFFFEA00), 22f, 1.3f)
}

enum class MaterialType(
    val label: String,
    val maxHp: Float,
    val density: Float,
    val restitution: Float,
    val color: Color,
    val borderColor: Color
) {
    RED_TAPE("Red Tape Stack", 70f, 1.0f, 0.25f, Color(0xFFD9C5B2), Color(0xFFFF0055)),
    CORP_GLASS("Corporate Glass", 35f, 0.7f, 0.4f, Color(0x8800F0FF), Color(0xFF00F0FF)),
    CONCRETE("Riot Concrete", 190f, 2.5f, 0.12f, Color(0xFF5A5D64), Color(0xFF9E9E9E))
}

enum class EnemyType(
    val title: String,
    val maxHp: Float,
    val radius: Float,
    val points: Int,
    val primaryColor: Color,
    val accentColor: Color
) {
    JUNIOR_BUREAUCRAT("Jr. Bureaucrat", 45f, 22f, 1500, Color(0xFFCFD8DC), Color(0xFF455A64)),
    SPIN_DOCTOR("Spin Doctor", 85f, 26f, 3000, Color(0xFFFFB300), Color(0xFF1E88E5)),
    CAREER_MINISTER("Career Minister", 145f, 32f, 6000, Color(0xFFFFD700), Color(0xFFD50000))
}

data class ActiveProjectile(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val type: ProjectileType,
    val radius: Float = type.radius,
    var abilityUsed: Boolean = false,
    var isLaunched: Boolean = false,
    var active: Boolean = true,
    var age: Float = 0f,
    val trail: MutableList<Pair<Float, Float>> = mutableListOf()
)

data class BlockEntity(
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val material: MaterialType,
    var hp: Float = material.maxHp
)

data class EnemyEntity(
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val type: EnemyType,
    var hp: Float = type.maxHp
)

data class SprayParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var size: Float,
    var life: Float = 1.0f
)

data class FloatingCallout(
    var x: Float,
    var y: Float,
    val text: String,
    val color: Color,
    var life: Float = 1.2f
)

data class ShockwaveRing(
    val x: Float,
    val y: Float,
    var radius: Float,
    val maxRadius: Float,
    val color: Color,
    var alpha: Float = 1f
)
