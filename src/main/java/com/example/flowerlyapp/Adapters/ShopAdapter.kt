package com.example.flowerlyapp.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flowerlyapp.DetailsActivity
import com.example.flowerlyapp.R
import com.example.flowerlyapp.data.database.entities.FlowerEntity
import com.example.flowerlyapp.data.utils.ImageResourceMapper
import com.example.flowerlyapp.databinding.ShopItemBinding

class ShopAdapter(
    private val context: Context,
    private var flowers: List<FlowerEntity>,
    private val onAddToCart: (FlowerEntity) -> Unit,
    private val onToggleFavorite: (FlowerEntity) -> Unit,
    private val isFavorite: (String) -> Boolean
) : RecyclerView.Adapter<ShopAdapter.ShopViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val binding = ShopItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ShopViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        val flower = flowers[position]
        holder.bind(flower)
    }

    override fun getItemCount(): Int = flowers.size

    fun updateItems(newFlowers: List<FlowerEntity>) {
        flowers = newFlowers
        notifyDataSetChanged()
    }

    inner class ShopViewHolder(private val binding: ShopItemBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(flower: FlowerEntity) {
            binding.apply {
                // Устанавливаем данные цветка
                android.util.Log.d("ShopAdapter", "=== ПРИВЯЗКА ТОВАРА ===")
                android.util.Log.d("ShopAdapter", "flower.id: ${flower.id}")
                android.util.Log.d("ShopAdapter", "flower.name: ${flower.name}")
                android.util.Log.d("ShopAdapter", "flower.imageResourceId: ${flower.imageResourceId}")
                
                flowerName.text = flower.name
                flowerPrice.text = "${flower.price.toInt()} ₽"
                flowerDescription.text = flower.description
                flowerImage.setImageResource(ImageResourceMapper.getImageResource(flower.imageResourceId))
                
                // Обработчики кнопок
                btnAddToCart.setOnClickListener {
                    onAddToCart(flower)
                }
                
                // Устанавливаем статус избранного (если кнопка есть)
                try {
                    android.util.Log.d("ShopAdapter", "Настройка кнопки избранного для ${flower.name}")
                    val isFav = isFavorite(flower.id)
                    android.util.Log.d("ShopAdapter", "isFavorite(${flower.id}) = $isFav")
                    
                    favoriteButton.setImageResource(
                        if (isFav) R.drawable.favorites_full_btn else R.drawable.favorites_btn
                    )
                    
                    favoriteButton.setOnClickListener {
                        android.util.Log.d("ShopAdapter", "=== КНОПКА ИЗБРАННОГО НАЖАТА ===")
                        android.util.Log.d("ShopAdapter", "Товар: ${flower.name} (${flower.id})")
                        onToggleFavorite(flower)
                    }
                    
                    android.util.Log.d("ShopAdapter", "Кнопка избранного настроена успешно")
                } catch (e: Exception) {
                    android.util.Log.e("ShopAdapter", "Ошибка настройки кнопки избранного: ${e.message}", e)
                    // Кнопка избранного не найдена, скрываем её
                    favoriteButton.visibility = View.GONE
                }
                
                // Обработчик клика по карточке
                root.setOnClickListener {
                    val intent = Intent(context, DetailsActivity::class.java).apply {
                        putExtra("flowerId", flower.id)
                        putExtra("flowerName", flower.name)
                        putExtra("flowerDescription", flower.description)
                        putExtra("flowerCompose", flower.composition)
                        putExtra("flowerPrice", flower.price)
                        putExtra("flowerImage", flower.imageResourceId)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }
}
