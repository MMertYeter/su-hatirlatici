package com.mert.sutakip.data.local

import androidx.room.TypeConverter
import com.mert.sutakip.data.local.entity.IcecekTuru

/** Room, enum tiplerini otomatik saklayamadığı için String<->Enum dönüşümü burada yapılır. */
class Converters {

    @TypeConverter
    fun icecekTurundenStringe(deger: IcecekTuru): String = deger.name

    @TypeConverter
    fun stringdenIcecekTurune(deger: String): IcecekTuru =
        runCatching { IcecekTuru.valueOf(deger) }.getOrDefault(IcecekTuru.SU)
}
