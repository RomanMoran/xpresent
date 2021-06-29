package com.xpresent.xpresent.custom_view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.Button
import android.widget.LinearLayout
import com.xpresent.xpresent.R
import com.xpresent.xpresent.custom_view.SelectorButton.Type.*
import com.xpresent.xpresent.util.*

class SelectorButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var selected = FIRST
        set(value) {
            field = value
            firstButton.initColor(FIRST)
            secondButton.initColor(SECOND)
            listener?.invoke(field)
        }

    private val firstButton by lazy { initButton(context, FIRST) }
    private val secondButton by lazy { initButton(context, SECOND) }
    private var listener: ((Type) -> Unit?)? = null

    init {
        orientation = HORIZONTAL
        addView(firstButton)
        addView(secondButton)
    }

    fun onSelectedListener(listener: (Type) -> Unit?) {
        this.listener = listener
    }

    val isFirst get() = selected == FIRST
    val isSecond get() = selected == SECOND

    private fun initButton(context: Context, type: Type) = Button(
        context, null, R.style.Widget_AppCompat_ButtonBar_AlertDialog
    ).apply {
        layoutParams = LayoutParams(MATCH_PARENT, 36.dpToPx, 1F).apply {
            if (type == FIRST) setMargins(0,0, 10.dpToPx, 0)
        }
        gravity = Gravity.CENTER
        transformationMethod = null
        setBackgroundResource(R.drawable.tab_indicator)
        initColor(type)
        text(type.stringRes)
        onClickListener { selected = type }
    }

    private fun Button.initColor(type: Type) {
        backgroundTint(type.currentBgColor)
        textColor(type.currentTextColor)
    }

    private val Type.currentBgColor get() = when (this == selected) {
        true -> R.color.colorTagBlue
        false -> R.color.mine_shaft_alpha_50
    }

    private val Type.currentTextColor get() = when (this == selected) {
        true -> R.color.colorTagBlue
        false -> R.color.colorOnSurface
    }

//    private val attrs get() =

    enum class Type(val stringRes: Int) {
        FIRST(R.string.to_myself), SECOND(R.string.for_present)
    }
}