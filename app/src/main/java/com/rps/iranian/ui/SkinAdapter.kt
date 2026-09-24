package com.rps.iranian.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rps.iranian.R
import com.rps.iranian.model.HandType
import com.rps.iranian.model.HandSkin
import com.rps.iranian.store.GameStore
import com.rps.iranian.view.HandView
import java.text.NumberFormat
import java.util.Locale

/**
 * آداپتور برای نمایش لیست اسکین‌ها در فروشگاه.
 * هر آیتم شامل پیش‌نمایش دست، نام، قیمت و دکمه خرید/انتخاب است.
 */
class SkinAdapter(
    private val skins: List<HandSkin>,
    private val store: GameStore,
    private val onBuyClick: (HandSkin) -> Unit,
    private val onSelectClick: (HandSkin) -> Unit
) : RecyclerView.Adapter<SkinAdapter.SkinViewHolder>() {

    inner class SkinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val handPreview: HandView = itemView.findViewById(R.id.handPreview)
        val tvSkinName: TextView = itemView.findViewById(R.id.tvSkinName)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val btnAction: Button = itemView.findViewById(R.id.btnAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkinViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_skin, parent, false)
        return SkinViewHolder(view)
    }

    override fun onBindViewHolder(holder: SkinViewHolder, position: Int) {
        val skin = skins[position]
        val context = holder.itemView.context

        // پیش‌نمایش دست با اسکین فعلی
        holder.handPreview.setSkin(skin)
        holder.handPreview.setHandType(HandType.ROCK)
        holder.handPreview.setMirrored(false)

        holder.tvSkinName.text = context.getString(skin.nameResId)

        val isOwned = store.isSkinOwned(skin)
        val isSelected = store.selectedSkinId == skin.id

        when {
            isSelected -> {
                // اسکین فعلا انتخاب شده
                holder.btnAction.text = context.getString(R.string.selected)
                holder.btnAction.setBackgroundResource(R.drawable.btn_accent)
                holder.btnAction.isEnabled = false
                holder.tvPrice.text = "—"
            }
            isOwned -> {
                // خریداری شده، می‌توان انتخاب کرد
                holder.btnAction.text = context.getString(R.string.select)
                holder.btnAction.setBackgroundResource(R.drawable.btn_primary)
                holder.btnAction.isEnabled = true
                holder.tvPrice.text = "خریداری شده"
                holder.btnAction.setOnClickListener { onSelectClick(skin) }
            }
            else -> {
                // خریدنی
                holder.btnAction.text = context.getString(R.string.buy)
                holder.btnAction.setBackgroundResource(R.drawable.btn_primary)
                holder.btnAction.isEnabled = store.coins >= skin.price
                val formatted = NumberFormat.getNumberInstance(Locale("fa", "IR")).format(skin.price)
                holder.tvPrice.text = formatted
                holder.btnAction.setOnClickListener { onBuyClick(skin) }
            }
        }
    }

    override fun getItemCount(): Int = skins.size
}
