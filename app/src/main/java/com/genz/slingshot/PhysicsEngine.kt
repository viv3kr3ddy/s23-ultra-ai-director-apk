package com.genz.slingshot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class PhysicsEngine {
    val worldWidth = 1600f
    val worldHeight = 900f
    val gravity = 980f

    var currentStageIndex by mutableIntStateOf(0)
    var unlockedStageIndex by mutableIntStateOf(0)
    var score by mutableIntStateOf(0)
    var stageState by mutableStateOf("PLAYING")

    var stageConfig: StageConfig = StageRepository.getStage(0)
    val queue = mutableListOf<ProjectileType>()
    val activeProjectiles = mutableListOf<ActiveProjectile>()
    val blocks = mutableListOf<BlockEntity>()
    val enemies = mutableListOf<EnemyEntity>()
    val particles = mutableListOf<SprayParticle>()
    val callouts = mutableListOf<FloatingCallout>()
    val shockwaves = mutableListOf<ShockwaveRing>()

    var isDragging by mutableStateOf(false)
    var dragX by mutableStateOf(StageRepository.SLING_X)
    var dragY by mutableStateOf(StageRepository.SLING_Y)
    val maxPull = 155f
    val launchPower = 7.4f

    private val protestWords = listOf(
        "RATIO'D!",
        "LEAKED!",
        "FACT CHECKED!",
        "UNSUBSCRIBED!",
        "VIRAL!",
        "404 ETHICS!"
    )

    init {
        loadStage(0)
    }

    fun loadStage(index: Int) {
        currentStageIndex = index.coerceIn(0, 3)
        stageConfig = StageRepository.getStage(currentStageIndex)
        queue.clear()
        queue.addAll(stageConfig.roster)
        activeProjectiles.clear()
        blocks.clear()
        blocks.addAll(stageConfig.blocks.map { it.copy() })
        enemies.clear()
        enemies.addAll(stageConfig.enemies.map { it.copy() })
        particles.clear()
        callouts.clear()
        shockwaves.clear()
        stageState = "PLAYING"
        spawnNextProjectileOnSling()
    }

    private fun spawnNextProjectileOnSling() {
        if (queue.isNotEmpty()) {
            val nextType = queue.removeAt(0)
            activeProjectiles.add(
                ActiveProjectile(
                    x = StageRepository.SLING_X,
                    y = StageRepository.SLING_Y,
                    vx = 0f,
                    vy = 0f,
                    type = nextType,
                    isLaunched = false
                )
            )
            dragX = StageRepository.SLING_X
            dragY = StageRepository.SLING_Y
        }
    }

    fun onPointerDown(wx: Float, wy: Float) {
        if (stageState != "PLAYING") return
        val unlaunched = activeProjectiles.firstOrNull { !it.isLaunched }
        if (unlaunched != null) {
            val dist = hypot(wx - StageRepository.SLING_X, wy - StageRepository.SLING_Y)
            if (dist <= 140f) {
                isDragging = true
                updateDrag(wx, wy)
            }
        } else {
            triggerMidAirAbility()
        }
    }

    fun updateDrag(wx: Float, wy: Float) {
        if (!isDragging) return
        val dx = wx - StageRepository.SLING_X
        val dy = wy - StageRepository.SLING_Y
        val dist = hypot(dx, dy)
        if (dist > maxPull) {
            val angle = atan2(dy, dx)
            dragX = StageRepository.SLING_X + cos(angle) * maxPull
            dragY = StageRepository.SLING_Y + sin(angle) * maxPull
        } else {
            dragX = wx
            dragY = wy
        }
        activeProjectiles.firstOrNull { !it.isLaunched }?.let {
            it.x = dragX
            it.y = dragY
        }
    }

    fun onPointerUp() {
        if (!isDragging) return
        isDragging = false
        val unlaunched = activeProjectiles.firstOrNull { !it.isLaunched } ?: return
        val pullDx = StageRepository.SLING_X - dragX
        val pullDy = StageRepository.SLING_Y - dragY
        if (hypot(pullDx, pullDy) > 22f) {
            unlaunched.vx = pullDx * launchPower
            unlaunched.vy = pullDy * launchPower
            unlaunched.isLaunched = true
        } else {
            unlaunched.x = StageRepository.SLING_X
            unlaunched.y = StageRepository.SLING_Y
        }
    }

    private fun triggerMidAirAbility() {
        val flyer = activeProjectiles.firstOrNull { it.isLaunched && !it.abilityUsed && it.active } ?: return
        flyer.abilityUsed = true

        when (flyer.type) {
            ProjectileType.DOOMSCROLLER -> {
                flyer.vx *= 1.65f
                flyer.vy = -120f
                spawnCallout(flyer.x, flyer.y - 30f, "FLASH BURST!", flyer.type.color)
                spawnSprayBurst(flyer.x, flyer.y, flyer.type.color, 18)
            }
            ProjectileType.VIRAL_THREAD -> {
                spawnCallout(flyer.x, flyer.y - 30f, "3X REPOST!", flyer.type.color)
                val cloneUp = ActiveProjectile(
                    x = flyer.x,
                    y = flyer.y - 18f,
                    vx = flyer.vx * 1.08f,
                    vy = flyer.vy - 210f,
                    type = ProjectileType.VIRAL_THREAD,
                    radius = 15f,
                    abilityUsed = true,
                    isLaunched = true
                )
                val cloneDown = ActiveProjectile(
                    x = flyer.x,
                    y = flyer.y + 18f,
                    vx = flyer.vx * 1.08f,
                    vy = flyer.vy + 210f,
                    type = ProjectileType.VIRAL_THREAD,
                    radius = 15f,
                    abilityUsed = true,
                    isLaunched = true
                )
                activeProjectiles.add(cloneUp)
                activeProjectiles.add(cloneDown)
                spawnSprayBurst(flyer.x, flyer.y, flyer.type.color, 20)
            }
            ProjectileType.BASS_DROP -> {
                triggerBassShockwave(flyer.x, flyer.y)
            }
            ProjectileType.WHISTLEBLOWER -> {
                spawnCallout(flyer.x, flyer.y - 40f, "FOIA DUMP!", flyer.type.color)
                val payload = ActiveProjectile(
                    x = flyer.x,
                    y = flyer.y + 24f,
                    vx = flyer.vx * 0.15f,
                    vy = 820f,
                    type = ProjectileType.BASS_DROP,
                    radius = 24f,
                    abilityUsed = true,
                    isLaunched = true
                )
                flyer.vy = -480f
                flyer.vx *= 0.8f
                activeProjectiles.add(payload)
            }
        }
    }

    private fun triggerBassShockwave(cx: Float, cy: Float) {
        val radius = 240f
        shockwaves.add(ShockwaveRing(cx, cy, 20f, radius, Color(0xFFB8FF01)))
        spawnCallout(cx, cy - 45f, "140dB DROP!", Color(0xFFB8FF01))
        spawnSprayBurst(cx, cy, Color(0xFFB8FF01), 28)

        blocks.forEach { b ->
            val d = hypot(b.x - cx, b.y - cy)
            if (d < radius) {
                val factor = (1f - d / radius)
                val angle = atan2(b.y - cy, b.x - cx)
                b.vx += cos(angle) * 580f * factor
                b.vy += sin(angle) * 580f * factor - 160f
                b.hp -= 125f * factor
            }
        }
        enemies.forEach { e ->
            val d = hypot(e.x - cx, e.y - cy)
            if (d < radius) {
                val factor = (1f - d / radius)
                val angle = atan2(e.y - cy, e.x - cx)
                e.vx += cos(angle) * 620f * factor
                e.vy += sin(angle) * 620f * factor - 200f
                e.hp -= 110f * factor
            }
        }
    }

    fun step(dt: Float) {
        val clampedDt = dt.coerceAtMost(0.033f)

        val toRemoveProj = mutableListOf<ActiveProjectile>()
        for (p in activeProjectiles) {
            if (!p.isLaunched || !p.active) continue
            p.age += clampedDt
            p.vy += gravity * clampedDt
            p.x += p.vx * clampedDt
            p.y += p.vy * clampedDt

            if (p.trail.size > 24) p.trail.removeAt(0)
            p.trail.add(p.x to p.y)

            if (p.y + p.radius >= StageRepository.GROUND_Y) {
                p.y = StageRepository.GROUND_Y - p.radius
                p.vy = -p.vy * 0.42f
                p.vx *= 0.72f
                if (p.type == ProjectileType.BASS_DROP && !p.abilityUsed) {
                    p.abilityUsed = true
                    triggerBassShockwave(p.x, p.y)
                }
            }

            for (b in blocks) {
                if (circleRectOverlap(p.x, p.y, p.radius, b)) {
                    resolveProjectileBlockCollision(p, b)
                }
            }

            for (e in enemies) {
                val dist = hypot(p.x - e.x, p.y - e.y)
                if (dist < p.radius + e.type.radius) {
                    val impactSpeed = hypot(p.vx - e.vx, p.vy - e.vy)
                    e.hp -= impactSpeed * 0.14f * p.type.mass
                    e.vx += p.vx * 0.55f
                    e.vy += p.vy * 0.55f - 90f
                    p.vx *= 0.5f
                    p.vy = -abs(p.vy) * 0.4f
                    spawnSprayBurst(e.x, e.y, p.type.color, 12)
                    if (p.type == ProjectileType.BASS_DROP && !p.abilityUsed) {
                        p.abilityUsed = true
                        triggerBassShockwave(p.x, p.y)
                    }
                }
            }

            val speed = hypot(p.vx, p.vy)
            if (p.age > 4.2f || (p.age > 1.2f && speed < 32f) || p.x > worldWidth + 200f || p.x < -100f) {
                p.active = false
                toRemoveProj.add(p)
            }
        }
        activeProjectiles.removeAll(toRemoveProj)

        updateBlocksAndEnemies(clampedDt)
        updateVisualEffects(clampedDt)

        if (stageState == "PLAYING") {
            if (enemies.isEmpty()) {
                stageState = "WON"
                score += queue.size * 1000
                if (currentStageIndex + 1 > unlockedStageIndex && currentStageIndex < 3) {
                    unlockedStageIndex = currentStageIndex + 1
                }
            } else if (activeProjectiles.isEmpty()) {
                if (queue.isNotEmpty()) {
                    spawnNextProjectileOnSling()
                } else {
                    val moving = blocks.any { hypot(it.vx, it.vy) > 40f } ||
                        enemies.any { hypot(it.vx, it.vy) > 40f }
                    if (!moving) {
                        stageState = "LOST"
                    }
                }
            }
        }
    }

    private fun resolveProjectileBlockCollision(p: ActiveProjectile, b: BlockEntity) {
        val impactSpeed = hypot(p.vx - b.vx, p.vy - b.vy)
        val multiplier = when {
            p.type == ProjectileType.VIRAL_THREAD && b.material == MaterialType.CORP_GLASS -> 2.4f
            p.type == ProjectileType.BASS_DROP && b.material == MaterialType.CONCRETE -> 2.0f
            else -> 1.0f
        }
        val damage = impactSpeed * 0.11f * p.type.mass * multiplier
        b.hp -= damage
        b.vx += p.vx * (0.45f / b.material.density)
        b.vy += p.vy * (0.35f / b.material.density)

        val dx = p.x - b.x
        val dy = p.y - b.y
        if (abs(dx) / (b.width / 2f) > abs(dy) / (b.height / 2f)) {
            p.vx = -p.vx * 0.45f
            p.x += if (dx > 0) 6f else -6f
        } else {
            p.vy = -p.vy * 0.45f
            p.y += if (dy > 0) 6f else -6f
        }

        if (p.type == ProjectileType.BASS_DROP && !p.abilityUsed) {
            p.abilityUsed = true
            triggerBassShockwave(p.x, p.y)
        }
        spawnSprayBurst(p.x, p.y, b.material.borderColor, 8)
    }

    private fun updateBlocksAndEnemies(dt: Float) {
        blocks.sortByDescending { it.y }

        for (i in blocks.indices) {
            val b = blocks[i]
            b.vy += gravity * dt
            b.x += b.vx * dt
            b.y += b.vy * dt
            b.vx *= 0.90f

            if (b.y + b.height / 2f >= StageRepository.GROUND_Y) {
                if (b.vy > 260f) {
                    b.hp -= (b.vy - 260f) * 0.12f
                }
                b.y = StageRepository.GROUND_Y - b.height / 2f
                b.vy = 0f
                b.vx *= 0.7f
            }

            for (j in 0 until i) {
                val lower = blocks[j]
                if (aabbOverlap(b, lower)) {
                    val overlapY = (b.y + b.height / 2f) - (lower.y - lower.height / 2f)
                    val overlapX = min(b.x + b.width / 2f, lower.x + lower.width / 2f) -
                        max(b.x - b.width / 2f, lower.x - lower.width / 2f)
                    if (overlapY > 0f && overlapY < 38f && overlapX > 8f && b.y < lower.y) {
                        val relVy = abs(b.vy - lower.vy)
                        if (relVy > 220f) {
                            b.hp -= (relVy - 220f) * 0.14f
                            lower.hp -= (relVy - 220f) * 0.14f * b.material.density
                        }
                        b.y = lower.y - lower.height / 2f - b.height / 2f
                        b.vy = lower.vy * 0.4f
                        val offset = b.x - lower.x
                        if (abs(offset) > lower.width * 0.42f) {
                            b.vx += offset * 2.2f * dt
                        } else {
                            b.vx *= 0.8f
                        }
                    } else {
                        val push = if (b.x > lower.x) 35f else -35f
                        b.vx += push * dt
                        lower.vx -= push * dt
                    }
                }
            }
        }

        val iterator = blocks.iterator()
        while (iterator.hasNext()) {
            val b = iterator.next()
            if (b.hp <= 0f) {
                score += 350
                spawnSprayBurst(b.x, b.y, b.material.borderColor, 16)
                iterator.remove()
            }
        }

        val enemyIterator = enemies.iterator()
        while (enemyIterator.hasNext()) {
            val e = enemyIterator.next()
            e.vy += gravity * dt
            e.x += e.vx * dt
            e.y += e.vy * dt
            e.vx *= 0.92f

            if (e.y + e.type.radius >= StageRepository.GROUND_Y) {
                if (e.vy > 210f) {
                    e.hp -= (e.vy - 210f) * 0.22f
                }
                e.y = StageRepository.GROUND_Y - e.type.radius
                e.vy = 0f
                e.vx *= 0.72f
            }

            for (b in blocks) {
                if (circleRectOverlap(e.x, e.y, e.type.radius, b)) {
                    val relSpeed = hypot(b.vx - e.vx, b.vy - e.vy)
                    if (relSpeed > 130f) {
                        e.hp -= (relSpeed - 100f) * 0.24f * b.material.density
                    }
                    if (e.y < b.y - b.height / 2f + 14f && e.vy >= 0f) {
                        e.y = b.y - b.height / 2f - e.type.radius
                        e.vy = b.vy
                        e.vx += b.vx * 0.25f
                    } else {
                        val pushDir = if (e.x >= b.x) 1f else -1f
                        e.vx += pushDir * 60f * dt
                    }
                }
            }

            if (e.hp <= 0f) {
                score += e.type.points
                val word = protestWords.random()
                spawnCallout(e.x, e.y - 30f, word, stageConfig.neonAccent)
                spawnSprayBurst(e.x, e.y, stageConfig.neonAccent, 26)
                enemyIterator.remove()
            }
        }
    }

    private fun updateVisualEffects(dt: Float) {
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.vy += 320f * dt
            p.life -= dt * 1.6f
            if (p.life <= 0f) pIter.remove()
        }

        val cIter = callouts.iterator()
        while (cIter.hasNext()) {
            val c = cIter.next()
            c.y -= 48f * dt
            c.life -= dt
            if (c.life <= 0f) cIter.remove()
        }

        val sIter = shockwaves.iterator()
        while (sIter.hasNext()) {
            val s = sIter.next()
            s.radius += 620f * dt
            s.alpha = (1f - s.radius / s.maxRadius).coerceIn(0f, 1f)
            if (s.radius >= s.maxRadius) sIter.remove()
        }
    }

    private fun spawnSprayBurst(cx: Float, cy: Float, color: Color, count: Int) {
        repeat(count) {
            val angle = Random.nextFloat() * 6.283f
            val speed = Random.nextFloat() * 280f + 60f
            particles.add(
                SprayParticle(
                    x = cx,
                    y = cy,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 80f,
                    color = color,
                    size = Random.nextFloat() * 6f + 3f
                )
            )
        }
    }

    private fun spawnCallout(cx: Float, cy: Float, text: String, color: Color) {
        callouts.add(FloatingCallout(cx, cy, text, color))
    }

    private fun circleRectOverlap(cx: Float, cy: Float, cr: Float, b: BlockEntity): Boolean {
        val closestX = cx.coerceIn(b.x - b.width / 2f, b.x + b.width / 2f)
        val closestY = cy.coerceIn(b.y - b.height / 2f, b.y + b.height / 2f)
        return hypot(cx - closestX, cy - closestY) <= cr
    }

    private fun aabbOverlap(a: BlockEntity, b: BlockEntity): Boolean {
        return abs(a.x - b.x) < (a.width + b.width) / 2f &&
            abs(a.y - b.y) < (a.height + b.height) / 2f
    }
}
