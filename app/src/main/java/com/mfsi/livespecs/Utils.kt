package com.mfsi.livespecs

import android.content.Context
import android.widget.Toast

//Extension function for creating toast in less verbose way
fun Context.toast(context: Context = applicationContext, text: String, duration: Int = Toast.LENGTH_SHORT){
    Toast.makeText(context, text, duration).show()
}