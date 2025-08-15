package cn.wgc.custom.keyboard.view

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.InputType
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import cn.wgc.custom.keyboard.R
import cn.wgc.custom.keyboard.view.CustomKeyboard.Companion.LETTER_TO_NUMBER_TYPE


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
@Suppress("DEPRECATION")
open class KeyboardEditText : AppCompatEditText, View.OnFocusChangeListener {

    private lateinit var keyboardView: CustomKeyboard
    private var keyboardType = LETTER_TO_NUMBER_TYPE
    private lateinit var popupWindow: PopupWindow
    private var pointInputEnable = false
    private var shufflePwdKey = false
    private var useDialogWindow = false
    private var autoHandlerPwdEnable = true
    private var currentWindow: Window? = null
    private var keyboardTopY = 0

    //将 contentParent 拆分为两个职责更明确的变量
    private lateinit var scrollableContainer: View // 用于滚动的容器
    private lateinit var popupAnchorView: View      // 用于定位的锚点
    private var onKeyboardStatusChangeListener: OnKeyboardStatusChangeListener? = null


    //获取屏幕的高度
    private val screenHeight: Int
        get() {
            val dm = DisplayMetrics()
            val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return manager.currentWindowMetrics.bounds.height()
            } else {
                manager.defaultDisplay.getRealMetrics(dm)
                return dm.heightPixels
            }
        }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
                                                                                   attrs,
                                                                                   defStyleAttr) {
        init(context, attrs)
    }

    @SuppressLint("UseKtx")
    private fun init(context: Context, attrs: AttributeSet?) {
        attrs?.let {
            val ty = context.obtainStyledAttributes(it, R.styleable.KeyboardEditText)
            keyboardType =
                ty.getInt(R.styleable.KeyboardEditText_keyboardType, LETTER_TO_NUMBER_TYPE)
            pointInputEnable = ty.getBoolean(R.styleable.KeyboardEditText_pointInputEnable, false)
            shufflePwdKey = ty.getBoolean(R.styleable.KeyboardEditText_shufflePwdKey, false)
            autoHandlerPwdEnable =
                ty.getBoolean(R.styleable.KeyboardEditText_autoHandlerPwdEnable, true)
            ty.recycle()
        }
        onFocusChangeListener = this
        if (context is Activity) {
            // 如果是在 Activity 中，直接用 Activity 的 window 初始化
            initKeyboard(context.window)
            // 并且锚点就是 Activity 的 DecorView
            this.popupAnchorView = context.window.decorView
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }


    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (hasFocus) {
            if (::popupWindow.isInitialized && !popupWindow.isShowing) {
                keyboardView.changeKeyboardType(keyboardType)
                notSystemSoftInput()
                hideInput(context, this)

                //使用正确的锚点来显示 PopupWindow
                popupWindow.showAtLocation(popupAnchorView, Gravity.BOTTOM, 0, 0)
                onKeyboardStatusChangeListener?.onKeyBoardShow()
                if (keyboardTopY > 0) {
                    scrollView()
                }
            }
        } else {
            if (::popupWindow.isInitialized && popupWindow.isShowing) {
                popupWindow.dismiss()
                onKeyboardStatusChangeListener?.onKeyBoardHide()
            }
            if (::scrollableContainer.isInitialized) {
                scrollableContainer.scrollTo(0, 0)
            }
        }
    }

    /**
     *
     * 这样我们才能获取到它所属的 Activity
     */
    fun addDialogWindow(dialog: Dialog) {
        useDialogWindow = true
        initKeyboard(dialog.window!!)
        this.popupAnchorView = dialog.ownerActivity?.window?.decorView ?: dialog.window!!.decorView
    }

    fun addONKeyboardStatusChangeListener(listener: OnKeyboardStatusChangeListener) {
        onKeyboardStatusChangeListener = listener
    }


    private fun initKeyboard(window: Window) {
        currentWindow = window
        scrollableContainer = window.decorView.findViewById(android.R.id.content)
        val popView = LayoutInflater.from(context).inflate(R.layout.pop_keyboard, null)
        keyboardView = popView.findViewById(R.id.view_keyboard)

        ViewCompat.setOnApplyWindowInsetsListener(popView) { _, insets ->
            val navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            popView.updatePadding(bottom = navBarInsets.bottom)
            insets
        }

        addKeyLayoutListener()
        popupWindow = PopupWindow(popView,
                                  ViewGroup.LayoutParams.MATCH_PARENT,
                                  ViewGroup.LayoutParams.WRAP_CONTENT)
        popupWindow.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        popupWindow.animationStyle = R.style.CustomKeyboardAnim
        popupWindow.isOutsideTouchable = false
        popupWindow.isTouchable = true
        popupWindow.isClippingEnabled = true
        popupWindow.setOnDismissListener {
            if (::scrollableContainer.isInitialized) {
                scrollableContainer.scrollTo(0, 0)
            }
        }
        keyboardView.changeKeyboardType(keyboardType)
        keyboardView.setPointInputEnable(pointInputEnable)
        keyboardView.shufflePwdKeyEnable(shufflePwdKey)
        keyboardView.addOnKeyListener(object : CustomKeyboard.OnKeyListener {
            override fun onKeyboardTypeChange(type: Int) {
            }

            override fun onKeyPress(keyValue: Int) {
                val editable = editableText
                val start = selectionStart
                val end = selectionEnd
                if (start == end) {
                    editable.insert(start, keyValue.toChar().toString())
                } else {
                    editable.replace(start, end, keyValue.toChar().toString())
                }
            }

            override fun onKeyDelete() {
                val editable = editableText
                val start = selectionStart
                val end = selectionEnd
                if (start == end) {
                    if (start > 0) {
                        editable.delete(start - 1, start)
                    }
                } else {
                    editable.delete(start, end)
                }
            }

            override fun onPwdStatusChange(pwdHide: Boolean) {
                if (autoHandlerPwdEnable) {
                    val start = selectionStart
                    inputType = if (pwdHide) InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    setSelection(start)
                } else {
                    pwdStatusChange(pwdHide)
                }
            }

            override fun onKeyComplete() {
                clearFocus()
            }
        })
    }


    private fun addKeyLayoutListener() {
        keyboardView.viewTreeObserver?.addOnGlobalLayoutListener(object :
                                                                     ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val location = IntArray(2)
                keyboardView.getLocationOnScreen(location)
                keyboardTopY = location[1]
                if (keyboardTopY > 0 && keyboardTopY < screenHeight) {
                    keyboardView.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                    scrollView()
                }
            }
        })
    }


    private fun hideInput(context: Context, view: View) {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager.isActive) {
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    private fun notSystemSoftInput() {
        showSoftInputOnFocus = false
    }

    private fun scrollView() {
        val location = IntArray(2)
        getLocationInWindow(location)
        val editTextTopY = location[1]
        val editTextBottomY = editTextTopY + height

        if (editTextBottomY <= keyboardTopY) {
            return
        }

        val scrollAmount = editTextBottomY - keyboardTopY
        //确保滚动的是我们定义好的 scrollableContainer
        scrollableContainer.scrollBy(0, scrollAmount)
    }

    open fun pwdStatusChange(isHide: Boolean) {

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (::popupWindow.isInitialized && popupWindow.isShowing) {
            popupWindow.dismiss()
            onKeyboardStatusChangeListener?.onKeyBoardHide()
        }
        // 当视图销毁时，将滚动位置重置
        if (::scrollableContainer.isInitialized) {
            scrollableContainer.scrollTo(0, 0)
        }
    }


    interface OnKeyboardStatusChangeListener {
        fun onKeyBoardShow()
        fun onKeyBoardHide()
    }
}