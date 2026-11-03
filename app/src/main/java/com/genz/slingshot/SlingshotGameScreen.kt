package com.genz.slingshot

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.hypot
import kotlin.math.min

@Composable
fun SlingshotGameScreen() {
    val engine = remember { PhysicsEngine() }
    var frameTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        var lastNanos = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.04f)
                lastNanos = now
                engine.step(dt)
                frameTick = now
            }
        }
    }

    val textPaint = remember {
        Paint().apply {
            isAntiAlias = true
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0F))) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val scaleX = size.width / engine.worldWidth
                        val scaleY = size.height / engine.worldHeight
                        val scale = min(scaleX, scaleY)
                        val offsetX = (size.width - engine.worldWidth * scale) / 2f
                        val offsetY = (size.height - engine.worldHeight * scale) / 2f

                        val down = awaitFirstDown(requireUnconsumed = false)
                        val wx = (down.position.x - offsetX) / scale
                        val wy = (down.position.y - offsetY) / scale
                        engine.onPointerDown(wx, wy)

                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            if (change != null && change.pressed) {
                                val curWx = (change.position.x - offsetX) / scale
                                val curWy = (change.position.y - offsetY) / scale
                                engine.updateDrag(curWx, curWy)
                                change.consume()
                            }
                        } while (event.changes.any { it.pressed })

                        engine.onPointerUp()
                    }
                }
        ) {
            val tick = frameTick
            if (tick >= 0L) {
                val scaleX = size.width / engine.worldWidth
                val scaleY = size.height / engine.worldHeight
                val scale = min(scaleX, scaleY)
                val offsetX = (size.width - engine.worldWidth * scale) / 2f
                val offsetY = (size.height - engine.worldHeight * scale) / 2f

                drawContext.canvas.save()
                drawContext.canvas.translate(offsetX, offsetY)
                drawContext.canvas.scale(scale, scale)

                drawUrbanWorld(engine, textPaint)

                drawContext.canvas.restore()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val cities = listOf("1: PARIS", "2: MADRID", "3: BERLIN", "4: BRUSSELS")
                    cities.forEachIndexed { idx, label ->
                        val unlocked = idx <= engine.unlockedStageIndex
                        val selected = idx == engine.currentStageIndex
                        val borderCol = when {
                            selected -> engine.stageConfig.neonAccent
                            unlocked -> Color(0xFF888899)
                            else -> Color(0xFF333344)
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    if (selected) Color(0xCC1E1E2C) else Color(0x99101018),
                                    RoundedCornerShape(6.dp)
                                )
                                .border(1.5.dp, borderCol, RoundedCornerShape(6.dp))
                                .clickable(enabled = unlocked) { engine.loadStage(idx) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (unlocked) label else "$label [LOCKED]",
                                color = if (unlocked) Color.White else Color(0xFF666677),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xCC14141F), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF00F0FF), RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "VIRAL VIEWS: ${engine.score / 100}.${(engine.score % 100) / 10}K",
                            color = Color(0xFF00F0FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xCCFF0055), RoundedCornerShape(6.dp))
                            .clickable { engine.loadStage(engine.currentStageIndex) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "RESET BARRICADE",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val currentFlyer = engine.activeProjectiles.firstOrNull()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${engine.stageConfig.city} — ${engine.stageConfig.subtitle}",
                    color = Color(0xFFCCCCCC),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (currentFlyer != null) {
                    Text(
                        text = "ACTIVE: ${currentFlyer.type.title} [TAP AIR FOR ${currentFlyer.type.abilityName}]",
                        color = currentFlyer.type.color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (engine.stageState != "PLAYING") {
            val isWin = engine.stageState == "WON"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xAA07070C)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(420.dp)
                        .background(Color(0xFF151520), RoundedCornerShape(12.dp))
                        .border(2.dp, engine.stageConfig.neonAccent, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isWin) "ESTABLISHMENT RATIO'D!" else "PROTEST DISPERSED",
                        color = engine.stageConfig.neonAccent,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isWin) {
                            "The barricade collapsed and the livestream went viral across Europe."
                        } else {
                            "The bureaucrats held the line. Adjust your trajectory and tap mid-air to trigger abilities!"
                        },
                        color = Color(0xFFDDDDDD),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF2D2D3F), RoundedCornerShape(8.dp))
                                .clickable { engine.loadStage(engine.currentStageIndex) }
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Text("REPLAY CITY", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        if (isWin && engine.currentStageIndex < 3) {
                            Box(
                                modifier = Modifier
                                    .background(engine.stageConfig.neonAccent, RoundedCornerShape(8.dp))
                                    .clickable { engine.loadStage(engine.currentStageIndex + 1) }
                                    .padding(horizontal = 18.dp, vertical = 10.dp)
                            ) {
                                Text("NEXT CITY ->", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawUrbanWorld(engine: PhysicsEngine, textPaint: Paint) {
    val cfg = engine.stageConfig

    drawRect(
        brush = Brush.verticalGradient(listOf(cfg.skyTop, cfg.skyBottom)),
        size = Size(engine.worldWidth, engine.worldHeight)
    )

    val buildingWidths = listOf(140f, 190f, 160f, 220f, 170f, 210f, 180f, 230f, 190f)
    var bx = 0f
    buildingWidths.forEachIndexed { idx, bw ->
        val bh = 220f + (idx * 67 % 190)
        drawRect(
            color = Color(0xFF0D0E15),
            topLeft = Offset(bx, StageRepository.GROUND_Y - bh),
            size = Size(bw - 6f, bh)
        )
        bx += bw
    }

    textPaint.color = cfg.neonAccent.copy(alpha = 0.28f).toArgb()
    textPaint.textSize = 36f
    drawContext.canvas.nativeCanvas.drawText(
        cfg.slogan,
        engine.worldWidth * 0.52f,
        210f,
        textPaint
    )

    drawRect(
        color = Color(0xFF181920),
        topLeft = Offset(0f, StageRepository.GROUND_Y),
        size = Size(engine.worldWidth, engine.worldHeight - StageRepository.GROUND_Y)
    )
    drawLine(
        color = cfg.neonAccent,
        start = Offset(0f, StageRepository.GROUND_Y),
        end = Offset(engine.worldWidth, StageRepository.GROUND_Y),
        strokeWidth = 4f
    )

    engine.queue.forEachIndexed { i, pType ->
        val qx = 70f + i * 44f
        val qy = StageRepository.GROUND_Y - pType.radius
        drawCircle(color = pType.color, radius = pType.radius * 0.8f, center = Offset(qx, qy))
        drawCircle(
            color = Color.White,
            radius = pType.radius * 0.8f,
            center = Offset(qx, qy),
            style = Stroke(width = 2.5f)
        )
    }

    val sx = StageRepository.SLING_X
    val sy = StageRepository.SLING_Y
    drawLine(
        color = Color(0xFFFF6D00),
        start = Offset(sx, StageRepository.GROUND_Y),
        end = Offset(sx, sy),
        strokeWidth = 14f
    )
    drawLine(
        color = Color(0xFFFFEA00),
        start = Offset(sx, sy),
        end = Offset(sx - 22f, sy - 38f),
        strokeWidth = 10f
    )

    val unlaunched = engine.activeProjectiles.firstOrNull { !it.isLaunched }
    if (engine.isDragging && unlaunched != null) {
        val pullDx = sx - engine.dragX
        val pullDy = sy - engine.dragY
        if (hypot(pullDx, pullDy) > 22f) {
            val vx = pullDx * engine.launchPower
            val vy = pullDy * engine.launchPower
            for (step in 1..22) {
                val t = step * 0.065f
                val px = unlaunched.x + vx * t
                val py = unlaunched.y + vy * t + 0.5f * engine.gravity * t * t
                if (py < StageRepository.GROUND_Y) {
                    drawCircle(
                        color = unlaunched.type.color.copy(alpha = (1f - step / 24f)),
                        radius = 4.5f,
                        center = Offset(px, py)
                    )
                }
            }
        }
    }

    if (unlaunched != null) {
        drawLine(
            color = Color(0xFF00F0FF),
            start = Offset(sx - 18f, sy - 32f),
            end = Offset(unlaunched.x, unlaunched.y),
            strokeWidth = 5f
        )
    }

    for (b in engine.blocks) {
        val topLeft = Offset(b.x - b.width / 2f, b.y - b.height / 2f)
        drawRoundRect(
            color = b.material.color,
            topLeft = topLeft,
            size = Size(b.width, b.height),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = b.material.borderColor,
            topLeft = topLeft,
            size = Size(b.width, b.height),
            cornerRadius = CornerRadius(4f, 4f),
            style = Stroke(width = 2.5f)
        )
        if (b.hp < b.material.maxHp * 0.65f) {
            drawLine(
                color = Color.Black.copy(alpha = 0.6f),
                start = Offset(b.x - b.width * 0.3f, b.y - b.height * 0.3f),
                end = Offset(b.x + b.width * 0.25f, b.y + b.height * 0.3f),
                strokeWidth = 2.5f
            )
        }
    }

    for (e in engine.enemies) {
        drawCircle(
            color = e.type.primaryColor,
            radius = e.type.radius,
            center = Offset(e.x, e.y)
        )
        drawCircle(
            color = e.type.accentColor,
            radius = e.type.radius,
            center = Offset(e.x, e.y),
            style = Stroke(width = 4f)
        )
        drawLine(
            color = Color(0xFFD50000),
            start = Offset(e.x, e.y + 2f),
            end = Offset(e.x, e.y + e.type.radius * 0.75f),
            strokeWidth = 5f
        )
        drawRect(
            color = Color.Black,
            topLeft = Offset(e.x - e.type.radius * 0.55f, e.y - e.type.radius * 0.35f),
            size = Size(e.type.radius * 1.1f, 7f)
        )
        val hpRatio = (e.hp / e.type.maxHp).coerceIn(0f, 1f)
        val barW = e.type.radius * 1.8f
        drawRect(
            color = Color(0xAA000000),
            topLeft = Offset(e.x - barW / 2f, e.y - e.type.radius - 14f),
            size = Size(barW, 6f)
        )
        drawRect(
            color = if (hpRatio > 0.4f) Color(0xFFB8FF01) else Color(0xFFFF0055),
            topLeft = Offset(e.x - barW / 2f, e.y - e.type.radius - 14f),
            size = Size(barW * hpRatio, 6f)
        )
    }

    for (p in engine.activeProjectiles) {
        for (i in p.trail.indices) {
            val pt = p.trail[i]
            drawCircle(
                color = p.type.color.copy(alpha = (i.toFloat() / p.trail.size) * 0.45f),
                radius = 4f,
                center = Offset(pt.first, pt.second)
            )
        }
        drawCircle(
            color = p.type.color,
            radius = p.radius,
            center = Offset(p.x, p.y)
        )
        drawCircle(
            color = Color.White,
            radius = p.radius,
            center = Offset(p.x, p.y),
            style = Stroke(width = 3f)
        )
    }

    if (unlaunched != null) {
        drawLine(
            color = Color(0xFFFF0055),
            start = Offset(sx + 18f, sy - 32f),
            end = Offset(unlaunched.x, unlaunched.y),
            strokeWidth = 5f
        )
    }
    drawLine(
        color = Color(0xFFFFEA00),
        start = Offset(sx, sy),
        end = Offset(sx + 22f, sy - 38f),
        strokeWidth = 10f
    )

    for (s in engine.shockwaves) {
        drawCircle(
            color = s.color.copy(alpha = s.alpha),
            radius = s.radius,
            center = Offset(s.x, s.y),
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f)
            )
        )
    }

    for (pt in engine.particles) {
        drawCircle(
            color = pt.color.copy(alpha = pt.life.coerceIn(0f, 1f)),
            radius = pt.size,
            center = Offset(pt.x, pt.y)
        )
    }

    for (c in engine.callouts) {
        textPaint.color = c.color.toArgb()
        textPaint.textSize = 26f
        drawContext.canvas.nativeCanvas.drawText(c.text, c.x, c.y, textPaint)
    }
}
