package com.example.flowerlyapp.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flowerlyapp.R
import com.example.flowerlyapp.data.database.entities.OrderEntity
import java.text.SimpleDateFormat
import java.util.*

/**
 * Адаптер для отображения списка заказов
 */
class OrdersAdapter(
    private val context: Context,
    private var orders: List<OrderEntity>,
    private val onOrderClick: (OrderEntity) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderId: TextView = itemView.findViewById(R.id.orderId)
        val orderStatus: TextView = itemView.findViewById(R.id.orderStatus)
        val orderTotal: TextView = itemView.findViewById(R.id.orderTotal)
        val orderDate: TextView = itemView.findViewById(R.id.orderDate)
        val orderAddress: TextView = itemView.findViewById(R.id.orderAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.order_item, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        
        holder.apply {
            orderId.text = "Заказ #${order.id.takeLast(8)}"
            orderStatus.text = getStatusText(order.status)
            orderTotal.text = "${order.totalAmount.toInt()} ₽"
            orderDate.text = formatDate(order.createdAt)
            orderAddress.text = order.deliveryAddress ?: "Адрес не указан"
            
            // Устанавливаем цвет статуса
            orderStatus.setTextColor(getStatusColor(order.status))
            
            // Обработчик клика
            itemView.setOnClickListener {
                onOrderClick(order)
            }
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<OrderEntity>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    private fun getStatusText(status: String): String {
        return when (status) {
            "pending" -> "Ожидает подтверждения"
            "confirmed" -> "Подтвержден"
            "shipped" -> "Отправлен"
            "delivered" -> "Доставлен"
            "cancelled" -> "Отменен"
            else -> status
        }
    }

    private fun getStatusColor(status: String): Int {
        return when (status) {
            "pending" -> context.getColor(android.R.color.holo_orange_dark)
            "confirmed" -> context.getColor(android.R.color.holo_blue_dark)
            "shipped" -> context.getColor(android.R.color.holo_purple)
            "delivered" -> context.getColor(android.R.color.holo_green_dark)
            "cancelled" -> context.getColor(android.R.color.holo_red_dark)
            else -> context.getColor(android.R.color.darker_gray)
        }
    }

    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return formatter.format(date)
    }
}
