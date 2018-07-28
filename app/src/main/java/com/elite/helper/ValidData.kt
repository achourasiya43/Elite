package com.elite.helper

import android.widget.EditText
import android.widget.TextView

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by Kotlin on 2/27/2016.
 */
class ValidData {

    private fun getString(textView: TextView): String {
        return textView.text.toString().trim { it <= ' ' }
    }


    fun isNullValue(textView: TextView): Boolean {
        return if (getString(textView).isEmpty() && getString(textView) == "") {
            false
        } else true
    }

    fun isEmpty(textView: TextView): Boolean {
        return if (getString(textView).isEmpty()) {
            true
        } else false
    }

    fun isEmailValid(editText: EditText): Boolean {
        val getValue = editText.text.toString().trim { it <= ' ' }
        return android.util.Patterns.EMAIL_ADDRESS.matcher(getValue).matches()

    }

    fun isPasswordValid(editText: EditText): Boolean {
        val getValue = editText.text.toString().trim { it <= ' ' }// trm white space
        return getValue.length >= 6

    }

    fun isPhoneValid(editText: EditText): Boolean {
        val getValue = editText.text.toString().trim { it <= ' ' }// trm white space
        return getValue.length >= 4

    }



}
