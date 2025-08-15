package cn.wgc.custom.keyboard.util

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.IBinder
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity

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
}