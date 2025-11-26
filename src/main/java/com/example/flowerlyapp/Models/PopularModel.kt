package com.example.flowerlyapp.Models

class PopularModel{

    private var flowerImage : Int? = null
    private var flowerName : String = ""
    private var flowerPrice : String = ""
    private var flowerDescription : String = ""
    private var flowerCompose : String = ""
    private var flowerId : String = ""

    constructor()
    constructor(flowerImage: Int?, flowerName: String, flowerPrice: String , flowerDescription: String, flowerCompose: String, flowerId: String = "") {
        this.flowerImage = flowerImage
        this.flowerName = flowerName
        this.flowerPrice = flowerPrice
        this.flowerDescription = flowerDescription
        this.flowerCompose = flowerCompose
        this.flowerId = flowerId

    }

     fun getFlowerImage() : Int? {
        return flowerImage
    }

     fun getFlowerName() : String{
        return flowerName
    }

     fun getFlowerPrice() : String{
        return flowerPrice
    }

    fun getFlowerDescription() : String{
        return flowerDescription
    }

    fun getFlowerCompose() : String{
        return flowerCompose
    }

    fun getFlowerId() : String{
        return flowerId
    }

     fun setFlowerImage(flowerImage: Int?){
        this.flowerImage = flowerImage
    }

     fun setFlowerName(flowerName: String){
        this.flowerName = flowerName
    }

     fun setFlowerPrice(flowerPrice: String){
        this.flowerPrice = flowerPrice
    }

    fun setFlowerDescription(flowerDescription : String){
        this.flowerDescription = flowerDescription
    }

    fun setFlowerCompose(flowerComposition : String){
        this.flowerCompose = flowerCompose
    }

    fun setFlowerId(flowerId : String){
        this.flowerId = flowerId
    }
}


