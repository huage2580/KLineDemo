package com.hua.klinetext

data class KData(val open:Double,val close:Double,val max:Double,val min:Double,
                var rangeMax:Double = 0.0,var rangeMin:Double = 0.0)