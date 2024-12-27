package cn.wgc.custom.keyboard.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import cn.wgc.custom.keyboard.R
import cn.wgc.custom.keyboard.util.CheckUtil


/**
 * <pre>
 *
 *     author : wgc
 *     time   :  2024/11/03
 *     desc   :
 *     version: 1.0
 *
 * </pre>
 */
class CheckKeyboardEditText : KeyboardEditText {

    companion object {
        const val ID_NUMBER: Int = 0
        const val NAME_MODE: Int = 1
        const val PHONE_MODE: Int = 2
        const val PHONE_CODE_MODE: Int = 3
    }

    private var mode = 0
    private var passColor = 0
    private var errorColor = 0
    private var checkCodeLen = 4
    private var checkPhoneLen = 11
    private var checkNameLen = 15
    private var checkIdNumLen = 18
    private var clearDrawable: Drawable? = null //删除按钮
    private var onCheckListener: OnCheckListener? = null
    private var hasViewFocus = false
    private var drawableEnable = false
    private var isArrowClear = true
    private var isPhone = false


    private var textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable) {
            checkInfo(s)
            if (isArrowClear) {
                setClearIconVisible(s.toString().isNotEmpty())
            }
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
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CheckKeyboardEditText)
        mode = typedArray.getInt(R.styleable.CheckKeyboardEditText_checkMode, -1)
        checkCodeLen =
            typedArray.getInt(R.styleable.CheckKeyboardEditText_checkCodeLen, checkCodeLen)
        checkPhoneLen =
            typedArray.getInt(R.styleable.CheckKeyboardEditText_checkPhoneLen, checkPhoneLen)
        checkNameLen =
            typedArray.getInt(R.styleable.CheckKeyboardEditText_checkNameLen, checkNameLen)
        checkIdNumLen =
            typedArray.getInt(R.styleable.CheckKeyboardEditText_checkIdNumLen, checkIdNumLen)
        passColor = typedArray.getColor(R.styleable.CheckKeyboardEditText_checkPassColor,
                                        ContextCompat.getColor(context,
                                                               R.color.kb_text_default_dark))
        errorColor = typedArray.getColor(R.styleable.CheckKeyboardEditText_checkErrorColor,
                                         ContextCompat.getColor(context,
                                                                R.color.kb_text_error_color))
        drawableEnable =
            typedArray.getBoolean(R.styleable.CheckKeyboardEditText_drawableEnable, true)

        addTextChangedListener(textWatcher)
        typedArray.recycle()
        setTextMaxLength()
        if (!drawableEnable) return
        // 获取EditText的DrawableRight,假如没有设置我们就使用默认的图片,获取图片的顺序是左上右下（ic_num_zero,ic_num_one,ic_num_two,ic_num_three,）
        clearDrawable = compoundDrawables[2]
        if (clearDrawable == null) {
            clearDrawable = ContextCompat.getDrawable(context, R.drawable.kb_clear)
        }

        clearDrawable?.setBounds(0,
                                 0,
                                 clearDrawable!!.intrinsicWidth,
                                 clearDrawable!!.intrinsicHeight)
        // 默认设置隐藏图标
        setClearIconVisible(false)
        // 设置焦点改变的监听
        // 设置输入框里面内容发生改变的监听
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            if (isArrowClear) {
                if (compoundDrawables[2] != null) {
                    val x = event.x.toInt()
                    val y = event.y.toInt()
                    val rect = compoundDrawables[2].bounds
                    val height = rect.height()
                    val distance = (getHeight() - height) / 2
                    val isInnerWidth = x > (width - totalPaddingRight) && x < (width - paddingRight)
                    val isInnerHeight = y > distance && y < (distance + height)
                    if (isInnerWidth && isInnerHeight) {
                        this.editableText.clear()
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        this.hasViewFocus = hasFocus
        if (hasFocus) {
            setClearIconVisible(getText().toString().isNotEmpty());
        } else {
            setClearIconVisible(false);
        }
        super.onFocusChange(v, hasFocus)
    }


    fun setClearIconVisible(visible: Boolean) {
        if (!drawableEnable || (visible && !hasViewFocus)) return
        val right = if (visible) clearDrawable else null
        setCompoundDrawables(compoundDrawables[0],
                             compoundDrawables[1],
                             right,
                             compoundDrawables[3])
    }


    private fun checkInfo(s: Editable) {
        when (mode) {
            ID_NUMBER -> {
                val idCard18: Boolean = CheckUtil.checkIDNum(s.toString())
                setFontColor(idCard18)
                onCheckListener?.checkIdNumResult(idCard18)
            }

            NAME_MODE -> {
                val isUsername: Boolean = CheckUtil.checkChineseName(s.toString())
                setFontColor(isUsername)
                onCheckListener?.checkNameResult(isUsername)
            }

            PHONE_MODE -> {
                isPhone = CheckUtil.checkPhone(s.toString())
                setFontColor(isPhone)
                onCheckListener?.checkPhoneResult(isPhone)
            }

            PHONE_CODE_MODE -> {
                setFontColor(s.toString().length == checkCodeLen)
                onCheckListener?.checkPhoneCodeResult(s.toString().length == checkCodeLen)
            }

        }
    }

    fun setDrawableEnable(drawableEnable: Boolean) {
        this.drawableEnable = drawableEnable
    }

    private fun setFontColor(isCheckPass: Boolean) {
        if (isCheckPass) setTextColor(passColor)
        else setTextColor(errorColor)
    }

    private fun setTextMaxLength() {
        when (mode) {
            ID_NUMBER -> filters = arrayOf<InputFilter>(LengthFilter(checkIdNumLen))
            NAME_MODE -> filters = arrayOf<InputFilter>(LengthFilter(checkNameLen))
            PHONE_MODE -> filters = arrayOf<InputFilter>(LengthFilter(checkPhoneLen))
            PHONE_CODE_MODE -> filters = arrayOf<InputFilter>(LengthFilter(checkCodeLen))
        }
    }

    fun setOnCheckListener(onCheckListener: OnCheckListener) {
        this.onCheckListener = onCheckListener
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        onCheckListener = null
        removeTextChangedListener(textWatcher)
    }

}