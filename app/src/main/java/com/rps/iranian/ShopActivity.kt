package com.rps.iranian

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rps.iranian.audio.AudioManager
import com.rps.iranian.model.HandSkin
import com.rps.iranian.store.GameStore
import com.rps.iranian.ui.SkinAdapter
import java.text.NumberFormat
import java.util.Locale

class ShopActivity : AppCompatActivity() {

    private lateinit var store: GameStore
    private lateinit var audio: AudioManager
    private lateinit var adapter: SkinAdapter
    private lateinit var tvCoins: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        store = GameStore(this)
        audio = (applicationContext as? RPSApplication)?.audioManager ?: AudioManager(this)

        bindViews()
        setupRecyclerView()
    }

    private fun bindViews() {
        tvCoins = findViewById(R.id.tvCoins)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            audio.playClick()
            finish()
        }
        updateCoinsDisplay()
    }

    private fun setupRecyclerView() {
        val recycler = findViewById<RecyclerView>(R.id.recyclerSkins)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = SkinAdapter(
            skins = HandSkin.values().toList(),
            store = store,
            onBuyClick = { skin -> handleBuyClick(skin) },
            onSelectClick = { skin -> handleSelectClick(skin) }
        )
        recycler.adapter = adapter
    }

    private fun handleBuyClick(skin: HandSkin) {
        if (store.isSkinOwned(skin)) return

        if (store.coins < skin.price) {
            audio.playLose()
            Toast.makeText(this, R.string.not_enough, Toast.LENGTH_SHORT).show()
            return
        }

        val success = store.buySkin(skin)
        if (success) {
            audio.playPurchase()
            audio.playCoin()
            Toast.makeText(this, R.string.purchase_success, Toast.LENGTH_SHORT).show()
            // انتخاب خودکار اسکین خریداری‌شده
            store.selectSkin(skin)
            updateCoinsDisplay()
            adapter.notifyDataSetChanged()
        } else {
            audio.playLose()
            Toast.makeText(this, R.string.not_enough, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSelectClick(skin: HandSkin) {
        if (!store.isSkinOwned(skin)) return
        audio.playSelect()
        store.selectSkin(skin)
        adapter.notifyDataSetChanged()
    }

    private fun updateCoinsDisplay() {
        val formatted = NumberFormat.getNumberInstance(Locale("fa", "IR")).format(store.coins)
        tvCoins.text = formatted
    }

    override fun onResume() {
        super.onResume()
        updateCoinsDisplay()
        adapter.notifyDataSetChanged()
    }
}
