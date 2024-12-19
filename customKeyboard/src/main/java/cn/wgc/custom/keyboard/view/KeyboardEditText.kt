package cn.wgc.custom.keyboard.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.provider.Settings
import android.text.InputType
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatEditText
import cn.wgc.custom.keyboard.R
import cn.wgc.custom.keyboard.util.KeyboardUtil
import cn.wgc.custom.keyboard.util.KeyboardUtil.hasNavigationBar
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

    private lateinit var contentParent: FrameLayout
    private lateinit var keyboardView: CustomKeyboard
    private var keyboardType = LETTER_TO_NUMBER_TYPE
    private lateinit var popupWindow: PopupWindow
    private var pointInputEnable = false
    private var start: Int = 0
    private var locationStartY = 0
    private var popupLocationOffsetY = 0
    private var selfLocationOffsetY = 0
    private var useDialogWindow = false
    private var currentWindow: Window? = null

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

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context,
                                                                                  attrs,
                                                                                  defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet) {
        val ty = context.obtainStyledAttributes(attrs, R.styleable.KeyboardEditText)
        keyboardType = ty.getInt(R.styleable.KeyboardEditText_keyboardType, LETTER_TO_NUMBER_TYPE)
        pointInputEnable = ty.getBoolean(R.styleable.KeyboardEditText_pointInputEnable, false)
        ty.recycle()
        onFocusChangeListener = this
        if (context is Activity) {
            initKeyboard(context.window)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }


    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (hasFocus) {
            if (!popupWindow.isShowing) {
                keyboardView.changeKeyboardType(keyboardType)
                notSystemSoftInput()
                hideInput(context, this)
                //判断是否使用了沉浸式状态栏
                val isImmersive =
                    (currentWindow!!.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) != 0
//                var navigationBarHeight = if (isImmersive && hasNavigationBar(currentWindow!!,
//                                                                              context)
//                ) KeyboardUtil.getNavigationBarHeight(currentWindow!!, context) else 0

                var navigationBarHeight =  KeyboardUtil.getNavigationBarHeight(currentWindow!!, context)
                val offsetY = popupLocationOffsetY + navigationBarHeight
                popupWindow.showAtLocation(contentParent, Gravity.BOTTOM, 0, offsetY)
                if (start > 0) {
                    scrollView()
                }
            }
        } else {
            clearFocus()
            popupWindow.dismiss()
            contentParent.scrollTo(0, 0)
        }
    }


    fun calculatePopupLocationOffsetY(): Int {
        val location = IntArray(2)
        contentParent.getChildAt(0).getLocationInWindow(location)
        val startY = location[1]
        return screenHeight - startY - contentParent.getChildAt(0).height
    }


    fun addDialogWindow(window: Window) {
        useDialogWindow = true
        initKeyboard(window)
    }

    fun setPopupLocationOffsetYOnDialog(offsetY: Int) {
        popupLocationOffsetY = offsetY
    }

    fun setSelfLocationOffsetYOnDialog(selfLocationOffsetY: Int) {
        this.selfLocationOffsetY = selfLocationOffsetY
    }

    private fun initKeyboard(window: Window) {
        currentWindow = window
        val decorView = window.decorView
        contentParent = decorView.findViewById(android.R.id.content)
        val popView = LayoutInflater.from(context).inflate(R.layout.pop_keyboard, null)
        keyboardView = popView.findViewById(R.id.view_keyboard)
        //监听键盘和自己布局事件，用于点击键盘外部隐藏键盘
        addKeyLayoutListener()
        popupWindow = PopupWindow(popView,
                                  ViewGroup.LayoutParams.MATCH_PARENT,
                                  ViewGroup.LayoutParams.WRAP_CONTENT)
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.GRAY))
        popupWindow.animationStyle = R.style.CustomKeyboardAnim
        popupWindow.isOutsideTouchable = false
        popupWindow.isTouchable = true
        popupWindow.isClippingEnabled = false
        popupWindow.setOnDismissListener {
            clearFocus()
        }
        keyboardView.changeKeyboardType(keyboardType)
        keyboardView.setPointInputEnable(pointInputEnable)
        keyboardView.addOnKeyListener(object : CustomKeyboard.OnKeyListener {
            override fun onKeyboardTypeChange(type: Int) {
            }

            override fun onKeyPress(keyValue: Int) {

                editableText.insert(selectionStart, keyValue.toChar().toString())
            }

            override fun onKeyDelete() {
                if (selectionStart == 0) return
                editableText.delete(selectionStart - 1, selectionStart)
            }

            override fun onPwdStatusChange(pwdHide: Boolean) {
                val start = selectionStart
                inputType = if (pwdHide) InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                setSelection(start)
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
                start = location[1]
                if (start in 1 until screenHeight) {
                    keyboardView.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                    scrollView()
                }
            }
        })
        viewTreeObserver.addOnGlobalLayoutListener(object :
                                                       ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                //处理键盘自动弹出的问题
                contentParent.requestFocus()
                contentParent.requestFocusFromTouch()
                val location = IntArray(2)
                getLocationInWindow(location)
                locationStartY = location[1]
                viewTreeObserver?.removeOnGlobalLayoutListener(this)
                //计算popupWindow显示位置的偏移量,主要考虑系统导航栏,如果是在dialog中使用需要调用KeyboardUtil计算
                //通过调用setPopupLocationOffsetYOnDialog方法设置
//                if (!useDialogWindow) {
//                    popupLocationOffsetY = calculatePopupLocationOffsetY()
//                }
            }
        })
    }


    /**
     * 强制隐藏输入法键盘
     */
    private fun hideInput(context: Context, view: View) {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager.isActive) {
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    /**
     * 屏蔽系统输入法
     */
    private fun notSystemSoftInput() {
        try {
            val cls = EditText::class.java
            val setShowSoftInputOnFocus =
                cls.getMethod("setShowSoftInputOnFocus", Boolean::class.javaPrimitiveType!!)
            setShowSoftInputOnFocus.isAccessible = true
            setShowSoftInputOnFocus.invoke(this, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun scrollView() {
        val location = IntArray(2)
        getLocationInWindow(location)
        locationStartY = location[1] + selfLocationOffsetY
        if (locationStartY > start - height) {
            val height = locationStartY - start + height
            contentParent.scrollTo(0, height)
        }
    }

    private fun dp2px(dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    private fun sp2px(spValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }


}