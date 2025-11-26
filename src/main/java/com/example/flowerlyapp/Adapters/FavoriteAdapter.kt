package com.example.flowerlyapp.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flowerlyapp.DetailsActivity
import com.example.flowerlyapp.data.database.entities.FlowerEntity
import com.example.flowerlyapp.data.utils.ImageResourceMapper
import com.example.flowerlyapp.databinding.FavoriteItemBinding

class FavoriteAdapter(
    private val context: Context,
    private val favoriteFlowers: MutableList<FlowerEntity>,
    private val onRemoveFromFavorites: (String) -> Unit, // flowerId
    private val onAddToCart: (FlowerEntity) -> Unit // flower
) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = FavoriteItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val flower = favoriteFlowers[position]
        holder.bind(flower)
    }

    override fun getItemCount(): Int = favoriteFlowers.size

    fun updateItems(newFlowers: List<FlowerEntity>) {
        favoriteFlowers.clear()
        favoriteFlowers.addAll(newFlowers)
        notifyDataSetChanged()
    }
    
    fun updateFavoriteIcon(flowerId: String, isFavorite: Boolean) {
        val position = favoriteFlowers.indexOfFirst { it.id == flowerId }
        if (position != -1) {
            notifyItemChanged(position)
        }
    }

    inner class FavoriteViewHolder(private val binding: FavoriteItemBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(flower: FlowerEntity) {
            binding.apply {
                // Устанавливаем данные цветка
                flowerName.text = flower.name
                flowerPrice.text = "${flower.price.toInt()} ₽"
                flowerDescription.text = flower.description
                flowerImage.setImageResource(ImageResourceMapper.getImageResource(flower.imageResourceId))
                
                // Обработчики кнопок действий
                favoriteButton.setOnClickListener {
                    // Меняем иконку на незакрашенную перед удалением
                    favoriteButton.setImageResource(com.example.flowerlyapp.R.drawable.favorites_btn)
                    onRemoveFromFavorites(flower.id)
                }
                
                btnAddToCart.setOnClickListener {
                    onAddToCart(flower)
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
