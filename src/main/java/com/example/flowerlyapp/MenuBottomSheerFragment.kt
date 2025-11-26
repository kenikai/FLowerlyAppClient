package com.example.flowerlyapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flowerlyapp.Adapters.PopularAdapter
import com.example.flowerlyapp.Models.PopularModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class MenuBottomSheerFragment : BottomSheetDialogFragment() {

    private lateinit var adapter: PopularAdapter
    private lateinit var menuList: ArrayList<PopularModel>
    private lateinit var menuRv : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_menu_bottom_sheer, container, false)

        menuList = ArrayList()
        menuList.add(PopularModel(R.drawable.rose101, "101 Роза", "8000 RUB", "Классический букет из свежих роз. Подойдет для любого случая.", "*Красные розы\n*Украшения"))
        menuList.add(PopularModel(R.drawable.monster_flower, "Букет 'Монстр'", "5000 RUB", "Эффектный букет с экзотическими цветами и насыщенными оттенками.", "*Белые розы\n*Хризантемы\n*Мелкие декоративные растения"))
        menuList.add(PopularModel(R.drawable.classic_flower, "Классический 'Пастель'", "6000 RUB","Нежная композиция в пастельных тонах. Идеально для романтического свидания.", "*Розовые и белые розы\n*Пионы\n*Мелкие декоративные растения"))
        menuList.add(PopularModel(R.drawable.autumn_flower, "Букет 'Осень'", "8000 RUB", "Теплый осенний букет с желтыми и оранжевыми оттенками.", "*Оранжевые и медные орхидеи\n*Сухоцветы"))
        menuList.add(PopularModel(R.drawable.box_flower, "Коробка 'trick or treaten'", "5000 RUB", "Эффектная цветочная композиция в стиле Хэллоуина, сочетающая элегантность и атмосферу праздника.", "*Оранжевые розы\n*Лилии\n*Рускус"))
        menuList.add(PopularModel(R.drawable.valentine_flower, "Букет 'Валентина'", "6000 RUB", "Специальный букет ко Дню Святого Валентина с красными розами.", "*Красные розы\n*Гвоздики\n*Ягоды гиперикума\n*Эвкалипт "))
        menuList.add(PopularModel(R.drawable.tulip_flower, "Букет 'Тюльпанов'", "8000 RUB", "Весенний букет из свежих тюльпанов в разных цветах.", "*Тюльпаны разных оттенков\n*Веточки зелени (эвкалипт, рускус)\n*Мелкие декоративные цветы (ваксфлауэр, гипсофила)"))
        menuList.add(PopularModel(R.drawable.birthday_flower, "Букет 'День рождения'", "5000 RUB", "Яркий и веселый букет, специально для Дня Рождения.", "*Розовые розы\n*Фиолетовая эустома\n*Красные мини-гвоздики\n*Зеленые гвоздики"))
        menuList.add(PopularModel(R.drawable.lillies_flower, "Букет 'Лиллия'", "6000 RUB", "Изысканный букет с ароматными лилиями.", "*Розовые лилии\n*Зелень (аспарагус, эвкалипт, рускус"))
        menuList.add(PopularModel(R.drawable.gerbera_flower, "Букет 'Гербер' ", "8000 RUB", "Яркий букет из гербер, который поднимет настроение.","*Оранжевые герберы\n*Мелкие белые цветы (гипсофила)\n*Эвкалипт"))



        adapter = PopularAdapter(requireContext(), menuList)

        menuRv = view.findViewById(R.id.menu_RV)
        menuRv.layoutManager = LinearLayoutManager(requireContext())
        menuRv.adapter = adapter
        return view
    }


}