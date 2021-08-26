package com.example.homecontrole.led

data class GradientData(
    var blending: Int,
    var speed: Int,
    var sampler_step: Int,
    var colors: ArrayList<Array<Int>>
)