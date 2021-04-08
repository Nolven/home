package com.example.homecontrole

data class GradientData(
    var blending: Int,
    var speed: Int,
    var sampler_step: Int,
    var colors: ArrayList<Array<Int>>
)