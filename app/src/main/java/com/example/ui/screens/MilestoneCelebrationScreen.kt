package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.R
import com.example.core.theme.AppColors
import com.example.core.theme.AppTextStyles
import com.example.core.theme.LocalAccentColor
import com.example.core.utils.HapticService
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.util.Random

// Confetti particle representation for Change 2
private class CelebrationParticle(
    var x: Float,
    var y: Float,
    val color: Color,
    val radius: Float,
    val speedY: Float,
    val driftX: Float,
    val rotationSpeed: Float,
    var rotation: Float = 0f,
    var alpha: Float = 1.0f
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MilestoneCelebrationScreen(
    milestone: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val accentColor = LocalAccentColor.current
    
    // 1. Dynamic configs based on milestone days
    val details = remember(milestone) {
        when (milestone) {
            3 -> MilestoneInfo("SPARK UNLOCKED 🔥", "3", "3 Day Streak Achieved", "🔥", Color(0xFFFF5722))
            7 -> MilestoneInfo("WARRIOR UNLOCKED ⚔️", "7", "7 Day Streak Achieved", "⚔️", Color(0xFFFF3D71))
            14 -> MilestoneInfo("CHAMPION UNLOCKED 🏆", "14", "14 Day Streak Achieved", "🏆", Color(0xFF5C6BC0))
            30 -> MilestoneInfo("LEGEND UNLOCKED 👑", "30", "30 Day Streak Achieved", "👑", Color(0xFFF9A825))
            100 -> MilestoneInfo("IMMORTAL UNLOCKED 💎", "100", "100 Day Streak Achieved", "💎", Color(0xFF00D4AA))
            else -> MilestoneInfo("MILESTONE ACHIEVED! 🔥", "$milestone", "$milestone Day Streak Achieved", "🔥", accentColor)
        }
    }
    
    // Progressive particle counts, colors, and durations
    val particleCount = when (milestone) {
        30, 100 -> 140
        14 -> 110
        else -> 85
    }
    val celebrationDuration = when (milestone) {
        30, 100 -> 4000L
        else -> 2500L
    }
    
    // 2. Confetti Particle State
    val confettiStartTime = remember { System.currentTimeMillis() }
    var confettiTick by remember { mutableStateOf(0) }
    var isConfettiActive by remember { mutableStateOf(true) }
    
    val particles = remember(particleCount) {
        val random = Random()
        val baseColors = listOf(
            accentColor, Color.White, Color(0xFFF9A825), // Gold
            Color(0xFFFF5722), Color(0xFF5C6BC0), Color(0xFF2E7D52), Color(0xFF00D4AA)
        )
        List(particleCount) {
            CelebrationParticle(
                x = random.nextFloat() * 1080f, // Initial layout placeholder (resets to canvas width at start)
                y = -random.nextFloat() * 400f - 20f,
                color = baseColors[random.nextInt(baseColors.size)],
                radius = random.nextFloat() * 10f + 5f,
                speedY = random.nextFloat() * 12f + 6f,
                driftX = (random.nextFloat() - 0.5f) * 4f,
                rotationSpeed = (random.nextFloat() - 0.5f) * 15f
            )
        }
    }
    
    // 3. UI Animations
    var startFlameAnim by remember { mutableStateOf(false) }
    var showBadge by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }
    var showShareSection by remember { mutableStateOf(false) }
    
    val flameScale by animateFloatAsState(
        targetValue = if (startFlameAnim) 1.0f else 0.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    val badgeScale by animateFloatAsState(
        targetValue = if (showBadge) 1.0f else 0.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    // Confetti update loop
    LaunchedEffect(isConfettiActive) {
        if (isConfettiActive) {
            while (System.currentTimeMillis() - confettiStartTime < celebrationDuration) {
                withFrameMillis {
                    confettiTick++
                }
            }
            isConfettiActive = false
        }
    }
    
    // Animation triggers, sound, and haptics
    LaunchedEffect(Unit) {
        // Trigger sound (milestone_cheer fallback to task_complete)
        try {
            val resId = context.resources.getIdentifier("milestone_cheer", "raw", context.packageName)
            val finalResId = if (resId != 0) resId else R.raw.task_complete
            val mediaPlayer = android.media.MediaPlayer.create(context, finalResId)
            mediaPlayer.setVolume(1.0f, 1.0f)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { mp -> mp.release() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Haptics: Celebratory triple pulse
        HapticService.celebrateMilestone()
        
        //Confetti Tick Haptics: 3 ticks every 600ms
        repeat(3) {
            delay(600)
            HapticService.tickHaptic()
        }
    }
    
    LaunchedEffect(Unit) {
        delay(100)
        startFlameAnim = true
        delay(250)
        showBadge = true
        delay(200)
        showTitle = true
        delay(150)
        showSubtitle = true
        delay(200) // Total delay 900ms (satisfies 800ms spec)
        showShareSection = true
    }
    
    Dialog(
        onDismissRequest = { /* Force continue button click */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
        ) {
            // Confetti canvas drawing
            if (isConfettiActive) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val d = confettiTick // Reference tick to trigger recomposition
                    val w = size.width
                    val h = size.height
                    val elapsed = System.currentTimeMillis() - confettiStartTime
                    val alphaProgress = (1.0f - (elapsed.toFloat() / celebrationDuration)).coerceIn(0f, 1f)
                    
                    particles.forEach { p ->
                        // Initialize position constraints on canvas layout load
                        if (p.x > w) {
                            p.x = Random().nextFloat() * w
                        }
                        
                        p.y += p.speedY
                        p.x += p.driftX
                        p.rotation += p.rotationSpeed
                        
                        // Loop confetti to top during active animation
                        if (p.y > h) {
                            p.y = -30f
                            p.x = Random().nextFloat() * w
                        }
                        
                        // Draw falling confetti strip
                        p.alpha = alphaProgress
                        val radRad = Math.toRadians(p.rotation.toDouble())
                        val cos = Math.cos(radRad).toFloat()
                        val sin = Math.sin(radRad).toFloat()
                        
                        val halfWidth = p.radius
                        val halfHeight = p.radius * 2f
                        
                        val p1 = Offset(p.x - halfWidth * cos - halfHeight * sin, p.y - halfWidth * sin + halfHeight * cos)
                        val p2 = Offset(p.x + halfWidth * cos - halfHeight * sin, p.y + halfWidth * sin + halfHeight * cos)
                        val p3 = Offset(p.x + halfWidth * cos + halfHeight * sin, p.y + halfWidth * sin - halfHeight * cos)
                        val p4 = Offset(p.x - halfWidth * cos + halfHeight * sin, p.y - halfWidth * sin - halfHeight * cos)
                        
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(p1.x, p1.y)
                            lineTo(p2.x, p2.y)
                            lineTo(p3.x, p3.y)
                            lineTo(p4.x, p4.y)
                            close()
                        }
                        
                        drawPath(path, color = p.color.copy(alpha = p.alpha))
                    }
                }
            }
            
            // Scrollable Content overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Centered flame and badge icons
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer(scaleX = flameScale, scaleY = flameScale)
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(details.tierColor.copy(alpha = 0.15f))
                            .border(1.5.dp, details.tierColor.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = details.tierColor,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Bouncing Tier badge
                    Box(
                        modifier = Modifier
                            .graphicsLayer(scaleX = badgeScale, scaleY = badgeScale)
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = details.emoji,
                            fontSize = 38.sp
                        )
                    }
                }
                
                // Titles and milestones
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 24.dp)
                ) {
                    AnimatedVisibility(
                        visible = showTitle,
                        enter = slideInVertically(initialOffsetY = { 80 }) + fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = details.title,
                            style = AppTextStyles.screenTitle(Color.White).copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    AnimatedVisibility(
                        visible = showSubtitle,
                        enter = fadeIn(animationSpec = tween(400)),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = details.subtitle,
                            style = AppTextStyles.headingMedium.copy(
                                color = details.tierColor,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Share options
                AnimatedVisibility(
                    visible = showShareSection,
                    enter = fadeIn(animationSpec = tween(500)),
                    exit = fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "SHARE YOUR MILESTONE",
                            style = AppTextStyles.caption.copy(
                                color = Color.White.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Row/Grid of share actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SharePillButton(
                                text = "📸 Story",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val uri = generateShareableCard(context, milestone, details.title, details.streak)
                                    if (uri != null) {
                                        shareToInstagramStory(context, uri)
                                    } else {
                                        Toast.makeText(context, "Error generating card", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                            SharePillButton(
                                text = "⬜ Post",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val uri = generateShareableCard(context, milestone, details.title, details.streak)
                                    if (uri != null) {
                                        shareToInstagramPost(context, uri)
                                    } else {
                                        Toast.makeText(context, "Error generating card", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SharePillButton(
                                text = "💬 WhatsApp",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    shareToWhatsApp(context, details.title, details.streak)
                                }
                            )
                            SharePillButton(
                                text = "↗️ More",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val uri = generateShareableCard(context, milestone, details.title, details.streak)
                                    if (uri != null) {
                                        shareGeneric(context, uri, details.title, details.streak)
                                    } else {
                                        Toast.makeText(context, "Error generating card", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
                
                // Bottom continue and tagline
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp)
                ) {
                    Text(
                        text = "Ignited with ❤️ in India 🇮🇳",
                        style = AppTextStyles.caption.copy(
                            color = AppColors.textHint,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            com.example.core.utils.SoundService.playTap()
                            HapticService.confirm()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = details.tierColor, contentColor = Color.Black),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text(
                            text = "Continue 🔥",
                            style = AppTextStyles.actionButton.copy(color = Color.Black, fontSize = 16.sp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SharePillButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .clickable {
                HapticService.selectionClick()
                onClick()
            }
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            style = AppTextStyles.caption.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp)
        )
    }
}

// 4. Milestone data model
private data class MilestoneInfo(
    val title: String,
    val streak: String,
    val subtitle: String,
    val emoji: String,
    val tierColor: Color
)

// 5. Card generator matching Change 2 spec
private fun generateShareableCard(context: Context, milestone: Int, title: String, streakCount: String): Uri? {
    try {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        // Gradient colors per milestone
        val colors = when (milestone) {
            3 -> intArrayOf(0xFF1E293B.toInt(), 0xFF0F172A.toInt()) // Dark slate
            7 -> intArrayOf(0xFF4A0E17.toInt(), 0xFF1F0307.toInt()) // Deep crimson
            14 -> intArrayOf(0xFF0F3057.toInt(), 0xFF00587A.toInt()) // Deep blue
            30 -> intArrayOf(0xFF2C0B3E.toInt(), 0xFF140220.toInt()) // Dark purple
            100 -> intArrayOf(0xFF0F1A1C.toInt(), 0xFF06353F.toInt()) // Deep teal
            else -> intArrayOf(0xFF0F172A.toInt(), 0xFF000000.toInt())
        }
        
        // Background
        val gradient = LinearGradient(0f, 0f, 0f, height.toFloat(), colors[0], colors[1], Shader.TileMode.CLAMP)
        val paint = Paint().apply { shader = gradient }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        // Tier specific accent color
        val tierColor = when (milestone) {
            3 -> 0xFFFF5722.toInt()
            7 -> 0xFFFF3D71.toInt()
            14 -> 0xFF5C6BC0.toInt()
            30 -> 0xFFF9A825.toInt()
            100 -> 0xFF00D4AA.toInt()
            else -> 0xFFFF5722.toInt()
        }
        
        // Glowing Flame Circle
        val glowPaint = Paint().apply {
            color = tierColor
            alpha = 25
            isAntiAlias = true
        }
        canvas.drawCircle(width / 2f, height * 0.35f, 220f, glowPaint)
        canvas.drawCircle(width / 2f, height * 0.35f, 150f, glowPaint)
        
        // Stylized Flame Path
        val flamePaint = Paint().apply {
            color = tierColor
            isAntiAlias = true
        }
        val flamePath = android.graphics.Path().apply {
            moveTo(width / 2f, height * 0.35f - 110f)
            cubicTo(
                width / 2f - 110f, height * 0.35f - 20f,
                width / 2f - 120f, height * 0.35f + 80f,
                width / 2f, height * 0.35f + 110f
            )
            cubicTo(
                width / 2f + 110f, height * 0.35f + 80f,
                width / 2f + 120f, height * 0.35f - 20f,
                width / 2f, height * 0.35f - 110f
            )
        }
        canvas.drawPath(flamePath, flamePaint)
        
        // Inner Flame
        val innerFlamePaint = Paint().apply {
            color = android.graphics.Color.WHITE
            alpha = 220
            isAntiAlias = true
        }
        val innerFlamePath = android.graphics.Path().apply {
            moveTo(width / 2f, height * 0.35f - 60f)
            cubicTo(
                width / 2f - 60f, height * 0.35f - 10f,
                width / 2f - 70f, height * 0.35f + 40f,
                width / 2f, height * 0.35f + 60f
            )
            cubicTo(
                width / 2f + 60f, height * 0.35f + 40f,
                width / 2f + 70f, height * 0.35f - 10f,
                width / 2f, height * 0.35f - 60f
            )
        }
        canvas.drawPath(innerFlamePath, innerFlamePaint)
        
        // Milestone title
        val titlePaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 68f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(title.uppercase(), width / 2f, height * 0.58f, titlePaint)
        
        // Streak counter
        val countPaint = Paint().apply {
            color = tierColor
            textSize = 230f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(streakCount, width / 2f, height * 0.73f, countPaint)
        
        // Label
        val labelPaint = Paint().apply {
            color = 0xFF8892B0.toInt()
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("DAY STREAK ACHIEVED", width / 2f, height * 0.77f, labelPaint)
        
        // Tagline & watermarks
        val taglinePaint = Paint().apply {
            color = 0xFF4A5480.toInt()
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Ignited with ❤️ in India 🇮🇳", width / 2f, height * 0.88f, taglinePaint)
        
        val brandPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            alpha = 100
            textSize = 46f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("STREAKLY", width / 2f, height * 0.93f, brandPaint)
        
        // Save file to cache folder
        val cachePath = File(context.cacheDir, "shared_images")
        cachePath.mkdirs()
        val file = File(cachePath, "milestone_celebration.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()
        
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

// 6. Share Intents
private fun shareToInstagramStory(context: Context, uri: Uri) {
    try {
        val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
            setDataAndType(uri, "image/*")
            putExtra("interactive_asset_uri", uri)
            putExtra("top_background_color", "#080B14")
            putExtra("bottom_background_color", "#0F1320")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Instagram app not found. Opening general share...", Toast.LENGTH_SHORT).show()
        shareGeneric(context, uri, "Milestone", "Streak")
    }
}

private fun shareToInstagramPost(context: Context, uri: Uri) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            setPackage("com.instagram.android")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Instagram app not found. Opening general share...", Toast.LENGTH_SHORT).show()
        shareGeneric(context, uri, "Milestone", "Streak")
    }
}

private fun shareToWhatsApp(context: Context, title: String, streakCount: String) {
    try {
        val text = "I just unlocked $title with a $streakCount day streak on Streakly! 🔥"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage("com.whatsapp")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp app not found.", Toast.LENGTH_SHORT).show()
    }
}

private fun shareGeneric(context: Context, uri: Uri, title: String, streakCount: String) {
    try {
        val text = "I just unlocked $title with a $streakCount day streak on Streakly! 🔥"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, text)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(Intent.createChooser(intent, "Share Milestone"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
