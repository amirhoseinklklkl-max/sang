package com.rps.iranian.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.rps.iranian.model.EffectType
import com.rps.iranian.model.HandSkin
import com.rps.iranian.model.HandType
import com.rps.iranian.model.TattooType
import kotlin.math.*

/**
 * نمای سفارشی برای رسم دست با Canvas.
 *
 * ویژگی‌ها:
 * - رسم سه حالت دست: سنگ (مشت)، کاغذ (باز)، قیچی (V)
 * - پشتیبانی از ۸ اسکین متنوع با رنگ‌ها و تتوهای مختلف
 * - انیمیشن مچ زدن (shake) هنگام شمارش
 * - افکت‌های ویژه (هاله طلایی، آتش، زمردی، پادشاهی)
 * - قابلیت آینه‌سازی برای دست حریف
 */
class HandView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var handType: HandType = HandType.ROCK
    private var skin: HandSkin = HandSkin.BASIC
    private var mirrored: Boolean = false

    // انیمیشن
    private var shakeOffset: Float = 0f
    private var bounceScale: Float = 1f
    private var glowPhase: Float = 0f
    private var firePhase: Float = 0f

    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val nailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val tattooPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val effectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 24f
        typeface = Typeface.DEFAULT_BOLD
    }

    private val valueAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 16
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            glowPhase = (System.currentTimeMillis() % 2000) / 2000f
            firePhase = (System.currentTimeMillis() % 600) / 600f
            // نوسان هاله و آتش همیشه فعال باشند (برای دست‌های دارای افکت)
            if (isShaking || skin.effectType != EffectType.NONE) {
                invalidate()
            }
        }
    }

    private var isShaking = false
    private var shakeAnimator: ValueAnimator? = null

    fun setHandType(type: HandType) {
        handType = type
        invalidate()
    }

    fun setSkin(newSkin: HandSkin) {
        skin = newSkin
        invalidate()
    }

    fun setMirrored(mirror: Boolean) {
        mirrored = mirror
        invalidate()
    }

    /**
     * شروع انیمیشن مچ زدن (برای شمارش سنگ کاغذ قیچی).
     */
    fun startShake() {
        isShaking = true
        shakeAnimator?.cancel()
        shakeAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 400
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { anim ->
                val t = anim.animatedValue as Float
                shakeOffset = sin(t * Math.PI * 4).toFloat() * 30f
                bounceScale = 1f + sin(t * Math.PI * 4).absoluteValue * 0.05f
                invalidate()
            }
        }
        shakeAnimator?.start()
    }

    /**
     * توقف انیمیشن مچ زدن.
     */
    fun stopShake() {
        isShaking = false
        shakeOffset = 0f
        bounceScale = 1f
        shakeAnimator?.cancel()
        invalidate()
    }

    /**
     * انیمیشن نمایش دست (pop) هنگام رونمایی از حرکت انتخابی.
     */
    fun revealAnimation() {
        val anim = ValueAnimator.ofFloat(0.5f, 1.2f, 1f).apply {
            duration = 400
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { a ->
                bounceScale = a.animatedValue as Float
                invalidate()
            }
        }
        anim.start()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        valueAnimator.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        valueAnimator.cancel()
        shakeAnimator?.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val size = min(width, height) * 0.85f

        // ذخیره و آماده‌سازی canvas
        canvas.save()

        // انیمیشن نوسان (مچ زدن)
        if (isShaking) {
            canvas.translate(cx + shakeOffset, cy)
            canvas.scale(bounceScale, bounceScale)
        } else {
            canvas.translate(cx, cy)
            canvas.scale(bounceScale, bounceScale)
        }

        // آینه‌سازی برای حریف
        if (mirrored) {
            canvas.scale(-1f, 1f)
        }

        // افکت پس‌زمینه (هاله/درخشش)
        drawEffect(canvas, size)

        // رسم دست بر اساس نوع آن
        when (handType) {
            HandType.ROCK -> drawRock(canvas, size)
            HandType.PAPER -> drawPaper(canvas, size)
            HandType.SCISSORS -> drawScissors(canvas, size)
        }

        canvas.restore()
    }

    // ===== رسم دست سنگ (مشت) =====
    private fun drawRock(canvas: Canvas, size: Float) {
        val half = size * 0.35f
        val palmPath = Path()

        // کف دست (پشت دست) به‌صورت بیضی
        palmPath.addRoundRect(
            RectF(-half, -half * 0.8f, half, half * 0.9f),
            half * 0.4f, half * 0.4f,
            Path.Direction.CW
        )

        basePaint.color = skin.baseColor
        shadowPaint.color = skin.shadowColor
        nailPaint.color = skin.nailColor

        // سایه زیر دست
        canvas.save()
        canvas.translate(8f, 12f)
        canvas.drawPath(palmPath, shadowPaint)
        canvas.restore()

        // بدنه اصلی دست
        canvas.drawPath(palmPath, basePaint)

        // خطوط و سایه‌های داخلی
        outlinePaint.color = darken(skin.baseColor, 0.5f)
        outlinePaint.strokeWidth = 2f
        canvas.drawPath(palmPath, outlinePaint)

        // بند انگشتان (۴ برآمدگی در بالا)
        val knuckleY = -half * 0.6f
        val knuckleRadius = half * 0.18f
        for (i in 0..3) {
            val x = -half * 0.55f + i * half * 0.37f
            canvas.drawCircle(x, knuckleY, knuckleRadius, basePaint)
            // سایه زیر هر بند
            canvas.drawCircle(x + 2f, knuckleY + 3f, knuckleRadius, shadowPaint.apply { alpha = 80 })
            // برجستگی ناخن
            canvas.drawCircle(x, knuckleY - knuckleRadius * 0.5f, knuckleRadius * 0.4f, nailPaint.apply { alpha = 180 })
        }

        // شست در جلوی دست
        val thumbPath = Path()
        thumbPath.addOval(
            RectF(-half * 0.3f, -half * 0.2f, half * 0.7f, half * 0.5f),
            Path.Direction.CW
        )
        canvas.save()
        canvas.rotate(20f)
        canvas.drawPath(thumbPath, basePaint)
        canvas.drawPath(thumbPath, outlinePaint)
        // ناخن شست
        canvas.drawCircle(half * 0.3f, half * 0.0f, knuckleRadius * 0.5f, nailPaint.apply { alpha = 200 })
        canvas.restore()

        // تتو (در صورت وجود)
        drawTattoo(canvas, 0f, half * 0.2f, half * 0.6f)
    }

    // ===== رسم دست کاغذ (باز) =====
    private fun drawPaper(canvas: Canvas, size: Float) {
        val half = size * 0.35f
        basePaint.color = skin.baseColor
        shadowPaint.color = skin.shadowColor
        nailPaint.color = skin.nailColor
        outlinePaint.color = darken(skin.baseColor, 0.5f)

        // کف دست (مستطیل گرد)
        val palmPath = Path()
        palmPath.addRoundRect(
            RectF(-half * 0.85f, -half * 0.2f, half * 0.85f, half * 0.95f),
            half * 0.3f, half * 0.3f,
            Path.Direction.CW
        )

        // سایه
        canvas.save()
        canvas.translate(8f, 12f)
        canvas.drawPath(palmPath, shadowPaint)
        canvas.restore()

        // کف دست
        canvas.drawPath(palmPath, basePaint)
        canvas.drawPath(palmPath, outlinePaint)

        // ۴ انگشت کشیده در بالا
        val fingerWidth = half * 0.32f
        val fingerHeight = half * 1.2f
        val startX = -half * 0.62f
        for (i in 0..3) {
            val x = startX + i * fingerWidth
            // بدنه انگشت
            val fingerPath = Path()
            fingerPath.addRoundRect(
                RectF(x, -fingerHeight, x + fingerWidth * 0.85f, -half * 0.1f),
                fingerWidth * 0.4f, fingerWidth * 0.4f,
                Path.Direction.CW
            )
            canvas.drawPath(fingerPath, basePaint)
            canvas.drawPath(fingerPath, outlinePaint)
            // سایه کناری انگشت
            canvas.drawRect(
                x + fingerWidth * 0.7f, -fingerHeight,
                x + fingerWidth * 0.85f, -half * 0.1f,
                shadowPaint.apply { alpha = 70 }
            )
            // ناخن
            val nailPath = Path()
            nailPath.addRoundRect(
                RectF(x + fingerWidth * 0.1f, -fingerHeight + 4f,
                      x + fingerWidth * 0.75f, -fingerHeight + fingerWidth * 0.5f),
                fingerWidth * 0.2f, fingerWidth * 0.2f,
                Path.Direction.CW
            )
            canvas.drawPath(nailPath, nailPaint.apply { alpha = 220 })
        }

        // شست در کنار
        val thumbPath = Path()
        thumbPath.addOval(
            RectF(-half * 1.05f, half * 0.0f, -half * 0.5f, half * 0.7f),
            Path.Direction.CW
        )
        canvas.save()
        canvas.rotate(-30f)
        canvas.drawPath(thumbPath, basePaint)
        canvas.drawPath(thumbPath, outlinePaint)
        // ناخن شست
        canvas.drawOval(
            RectF(-half * 0.95f, half * 0.1f, -half * 0.7f, half * 0.35f),
            nailPaint.apply { alpha = 220 }
        )
        canvas.restore()

        // تتو
        drawTattoo(canvas, 0f, half * 0.4f, half * 0.6f)
    }

    // ===== رسم دست قیچی (دو انگشت V) =====
    private fun drawScissors(canvas: Canvas, size: Float) {
        val half = size * 0.35f
        basePaint.color = skin.baseColor
        shadowPaint.color = skin.shadowColor
        nailPaint.color = skin.nailColor
        outlinePaint.color = darken(skin.baseColor, 0.5f)

        // کف دست (مشبک، مثل سنگ)
        val palmPath = Path()
        palmPath.addRoundRect(
            RectF(-half, -half * 0.6f, half, half * 1.0f),
            half * 0.4f, half * 0.4f,
            Path.Direction.CW
        )
        canvas.save()
        canvas.translate(8f, 12f)
        canvas.drawPath(palmPath, shadowPaint)
        canvas.restore()
        canvas.drawPath(palmPath, basePaint)
        canvas.drawPath(palmPath, outlinePaint)

        // دو انگشت از بالا (V شکل) - انگشت اشاره و وسطی
        val fingerWidth = half * 0.3f

        // انگشت اشاره (چپ، متمایل به راست)
        canvas.save()
        canvas.rotate(-15f, -half * 0.2f, -half * 0.4f)
        val indexFinger = Path()
        indexFinger.addRoundRect(
            RectF(-half * 0.4f, -half * 1.3f, -half * 0.1f, -half * 0.3f),
            fingerWidth * 0.4f, fingerWidth * 0.4f,
            Path.Direction.CW
        )
        canvas.drawPath(indexFinger, basePaint)
        canvas.drawPath(indexFinger, outlinePaint)
        // ناخن
        canvas.drawRoundRect(
            RectF(-half * 0.38f, -half * 1.25f, -half * 0.12f, -half * 1.0f),
            fingerWidth * 0.2f, fingerWidth * 0.2f,
            nailPaint.apply { alpha = 220 }
        )
        canvas.restore()

        // انگشت وسط (راست، متمایل به چپ)
        canvas.save()
        canvas.rotate(15f, half * 0.2f, -half * 0.4f)
        val middleFinger = Path()
        middleFinger.addRoundRect(
            RectF(half * 0.1f, -half * 1.3f, half * 0.4f, -half * 0.3f),
            fingerWidth * 0.4f, fingerWidth * 0.4f,
            Path.Direction.CW
        )
        canvas.drawPath(middleFinger, basePaint)
        canvas.drawPath(middleFinger, outlinePaint)
        canvas.drawRoundRect(
            RectF(half * 0.12f, -half * 1.25f, half * 0.38f, -half * 1.0f),
            fingerWidth * 0.2f, fingerWidth * 0.2f,
            nailPaint.apply { alpha = 220 }
        )
        canvas.restore()

        // بند انگشت حلقه و کوچک (بسته)
        val knuckleRadius = half * 0.15f
        canvas.drawCircle(half * 0.35f, -half * 0.35f, knuckleRadius, basePaint)
        canvas.drawCircle(half * 0.55f, -half * 0.2f, knuckleRadius * 0.85f, basePaint)

        // شست
        val thumbPath = Path()
        thumbPath.addOval(
            RectF(-half * 0.4f, half * 0.1f, half * 0.5f, half * 0.7f),
            Path.Direction.CW
        )
        canvas.save()
        canvas.rotate(25f)
        canvas.drawPath(thumbPath, basePaint)
        canvas.drawPath(thumbPath, outlinePaint)
        canvas.restore()

        // تتو
        drawTattoo(canvas, 0f, half * 0.4f, half * 0.6f)
    }

    // ===== رسم تتو روی دست =====
    private fun drawTattoo(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        when (skin.tattooType) {
            TattooType.NONE -> {}
            TattooType.FLOWER -> drawFlowerTattoo(canvas, cx, cy, size)
            TattooType.IRAN_FLAG -> drawIranFlagTattoo(canvas, cx, cy, size)
            TattooType.LION -> drawLionTattoo(canvas, cx, cy, size)
            TattooType.CROWN -> drawCrownTattoo(canvas, cx, cy, size)
        }
    }

    // تتو گل سنتی ایرانی
    private fun drawFlowerTattoo(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val petalRadius = size * 0.18f
        val centerRadius = size * 0.08f
        tattooPaint.color = 0xFFD81B60.toInt()
        tattooPaint.alpha = 200

        // ۶ گلبرگ
        for (i in 0..5) {
            val angle = (i * 60.0).toRadians().toFloat()
            val px = cx + cos(angle) * petalRadius
            val py = cy + sin(angle) * petalRadius
            canvas.drawCircle(px, py, petalRadius * 0.7f, tattooPaint)
        }
        // مرکز گل
        tattooPaint.color = 0xFFFFD700.toInt()
        canvas.drawCircle(cx, cy, centerRadius, tattooPaint)
    }

    // پرچم ایران
    private fun drawIranFlagTattoo(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val w = size * 0.7f
        val h = size * 0.4f
        val left = cx - w / 2
        val top = cy - h / 2

        // نوار سبز (بالا)
        tattooPaint.color = 0xFF239F40.toInt()
        canvas.drawRect(left, top, left + w, top + h / 3, tattooPaint)
        // نوار سفید (وسط)
        tattooPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawRect(left, top + h / 3, left + w, top + 2 * h / 3, tattooPaint)
        // نوار قرمز (پایین)
        tattooPaint.color = 0xFFDA0000.toInt()
        canvas.drawRect(left, top + 2 * h / 3, left + w, top + h, tattooPaint)

        // نشان خدا در وسط
        tattooPaint.color = 0xFFDA0000.toInt()
        val symbolPath = Path()
        symbolPath.moveTo(cx, top + h / 3 + 4f)
        symbolPath.cubicTo(
            cx - 8f, top + h / 2 - 4f,
            cx - 4f, top + 2 * h / 3 - 4f,
            cx, top + 2 * h / 3 - 2f
        )
        symbolPath.cubicTo(
            cx + 4f, top + 2 * h / 3 - 4f,
            cx + 8f, top + h / 2 - 4f,
            cx, top + h / 3 + 4f
        )
        canvas.drawPath(symbolPath, tattooPaint)
    }

    // شیر هخامنشی
    private fun drawLionTattoo(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        tattooPaint.color = 0xFFD4A24C.toInt()
        tattooPaint.alpha = 220

        // بدنه شیر (ساده شده)
        val lionPath = Path()
        // سر
        canvas.drawCircle(cx, cy - size * 0.15f, size * 0.15f, tattooPaint)
        // یال (طره‌های دور سر)
        for (i in 0..7) {
            val angle = (Math.PI / 2 + i * Math.PI / 8 - Math.PI / 4).toFloat()
            val px = cx + cos(angle) * size * 0.2f
            val py = (cy - size * 0.15f) + sin(angle) * size * 0.2f
            canvas.drawCircle(px, py, size * 0.04f, tattooPaint)
        }
        // بدنه
        val bodyPath = Path()
        bodyPath.addOval(
            RectF(
                cx - size * 0.2f, cy,
                cx + size * 0.2f, cy + size * 0.3f
            ),
            Path.Direction.CW
        )
        canvas.drawPath(bodyPath, tattooPaint)
        // دم (حلقه)
        tattooPaint.style = Paint.Style.STROKE
        tattooPaint.strokeWidth = 4f
        canvas.drawCircle(cx + size * 0.25f, cy + size * 0.2f, size * 0.08f, tattooPaint)
        tattooPaint.style = Paint.Style.FILL
    }

    // تاج پادشاهی
    private fun drawCrownTattoo(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        tattooPaint.color = 0xFFFFD700.toInt()
        tattooPaint.alpha = 240

        val crownPath = Path()
        val w = size * 0.5f
        val h = size * 0.3f

        // قاب تاج
        crownPath.moveTo(cx - w / 2, cy + h / 2)
        crownPath.lineTo(cx - w / 2, cy)
        crownPath.lineTo(cx - w / 3, cy - h / 3)
        crownPath.lineTo(cx - w / 6, cy)
        crownPath.lineTo(cx, cy - h / 2)
        crownPath.lineTo(cx + w / 6, cy)
        crownPath.lineTo(cx + w / 3, cy - h / 3)
        crownPath.lineTo(cx + w / 2, cy)
        crownPath.lineTo(cx + w / 2, cy + h / 2)
        crownPath.close()

        canvas.drawPath(crownPath, tattooPaint)

        // جواهر در وسط
        tattooPaint.color = 0xFFFF1744.toInt()
        canvas.drawCircle(cx, cy + h / 6, size * 0.05f, tattooPaint)
    }

    // ===== رسم افکت ویژه =====
    private fun drawEffect(canvas: Canvas, size: Float) {
        when (skin.effectType) {
            EffectType.NONE -> {}
            EffectType.GOLD_GLOW -> drawGlow(canvas, size, 0xFFFFD700.toInt(), 0.6f)
            EffectType.EMERALD_GLOW -> drawGlow(canvas, size, 0xFF50C878.toInt(), 0.6f)
            EffectType.ROYAL_AURA -> drawGlow(canvas, size, 0xFF8E4585.toInt(), 0.7f)
            EffectType.FIRE -> drawFireEffect(canvas, size)
        }
    }

    private fun drawGlow(canvas: Canvas, size: Float, color: Int, intensity: Float) {
        val pulseScale = 1f + 0.1f * sin(glowPhase * Math.PI * 2).toFloat()
        val radius = size * 0.6f * pulseScale
        val shader = RadialGradient(
            0f, 0f, radius,
            intArrayOf(color, Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        effectPaint.shader = shader
        effectPaint.alpha = (255 * intensity).toInt()
        canvas.drawCircle(0f, 0f, radius, effectPaint)
        effectPaint.shader = null
        effectPaint.alpha = 255
    }

    private fun drawFireEffect(canvas: Canvas, size: Float) {
        val fireBaseY = size * 0.35f
        val fireHeight = size * 0.4f

        // چند شعله نوسانی
        for (i in 0..4) {
            val x = (i - 2) * size * 0.15f
            val phase = (firePhase + i * 0.2f) % 1f
            val height = fireHeight * (0.7f + 0.3f * sin(phase * Math.PI * 2).toFloat())
            val width = size * 0.1f

            // گرادیان آتش
            val shader = LinearGradient(
                x, fireBaseY,
                x, fireBaseY - height,
                intArrayOf(0xFFFF4500.toInt(), 0xFFFFD700.toInt(), Color.TRANSPARENT),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            effectPaint.shader = shader

            val firePath = Path()
            firePath.moveTo(x - width, fireBaseY)
            firePath.cubicTo(
                x - width, fireBaseY - height * 0.5f,
                x - width * 0.5f, fireBaseY - height * 0.8f,
                x, fireBaseY - height
            )
            firePath.cubicTo(
                x + width * 0.5f, fireBaseY - height * 0.8f,
                x + width, fireBaseY - height * 0.5f,
                x + width, fireBaseY
            )
            firePath.close()
            canvas.drawPath(firePath, effectPaint)
        }
        effectPaint.shader = null
    }

    private fun darken(color: Int, factor: Float): Int {
        val r = (Color.red(color) * factor).toInt()
        val g = (Color.green(color) * factor).toInt()
        val b = (Color.blue(color) * factor).toInt()
        return Color.rgb(r, g, b)
    }

    private fun Double.toRadians(): Double = this * Math.PI / 180.0
}
