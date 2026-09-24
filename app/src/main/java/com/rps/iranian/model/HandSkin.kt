package com.rps.iranian.model

import com.rps.iranian.R

/**
 * اسکین‌های مختلف دست که کاربر می‌تواند در فروشگاه بخرد.
 * هر اسکین شامل رنگ پوست، تتو و افکت خاص است.
 */
enum class HandSkin(
    val id: Int,
    val nameResId: Int,
    val price: Int,
    val baseColor: Int,         // رنگ پوست دست
    val shadowColor: Int,       // سایه‌ها و خطوط
    val nailColor: Int,          // رنگ ناخن‌ها
    val tattooType: TattooType,  // نوع تتو
    val effectType: EffectType   // افکت ویژه
) {
    BASIC(
        id = 0,
        nameResId = R.string.skin_basic,
        price = 0,
        baseColor = 0xFFE8B98A.toInt(),
        shadowColor = 0xFFB88860.toInt(),
        nailColor = 0xFFF5D8B0.toInt(),
        tattooType = TattooType.NONE,
        effectType = EffectType.NONE
    ),
    FLOWER_TATTOO(
        id = 1,
        nameResId = R.string.skin_flower,
        price = 200,
        baseColor = 0xFFE8B98A.toInt(),
        shadowColor = 0xFFB88860.toInt(),
        nailColor = 0xFFFF8FA3.toInt(),
        tattooType = TattooType.FLOWER,
        effectType = EffectType.NONE
    ),
    IRAN_TATTOO(
        id = 2,
        nameResId = R.string.skin_iran,
        price = 500,
        baseColor = 0xFFE8B98A.toInt(),
        shadowColor = 0xFFB88860.toInt(),
        nailColor = 0xFF239F40.toInt(),
        tattooType = TattooType.IRAN_FLAG,
        effectType = EffectType.NONE
    ),
    GOLDEN(
        id = 3,
        nameResId = R.string.skin_golden,
        price = 1000,
        baseColor = 0xFFFFD700.toInt(),
        shadowColor = 0xFFB8860B.toInt(),
        nailColor = 0xFFFFF8DC.toInt(),
        tattooType = TattooType.NONE,
        effectType = EffectType.GOLD_GLOW
    ),
    FIRE(
        id = 4,
        nameResId = R.string.skin_fire,
        price = 800,
        baseColor = 0xFFFF6B35.toInt(),
        shadowColor = 0xFFB22222.toInt(),
        nailColor = 0xFFFFD700.toInt(),
        tattooType = TattooType.NONE,
        effectType = EffectType.FIRE
    ),
    EMERALD(
        id = 5,
        nameResId = R.string.skin_emerald,
        price = 600,
        baseColor = 0xFF50C878.toInt(),
        shadowColor = 0xFF2E7D32.toInt(),
        nailColor = 0xFFE0F2F1.toInt(),
        tattooType = TattooType.NONE,
        effectType = EffectType.EMERALD_GLOW
    ),
    LION_TATTOO(
        id = 6,
        nameResId = R.string.skin_lion,
        price = 700,
        baseColor = 0xFFE8B98A.toInt(),
        shadowColor = 0xFFB88860.toInt(),
        nailColor = 0xFFD4A24C.toInt(),
        tattooType = TattooType.LION,
        effectType = EffectType.NONE
    ),
    ROYAL(
        id = 7,
        nameResId = R.string.skin_royal,
        price = 1000,
        baseColor = 0xFF8E4585.toInt(),
        shadowColor = 0xFF4A148C.toInt(),
        nailColor = 0xFFFFD700.toInt(),
        tattooType = TattooType.CROWN,
        effectType = EffectType.ROYAL_AURA
    );

    companion object {
        fun fromId(id: Int): HandSkin {
            return values().find { it.id == id } ?: BASIC
        }
    }
}

/**
 * انواع تتو که روی دست نقاشی می‌شوند
 */
enum class TattooType {
    NONE,
    FLOWER,        // گل سنتی ایرانی
    IRAN_FLAG,     // پرچم ایران
    LION,          // شیر هخامنشی (نماد ایران باستان)
    CROWN          // تاج پادشاهی
}

/**
 * افکت‌های ویژه روی دست
 */
enum class EffectType {
    NONE,
    GOLD_GLOW,     // هاله طلایی
    FIRE,          // آتش
    EMERALD_GLOW,  // درخشش زمردی
    ROYAL_AURA     // هاله پادشاهی
}
