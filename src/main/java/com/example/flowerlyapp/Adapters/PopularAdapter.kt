package com.example.flowerlyapp.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flowerlyapp.DetailsActivity
import com.example.flowerlyapp.Models.PopularModel
import com.example.flowerlyapp.databinding.HomeFlowerItemBinding

class PopularAdapter(
    val context : Context,
    val list: ArrayList<PopularModel>
) : RecyclerView.Adapter<PopularAdapter.PopularViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PopularAdapter.PopularViewHolder {
       val binding = HomeFlowerItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return PopularViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PopularAdapter.PopularViewHolder, position: Int) {

        val listModel = list[position]

        holder.flowerName.text = listModel.getFlowerName()
        holder.flowerPrice.text = listModel.getFlowerPrice()
        listModel.getFlowerImage()?.let { holder.flowerImage.setImageResource(it) }

        holder.item.setOnClickListener{
            val intent = Intent(context, DetailsActivity :: class.java)
            intent.putExtra("flowerId", listModel.getFlowerId())
            intent.putExtra("flowerImage", listModel.getFlowerImage())
            intent.putExtra("flowerName", listModel.getFlowerName())
            intent.putExtra("flowerDescription", listModel.getFlowerDescription())
            intent.putExtra("flowerCompose", listModel.getFlowerCompose())
            intent.putExtra("flowerPrice", listModel.getFlowerPrice().replace(" ₽", "").toDoubleOrNull() ?: 0.0)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class PopularViewHolder(binding : HomeFlowerItemBinding) : RecyclerView.ViewHolder(binding.root) {

        val flowerImage = binding.homeFlowerImage
        val flowerName = binding.homeFlowerName
        val flowerPrice = binding.homeFlowerPrice

        val item = binding.root

    }
}