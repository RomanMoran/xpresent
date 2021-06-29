package com.xpresent.xpresent.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.*
import android.text.Editable
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.format.DateUtils
import android.util.Base64
import android.util.Patterns
import android.view.*
import android.view.ViewGroup.LayoutParams.*
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.webkit.URLUtil
import android.widget.*
import androidx.annotation.MenuRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.textfield.TextInputLayout
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

// TODO Util String

@Suppress("SimpleDateFormat")
fun String.toCalendar(
    format: String
): Calendar = try {
    dateFormat(format, TimeZone.getTimeZone("UTC+0"))
        .parse(this)
        ?.run { Calendar.getInstance().apply { timeInMillis = this@run.time } }
        ?: Calendar.getInstance().apply { timeInMillis = 0 }
} catch (e: Exception) { Calendar.getInstance().apply { timeInMillis = 0 } }

inline val Editable?.orEmpty get() = this?.toString() ?: ""

val String?.toInt get() = if (!this.isNullOrEmpty()) Integer.parseInt(this) else 0

@Suppress("DEPRECATION")
inline val String?.formatAsHtml
    get() = this?.let {
        if (SDK_INT < N) Html.fromHtml(it).toString()
        else Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY).toString()
    } ?: ""

inline val String.stringToBitmap: Bitmap
    get() = Base64.decode(this, Base64.DEFAULT).run {
        return BitmapFactory.decodeByteArray(this, 0, size)
    }

inline val String.isNetworkUrl get() = URLUtil.isNetworkUrl(this)

inline val String.isValidUrl get() = URLUtil.isValidUrl(this)

inline val String.isUrl get() = Patterns.WEB_URL.matcher(this).matches()

// TODO Util Image

inline val Bitmap.bitmapToString: String
    get() {
        val byteArrayBitmapStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 100, byteArrayBitmapStream)
        return Base64.encodeToString(byteArrayBitmapStream.toByteArray(), Base64.DEFAULT)

    }

fun Bitmap.toFile(pathname: String): File {
    val file = File(pathname)
    val outputStream = file.outputStream()
    compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    outputStream.flush()
    outputStream.close()
    return file
}


inline val Drawable.drawableToString: String
    get() {
        val byteArrayBitmapStream = ByteArrayOutputStream()
        toBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayBitmapStream)
        return Base64.encodeToString(byteArrayBitmapStream.toByteArray(), Base64.DEFAULT)
    }
inline val Drawable.toBitmap: Bitmap
    get() = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)

fun Activity.write(bitmap: Bitmap): String {
    val file = File(filesDir, "${System.currentTimeMillis()}.jpg")
    if (file.exists()) file.delete()
    try {
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, out)
        out.flush()
        out.close()
    } catch (e: Exception) {
        return ""
    }
    return file.absolutePath
}

// TODO Util Int, Long, Float, Double

inline val Int.dpToPx get() = (this * (Resources.getSystem().displayMetrics.density + 0.25F)).roundToInt()

inline val Float.dpToPx get() = this * (Resources.getSystem().displayMetrics.density + 0.25F)

inline val Int.pxToDp get() = (this / (Resources.getSystem().displayMetrics.density + 0.25F)).roundToInt()

inline val Float.pxToDp get() = this / (Resources.getSystem().displayMetrics.density + 0.25F)

inline val Long.serverTimeFormat: String
    @SuppressLint("SimpleDateFormat")
    get() = DateUtils.formatElapsedTime(this / 1000)

inline val Long.timeFormat: String
    @SuppressLint("SimpleDateFormat")
    get() = dateFormat("HH:mm").format(this)

// TODO Util Data

fun Calendar.formatDate(format: String): String = dateFormat(format).format(time)

inline val Calendar.serverDateFormat: String
    @SuppressLint("SimpleDateFormat")
    get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time)

inline val Calendar.timeFormat: String
    @SuppressLint("SimpleDateFormat")
    get() = dateFormat("HH:mm").format(time)


@SuppressLint("SimpleDateFormat")
fun dateFormat(
    pattern: String,
    timeZone: TimeZone = TimeZone.getTimeZone("UTC+0")
) = SimpleDateFormat(pattern)
    .apply { this.timeZone = timeZone }

val Calendar.dateFormatForDisplay: String
    @SuppressLint("SimpleDateFormat")
    get() = dateFormat("dd.MM.yyyy", TimeZone.getDefault()).format(time)

//TODO Util Android

inline val Context.authority get() = "$packageName.provider"

fun Context.uriForFile(file: File) = FileProvider.getUriForFile(this, authority, file)


inline val USAGE_STATS_SERVICE
    get() = if (SDK_INT >= LOLLIPOP_MR1) Context.USAGE_STATS_SERVICE else "usagestats"


// TODO Util Activity

inline fun Activity.onKeyboardVisibleListener(
    crossinline execute: (Boolean) -> Unit
) = findViewById<View>(android.R.id.content).let {
    it.viewTreeObserver.addOnGlobalLayoutListener {
        val r = Rect()
        it.getWindowVisibleDisplayFrame(r)
        val screenHeight = it.rootView.height
        val keypadHeight = screenHeight - r.bottom
        execute.invoke(keypadHeight > screenHeight * 0.15)
    }
}

@SuppressLint("ClickableViewAccessibility")
fun Activity.setupUI(v: View?) {
    if (v != null && v !is EditText) {
        v.setOnTouchListener { view, _ ->
            view.hideKeyboard()
            false
        }
    }
    if (v is ViewGroup) for (i in 0 until v.childCount) setupUI(v.getChildAt(i))
}

@SuppressLint("RestrictedApi")
inline fun FragmentActivity.popBackStackOrOnBackPressed(
    crossinline onBackPressed: () -> Unit
) = when (supportFragmentManager.fragments.isEmpty()) {
    true -> onBackPressed.invoke()
    false -> supportFragmentManager.menuVisible {
        when {
            it.childFragmentManager.backStackEntryCount > 0 -> it.childFragmentManager.popBackStack()
            it.parentFragmentManager.backStackEntryCount > 0 -> it.parentFragmentManager.popBackStack()
            else -> onBackPressed.invoke()
        }
    }
}


@SuppressLint("RestrictedApi")
inline fun FragmentManager.menuVisible(crossinline isMenuVisible: (Fragment) -> Unit) {
    for (it in fragments) {
        if (it.isMenuVisible) {
            isMenuVisible.invoke(it); break
        }
    }
}

@SuppressLint("RestrictedApi")
inline fun FragmentActivity.popBackStackOrOnBackPressed(
    isChild: Boolean,
    crossinline onBackPressed: () -> Unit
) = when (supportFragmentManager.fragments.isEmpty()) {
    true -> onBackPressed.invoke()
    false -> for (it in supportFragmentManager.fragments) {
        if (it.isMenuVisible) {
            when (isChild) {
                true -> it.childFragmentManager.popBackStack(onBackPressed)
                false -> it.parentFragmentManager.popBackStack(onBackPressed)
            }
            break
        }
    }
}

inline fun FragmentManager.popBackStack(
    crossinline onBackPressed: () -> Unit
) = if (backStackEntryCount > 0) popBackStack() else onBackPressed.invoke()

fun Activity.statusBarColor(colorId: Int) {
    if (SDK_INT >= LOLLIPOP)  window.statusBarColor = color(colorId)
}

fun Activity.navigationBarColor(colorId: Int) {
    if (SDK_INT >= LOLLIPOP) window.navigationBarColor = color(colorId)
}



// TODO Util Fragment

fun Fragment.string(stringId: Int) = requireContext().string(stringId)

fun Fragment.string(stringId: Int, vararg formatArgs: Any?) = requireContext().string(
    stringId,
    *formatArgs
)

fun Fragment.stringArray(arrayId: Int) = requireContext().resources.getStringArray(arrayId).toList()


// TODO Util Context

fun Context.string(stringId: Int) = getString(stringId)

fun <T> Context.string(stringId: Int, vararg formatArgs: T?) = getString(stringId, *formatArgs)

fun Context.stringArray(resId: Int): Array<String> = resources.getStringArray(resId)

fun Context.statusBarColor(colorId: Int) {
    if (SDK_INT >= LOLLIPOP && this is Activity) window.statusBarColor = color(colorId)

}

fun Context.navigationBarColor(colorId: Int) {
    if (SDK_INT >= LOLLIPOP && this is Activity) window.navigationBarColor = color(colorId)

}

inline val Context.layoutInflater
    get() = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

inline val Context.inputMethodManager
    get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

inline val Context.orientation get() = this.resources.configuration.orientation

fun Context.toast(text: String, flag: Int = Toast.LENGTH_LONG) = Toast
    .makeText(applicationContext, text, flag)
    .show()

fun Context.toast(resId: Int, flag: Int = Toast.LENGTH_LONG) = Toast
    .makeText(applicationContext, getString(resId), flag)
    .show()

@SuppressLint("UseCompatLoadingForDrawables")
fun Context.drawable(resId: Int) = if (SDK_INT >= LOLLIPOP) {
    resources.getDrawable(resId, theme)
} else {
    ContextCompat.getDrawable(this, resId)
}

@SuppressLint("UseCompatLoadingForColorStateLists")
fun Context.colorStateList(resId: Int) = if (SDK_INT >= M) {
    getColorStateList(resId)
} else {
    resources.getColorStateList(resId)
}

@SuppressLint("UseCompatLoadingForColorStateLists")
fun Context.color(resId: Int) = if (SDK_INT >= M) {
    getColor(resId)
} else {
    resources.getColor(resId)
}

fun Context.font(resId: Int) = if (SDK_INT >= O) {
    resources.getFont(resId)
} else {
    ResourcesCompat.getFont(this, resId)
}


// TODO Util View

fun View.string(stringId: Int) = context.string(stringId)

fun View.string(stringId: Int, vararg formatArgs: Any?) = context.string(stringId, *formatArgs)

inline fun <T> View.onClickListener(crossinline onClickListener: () -> T?) = setOnClickListener {
    onClickListener()
}

inline fun <T> View.onClickListener(
    isHideKeyboard: Boolean = false, crossinline onClickListener: () -> T?
) = setOnClickListener { if (isHideKeyboard) hideKeyboard(); onClickListener() }

inline fun <T> EditText.onDoneClickListener(
    crossinline onDoneClick: (text: String) -> T?
) = setOnEditorActionListener { _, actionId, _ ->
    if (actionId == IME_ACTION_DONE) {
        clearFocus()
        hideKeyboard()
        onDoneClick.invoke(string)
        return@setOnEditorActionListener true
    }
    false
}

val EditText.isDoneClickClearFocus get() = setOnEditorActionListener { _, actionId, _ ->
    if (actionId == IME_ACTION_DONE) {
        clearFocus()
        hideKeyboard()
        return@setOnEditorActionListener true
    }
    false
}

fun View.backgroundTintList(resId: Int) = if (SDK_INT >= LOLLIPOP) {
    backgroundTintList = context.colorStateList(resId)
} else {
    background.setColorFilter(context.color(resId), PorterDuff.Mode.SRC_ATOP)
}

fun ImageView.imageTintList(resId: Int) = if (SDK_INT >= LOLLIPOP) {
    imageTintList = context.colorStateList(resId)
} else {
    setColorFilter(context.color(resId), PorterDuff.Mode.SRC_ATOP)
}

inline fun View.onKeyboardVisibleListener(
    crossinline execute: (Boolean) -> Unit
) = findViewById<View>(android.R.id.content).let {
    it.viewTreeObserver.addOnGlobalLayoutListener {
        val r = Rect()
        it.getWindowVisibleDisplayFrame(r)
        val screenHeight = it.rootView.height
        val keypadHeight = screenHeight - r.bottom
        execute.invoke(keypadHeight > screenHeight * 0.15)
    }
}

val EditText.requestFocus: Unit get() { requestFocus(); showKeyboard() }

val EditText.clearFocus: Unit get() { clearFocus(); hideKeyboard() }

fun View.clearFocusAndHideKeyboard() {
    clearFocus()
    hideKeyboard()
}

fun View.hideKeyboard() = context
    .inputMethodManager
    .hideSoftInputFromWindow(this.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)


fun View.showKeyboard()  = context
    .inputMethodManager
    .showSoftInput(this, SHOW_IMPLICIT)

@RequiresApi(LOLLIPOP)
fun View.backgroundTint(resId: Int) {
    backgroundTintList = context.colorStateList(resId)
}

fun ImageView.loadAppsIcon(packageName: String) = load(
    this.context.packageManager.getApplicationIcon(packageName)
)

fun ImageView.loadWebsiteIcon(domain: String) = Glide.with(this)
    .load("https://www.google.com/s2/favicons?sz=64&domain=$domain")
    .into(object : CustomTarget<Drawable>() {
        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
            when (resource.toBitmap.byteCount != 1024) {
                true -> setImageDrawable(resource)
//                false -> setImageResource()
            }
        }

        override fun onLoadCleared(placeholder: Drawable?) {}
    })

fun ImageView.load(model: Any?, error: Int = 0) = Glide
    .with(this)
    .load(model)
    .error(error)
    .into(this)
    .view

fun ImageView.load(model: Any?, error: Int = 0, onResourceReady: () -> Unit) = Glide
    .with(this)
    .load(model)
    .listener(
        object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ) = false

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                onResourceReady()
                return false
            }
        }
    )
    .error(error)
    .into(this)
    .view

fun ImageView.loadNotCache(model: Any?, error: Int = 0) = Glide
    .with(this)
    .load(model)
    .diskCacheStrategy(DiskCacheStrategy.NONE)
    .skipMemoryCache(false)
    .error(error)
    .into(this)
    .view

inline val TimePicker.selectedHour get() = if (SDK_INT >= M) hour else currentHour

inline val TimePicker.selectedMinute get() = if (SDK_INT >= M) minute else currentMinute

fun TimePicker.hour(hour: Int) {
    if (SDK_INT >= M) this.hour = hour else currentHour = hour
}

fun TimePicker.minute(minute: Int) {
    if (SDK_INT >= M) this.minute = minute else currentMinute = minute
}

inline fun Spinner.onItemSelectedListener(crossinline onItemSelected: (position: Int) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            onItemSelected.invoke(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }
}

fun TextView.drawables(
    start: Int, top: Int, end: Int, bottom: Int
) = setCompoundDrawablesWithIntrinsicBounds(start, top, end, bottom)

fun TextView.drawablesStart(start: Int) = drawables(start, 0, 0, 0)

fun TextView.drawablesTop(top: Int) = drawables(0, top, 0, 0)

fun TextView.drawablesEnd(end: Int) = drawables(0, 0, end, 0)

fun TextView.drawablesBottom(bottom: Int) = drawables(0, 0, 0, bottom)

fun View.padding(
    startDp: Int, topDp: Int, endDp: Int, bottomDp: Int
) = setPadding(startDp.dpToPx, topDp.dpToPx, endDp.dpToPx, bottomDp.dpToPx)

fun View.paddingStart(startDp: Int) = setPadding(startDp.dpToPx, paddingTop, paddingEnd, paddingBottom)

fun View.paddingTop(topDp: Int) = setPadding(paddingLeft, topDp.dpToPx, paddingEnd, paddingBottom)

fun View.paddingEnd(endDp: Int) = setPadding(paddingLeft, paddingTop, endDp.dpToPx, paddingBottom)

fun View.paddingBottom(bottomDp: Int) = setPadding(paddingLeft, paddingTop, paddingEnd, bottomDp.dpToPx)

fun TextView.text(stringId: Int) { text = context.getString(stringId) }

fun TextView.text(stringId: Int, vararg formatArgs: Any?) {
    text = context.getString(stringId, *formatArgs)
}

fun TextView.textColor(colorId: Int) = setTextColor(context.color(colorId))

fun View.backgroundColor(colorId: Int) = setBackgroundColor(context.color(colorId))

inline fun View.onLayoutWasDrawnListener(crossinline onLayoutWasDrawn: View.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            onLayoutWasDrawn.invoke(this@onLayoutWasDrawnListener)
            viewTreeObserver.removeOnGlobalLayoutListener(this)
        }

    })
}

var EditText.string: String
    get() = text.orEmpty
    set(value) { if (value.isNotEmpty()) text = SpannableStringBuilder(value) else text.clear() }

inline fun EditText.onTextChangedListener(
    crossinline beforeTextChanged: (
        text: CharSequence?,
        start: Int,
        count: Int,
        after: Int
    ) -> Unit = { _, _, _, _ -> },
    crossinline onTextChanged: (
        text: CharSequence?,
        start: Int,
        before: Int,
        count: Int
    ) -> Unit = { _, _, _, _ -> },
    crossinline afterTextChanged: (text: String) -> Unit = {}
): TextWatcher {
    val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) = afterTextChanged.invoke(s.orEmpty)

        override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {
            beforeTextChanged.invoke(text, start, count, after)
        }

        override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
            onTextChanged.invoke(text, start, before, count)
        }
    }
    addTextChangedListener(textWatcher)
    return textWatcher
}

inline fun EditText.onAfterTextChangedListener(
    crossinline afterTextChanged: (text: String) -> Unit = {}
): TextWatcher {
    val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) = afterTextChanged.invoke(s.orEmpty)

        override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }
    addTextChangedListener(textWatcher)
    return textWatcher
}

inline fun ViewPager2.onPageSelectedListener(crossinline onPageSelected: (position: Int) -> Unit) {
    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            onPageSelected.invoke(position)
        }

    })
}

//inline val ListPopupWindow.isShow get() = listView?.isShown.orFalse


// TODO Util Unit

fun fromHtml(text: String, flag: Int = HtmlCompat.FROM_HTML_MODE_LEGACY) = HtmlCompat
    .fromHtml(text, flag)


// TODO Util Dialog
