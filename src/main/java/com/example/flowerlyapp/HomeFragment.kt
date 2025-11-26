package com.example.flowerlyapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.flowerlyapp.Adapters.ImageSliderAdapter
import com.example.flowerlyapp.Adapters.PopularAdapter
import com.example.flowerlyapp.Models.PopularModel
import com.example.flowerlyapp.presentation.viewmodel.AuthViewModelFactory
import com.example.flowerlyapp.presentation.viewmodel.CartViewModel
import com.example.flowerlyapp.data.network.TokenManager


class HomeFragment : Fragment() {

    private lateinit var viewPager2 : ViewPager2
    private lateinit var adapter : ImageSliderAdapter
    private lateinit var imageList: ArrayList<Int>
    private lateinit var handler: Handler

    private lateinit var popularAdapter : PopularAdapter
    private lateinit var listPopular : ArrayList<PopularModel>
    private lateinit var homeRv : RecyclerView

    private lateinit var goMenuText : TextView
    
    // ViewModels
    private val cartViewModel: CartViewModel by viewModels { AuthViewModelFactory(requireContext()) }
    
    // State
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Получаем user_id
        currentUserId = TokenManager.getUserId()
        val isLoggedIn = TokenManager.isLoggedIn()
        android.util.Log.d("HomeFragment", "onCreate: currentUserId = $currentUserId")
        android.util.Log.d("HomeFragment", "onCreate: isLoggedIn = $isLoggedIn")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        viewPager2 = view.findViewById(R.id.imageSlider)

        homeRv = view.findViewById(R.id.home_RV)
        goMenuText = view.findViewById(R.id.view_all)

        goMenuText.setOnClickListener{
            val bottomSheetMenu = MenuBottomSheerFragment()
            bottomSheetMenu.show(parentFragmentManager, "Test")
        }

        listPopular = ArrayList()
        listPopular.add(PopularModel(R.drawable.rose101, "101 Роза", "8000 RUB", "Классический букет из свежих роз. Подойдет для любого случая.", "*Красные розы\n*Украшения", "flower_1"))
        listPopular.add(PopularModel(R.drawable.monster_flower, "Букет 'Монстр'", "5000 RUB", "Эффектный букет с экзотическими цветами и насыщенными оттенками.", "*Белые розы\n*Хризантемы\n*Мелкие декоративные растения", "flower_2"))
        listPopular.add(PopularModel(R.drawable.classic_flower, "Классический 'Пастель'", "6000 RUB","Нежная композиция в пастельных тонах. Идеально для романтического свидания.", "*Розовые и белые розы\n*Пионы\n*Мелкие декоративные растения", "flower_3"))
        listPopular.add(PopularModel(R.drawable.autumn_flower, "Букет 'Осень'", "8000 RUB", "Теплый осенний букет с желтыми и оранжевыми оттенками.", "*Оранжевые и медные орхидеи\n*Сухоцветы", "flower_4"))
        listPopular.add(PopularModel(R.drawable.box_flower, "Коробка 'trick or treaten'", "5000 RUB", "Эффектная цветочная композиция в стиле Хэллоуина, сочетающая элегантность и атмосферу праздника.", "*Оранжевые розы\n*Лилии\n*Рускус", "flower_5"))
        listPopular.add(PopularModel(R.drawable.valentine_flower, "Букет 'Валентина'", "6000 RUB", "Специальный букет ко Дню Святого Валентина с красными розами.", "*Красные розы\n*Гвоздики\n*Ягоды гиперикума\n*Эвкалипт ", "flower_6"))
        listPopular.add(PopularModel(R.drawable.tulip_flower, "Букет 'Тюльпанов'", "8000 RUB", "Весенний букет из свежих тюльпанов в разных цветах.", "*Тюльпаны разных оттенков\n*Веточки зелени (эвкалипт, рускус)\n*Мелкие декоративные цветы (ваксфлауэр, гипсофила)", "flower_7"))
        listPopular.add(PopularModel(R.drawable.birthday_flower, "Букет 'День рождения'", "5000 RUB", "Яркий и веселый букет, специально для Дня Рождения.", "*Розовые розы\n*Фиолетовая эустома\n*Красные мини-гвоздики\n*Зеленые гвоздики", "flower_8"))
        listPopular.add(PopularModel(R.drawable.lillies_flower, "Букет 'Лиллия'", "6000 RUB", "Изысканный букет с ароматными лилиями.", "*Розовые лилии\n*Зелень (аспарагус, эвкалипт, рускус", "flower_9"))
        listPopular.add(PopularModel(R.drawable.gerbera_flower, "Букет 'Гербер' ", "8000 RUB", "Яркий букет из гербер, который поднимет настроение.","*Оранжевые герберы\nМелкие белые цветы (гипсофила)\n*Эвкалипт", "flower_10"))


        popularAdapter = PopularAdapter(requireContext(), listPopular)

        homeRv.layoutManager = LinearLayoutManager(requireContext())
        homeRv.adapter = popularAdapter




        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setTransfarmer()
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 2000)
            }

        })

    }

    private val runnable = Runnable {
        viewPager2.currentItem = viewPager2.currentItem + 1
    }

    private fun setTransfarmer() {
       val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(10))
        transformer.addTransformer{page, position ->
            val r = 1- Math.abs(position)
            page.scaleY = 0.85f + r * 0.14f
        }

        viewPager2.setPageTransformer(transformer)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, 2000)
    }

    private fun init() {
        imageList = ArrayList()
        adapter = ImageSliderAdapter(requireContext(), imageList, viewPager2)
        handler = Handler(Looper.myLooper()!!)

        imageList.add(R.drawable.banner)
        imageList.add(R.drawable.banner_2)
        imageList.add(R.drawable.banner_3)

        viewPager2.adapter = adapter
        viewPager2.offscreenPageLimit = 3
        viewPager2.clipToPadding = false
        viewPager2.clipChildren = false
        viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    }
}
