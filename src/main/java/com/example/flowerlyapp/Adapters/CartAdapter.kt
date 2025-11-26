package com.example.flowerlyapp.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flowerlyapp.data.database.dao.CartItemWithFlowerInfo
import com.example.flowerlyapp.data.utils.ImageResourceMapper
import com.example.flowerlyapp.databinding.CartItemBinding

class CartAdapter(
    private val context: Context,
    private var cartItems: List<CartItemWithFlowerInfo>,
    private val onQuantityChanged: (String, Int) -> Unit, // flowerId, newQuantity
    private val onRemoveItem: (String) -> Unit // flowerId
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]
        holder.bind(cartItem)
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateItems(newItems: List<CartItemWithFlowerInfo>) {
        android.util.Log.d("CartAdapter", "=== ОБНОВЛЕНИЕ ТОВАРОВ ===")
        android.util.Log.d("CartAdapter", "Новое количество товаров: ${newItems.size}")
        newItems.forEach { item ->
            android.util.Log.d("CartAdapter", "Товар: ${item.flowerName}, количество: ${item.quantity}")
        }
        cartItems = newItems
        notifyDataSetChanged()
    }

    inner class CartViewHolder(private val binding: CartItemBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(cartItem: CartItemWithFlowerInfo) {
            binding.apply {
                android.util.Log.d("CartAdapter", "=== ПРИВЯЗКА ТОВАРА ===")
                android.util.Log.d("CartAdapter", "flowerName: ${cartItem.flowerName}")
                android.util.Log.d("CartAdapter", "quantity: ${cartItem.quantity}")
                android.util.Log.d("CartAdapter", "unitPrice: ${cartItem.unitPrice}")
                android.util.Log.d("CartAdapter", "totalPrice: ${cartItem.totalPrice}")
                
                // Устанавливаем данные цветка
                flowerName.text = cartItem.flowerName
                flowerPrice.text = "${cartItem.unitPrice.toInt()} ₽"
                flowerImage.setImageResource(ImageResourceMapper.getImageResource(cartItem.imageResourceId))
                
                // Устанавливаем количество
                quantityText.text = cartItem.quantity.toString()
                android.util.Log.d("CartAdapter", "Установлено количество: ${cartItem.quantity}")
                
                // Устанавливаем общую стоимость
                totalPrice.text = "${cartItem.totalPrice.toInt()} ₽"
                
                // Обработчики кнопок
                btnIncrease.setOnClickListener {
                    val newQuantity = cartItem.quantity + 1
                    android.util.Log.d("CartAdapter", "Увеличиваем количество: ${cartItem.quantity} -> $newQuantity")
                    // Немедленно обновляем UI
                    quantityText.text = newQuantity.toString()
                    val newTotalPrice = newQuantity * cartItem.unitPrice
                    totalPrice.text = "${newTotalPrice.toInt()} ₽"
                    onQuantityChanged(cartItem.flowerId, newQuantity)
                }
                
                btnDecrease.setOnClickListener {
                    val newQuantity = cartItem.quantity - 1
                    android.util.Log.d("CartAdapter", "Уменьшаем количество: ${cartItem.quantity} -> $newQuantity")
                    if (newQuantity > 0) {
                        // Немедленно обновляем UI
                        quantityText.text = newQuantity.toString()
                        val newTotalPrice = newQuantity * cartItem.unitPrice
                        totalPrice.text = "${newTotalPrice.toInt()} ₽"
                        onQuantityChanged(cartItem.flowerId, newQuantity)
                    } else {
                        android.util.Log.d("CartAdapter", "Удаляем товар из корзины")
                        onRemoveItem(cartItem.flowerId)
                    }
                }
                
                btnRemove.setOnClickListener {
                    onRemoveItem(cartItem.flowerId)
                }
            }
        }
    }
}
