package cn.wgc.custom.keyboard.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import cn.wgc.custom.keyboard.view.KeyboardEditText

/**
 * <pre>
 *
 *     author : wgc
 *     time   : 2024/07/18
 *     desc   :
 *     version: 1.0
 *
 * </pre>
 */
object KeyboardUtil {

    /**
     * dialog中隐藏键盘
     */
    fun dispatchTouchEvent(ev: MotionEvent, dialog: Dialog) {
        val view = dialog.currentFocus
        if (ev.action == MotionEvent.ACTION_UP && view != null) {
            if (isShouldHideKeyBord(view, ev)) {
                //隐藏系统键盘
                hideSoftInput(view.windowToken, dialog.context)
                view.clearFocus()
            }
        }
    }

    /**
     * activity中隐藏键盘
     */
    fun dispatchTouchEvent(ev: MotionEvent, activity: AppCompatActivity) {
        val view = activity.currentFocus
        if (ev.action == MotionEvent.ACTION_UP && view != null) {
            if (isShouldHideKeyBord(view, ev)) {
                //隐藏系统键盘
                hideSoftInput(view.windowToken, activity)
                view.clearFocus()
            }
        }
    }

    /**
     * 判定当前是否需要隐藏键盘
     */
    private fun isShouldHideKeyBord(v: View, ev: MotionEvent): Boolean {
        if (v is EditText) {
            val l = intArrayOf(0, 0)
            v.getLocationInWindow(l)
            val left = l[0]
            val top = l[1]
            val bottom = top + v.getHeight()
            val right = left + v.getWidth()
            return !(ev.x > left && ev.x < right && ev.y > top && ev.y < bottom)
        }
        return false
    }

    /**
     * 隐藏软键盘
     */
    private fun hideSoftInput(token: IBinder?, context: Context) {
        if (token != null) {
            val manager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(token, 0)
        }
    }


    /**
     *解决ScrollView中自动获取焦点，弹出键盘的问题
     */
    @SuppressLint("ClickableViewAccessibility")
    fun handScrollViewFocusable(scrollView: ScrollView) {
        scrollView.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
        scrollView.isFocusable = true
        scrollView.isFocusableInTouchMode = true
    }

    fun handDialogKeyboardStatus(dialog: Dialog, contentView: View, hasListener: Boolean,
                                 vararg keyboardEditTexts: KeyboardEditText) {
        val dialogWindow = dialog.window!!

        keyboardEditTexts.forEach {
            it.addDialogWindow(dialogWindow)
        }
        if (!hasListener) {
            dialog.setOnShowListener { handKeyboardLocation(dialog, contentView, keyboardEditTexts) }
        } else {
            handKeyboardLocation(dialog, contentView, keyboardEditTexts)
        }

    }

    private fun handKeyboardLocation(dialog: Dialog, contentView: View,
                                     keyboardEditTexts: Array<out KeyboardEditText>) {
        contentView.viewTreeObserver?.addOnGlobalLayoutListener(object :
                                                                    ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                contentView.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                //延时为了解决在dialog中屏蔽导航栏，键盘显示会上顶一个导航栏高度的问题
//                contentView.postDelayed({
                val context = dialog.context
                val dm = DisplayMetrics()
                val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val screenHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    manager.currentWindowMetrics.bounds.height()
                } else {
                    manager.defaultDisplay.getRealMetrics(dm)
                    dm.heightPixels
                }
                val location = IntArray(2)
                contentView.getLocationOnScreen(location)
                val start = location[1]
                val navBarVisible = if (dialog.ownerActivity != null) {
                    hasNavigationBar(dialog.ownerActivity!!.window, context)
                } else {
                    hasNavigationBar(((context as ContextWrapper).baseContext as Activity).window, context)
                }
                val navigationBarHeight = if (navBarVisible) getNavigationBarHeight(context) else 0
                val attributes = dialog.window?.attributes
                var popupOffsetY = if (attributes?.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                    if (!navBarVisible) {
                        screenHeight - start - contentView.height
                    } else {
                        screenHeight - start - contentView.height - navigationBarHeight
                    }
                } else {
                    if (!navBarVisible) {
                        0
                    } else {
                        screenHeight - start - contentView.height - navigationBarHeight * 2
                    }
                }
                //计算实际控件在dialog中window的实际位置Y的偏差，位置偏差
                val selfLocationOffsetY = when (attributes?.gravity) {
                    Gravity.CENTER -> screenHeight / 2 - contentView.height / 2
                    Gravity.BOTTOM,
                    Gravity.BOTTOM or Gravity.RIGHT,
                    Gravity.BOTTOM or Gravity.LEFT,
                    -> screenHeight - contentView.height / 2

                    else -> 0
                }
                keyboardEditTexts.forEach {
                    it.setPopupLocationOffsetYOnDialog(-popupOffsetY)
                    it.setSelfLocationOffsetYOnDialog(selfLocationOffsetY)
                }

            }
        })
    }

    @SuppressLint("InternalInsetResource")
    fun getNavigationBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId != 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    @SuppressLint("InternalInsetResource")
    fun getStatusBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId != 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    fun hasNavigationBar(context: Context): Boolean {
        val resources = context.resources
        val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return if (id > 0) {
            resources.getBoolean(id)
        } else false
    }

    /**
     * 判断底部状态栏是否显示
     */
    fun hasNavigationBar(window: Window,context: Context): Boolean {
        val displayMetrics = context.resources.displayMetrics
        val usableHeight = displayMetrics.heightPixels
        val realHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.windowManager.currentWindowMetrics.bounds.height()
        } else {
            val metrics = DisplayMetrics()
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(metrics)
            metrics.heightPixels
        }
        return realHeight > usableHeight
    }
}