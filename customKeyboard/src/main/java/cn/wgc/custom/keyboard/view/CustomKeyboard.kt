package cn.wgc.custom.keyboard.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import cn.wgc.custom.keyboard.R
import cn.wgc.custom.keyboard.entity.KeyEntity
import cn.wgc.custom.keyboard.util.KeyUtil

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
@Suppress("NAME_SHADOWING")
class CustomKeyboard : View {

    companion object {

        const val NUMBER_TYPE = 0 //数字键盘类型
        const val ID_CARD_TYPE = 1 //身份证键盘
        const val NUMBER_TO_LETTER_TYPE = 2 //数字转英语键盘
        const val LETTER_TO_NUMBER_TYPE = 3 //英语转数字键盘
        const val PWD_TYPE = 4 //密码键盘类型
        const val KEYBOARD_LINE = 4
    }


    private val numKeys by lazy { KeyUtil.generateNumKeyEntities() }
    private val idNumKeys by lazy { KeyUtil.generateIdNumKeyEntities() }
    private val num2LetterKeys by lazy { KeyUtil.generateNum2LetterKeyEntities() }
    private val letter2NumKeys by lazy { KeyUtil.generateLetter2NumKeyEntities() }
    private val numPwdKeys by lazy { KeyUtil.generatePwdKeyEntities() }

    //    private val shuffleNumPwdKeys by lazy { KeyUtil.generateShufflePwdKeyEntities() }
    private var keys: ArrayList<KeyEntity> = arrayListOf()


    private var numberKeyRect = arrayListOf<RectF>()
    private var keyTextColor: Int = 0
    private var keyNormalColor: Int = 0
    private var keyPressColor: Int = 0

    private var keyTextSize = 0f
    private var keyPadding = 0f
    private var keyDrawableSize = 0f
    private var keyTopAndBottomPadding = 0f
    private var keyboardType = LETTER_TO_NUMBER_TYPE


    //通用键高
    private var keyHeight = 0f

    //通用键宽
    private var keyWidth = 0f


    private val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var onKeyListener: OnKeyListener? = null
    private var downPoint: PointF? = null

    private var capitalEnable = true
    private var totalKeyChange = false
    private var capitalKeyChange = false
    private var pointInputEnable = false
    private var pwdHide = true
    private var shufflePwdKey = false

    private var currentKeyEntity: KeyEntity? = null
    private var currentKeyDownPosition = -1

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
        val ty = context.obtainStyledAttributes(attrs, R.styleable.CustomKeyboard)
        keyTextColor = ty.getColor(R.styleable.CustomKeyboard_keyTextColor, Color.BLACK)
        keyNormalColor = ty.getColor(R.styleable.CustomKeyboard_keyNormalColor, Color.WHITE)
        keyPressColor = ty.getColor(R.styleable.CustomKeyboard_keyPressColor, Color.GRAY)
        keyTextSize = ty.getDimension(R.styleable.CustomKeyboard_keyTextSize, sp2px(24F).toFloat())
        keyPadding = ty.getDimension(R.styleable.CustomKeyboard_keyPadding, dp2px(1.5F).toFloat())
        keyDrawableSize =
            ty.getDimension(R.styleable.CustomKeyboard_keyDrawableSize, dp2px(18F).toFloat())
        keyTopAndBottomPadding = ty.getDimension(R.styleable.CustomKeyboard_keyTopAndBottomPadding,
                                                 dp2px(1.5F).toFloat())
        ty.recycle()
        rectPaint.color = Color.WHITE
        textPaint.textSize = keyTextSize
        generateKeys()

    }

    private fun generateKeys() {
        keys = when (keyboardType) {
            NUMBER_TYPE -> numKeys
            ID_CARD_TYPE -> idNumKeys
            NUMBER_TO_LETTER_TYPE -> num2LetterKeys
            LETTER_TO_NUMBER_TYPE -> letter2NumKeys
            PWD_TYPE -> if (!shufflePwdKey) numPwdKeys else KeyUtil.generateShufflePwdKeyEntities()
            else -> letter2NumKeys
        }
    }

    fun shufflePwdKeyEnable(shufflePwdKey: Boolean) {
        this.shufflePwdKey = shufflePwdKey
    }

    fun setPointInputEnable(enable: Boolean) {
        if (enable) {
            val keyEntity = keys.find { it.keyValue == -50 }
            if (keyEntity == null) return
            keyEntity.keyValue = 46
            keyEntity.keyName = "."
        } else {
            val keyEntity = keys.find { it.keyValue == 46 }
            if (keyEntity == null) return
            keyEntity.keyValue = -50
            keyEntity.keyName = "hide"
        }
        pointInputEnable = enable
    }


    fun changeKeyboardType(type: Int) {
        Log.d("wgc", "changeKeyboardType222")
        keyboardType = type
        generateKeys()
        requestLayout()
    }

    private fun refreshKeyboard() {
        generateKeys()
        requestLayout()
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val h = bottom - top
        val w = right - left
        keyHeight =
            (h - keyPadding * (KEYBOARD_LINE - 1) - keyTopAndBottomPadding * 2) / KEYBOARD_LINE

        when (keyboardType) {
            //数字键盘,身份证键盘3列,数字密码键盘
            NUMBER_TYPE, ID_CARD_TYPE, PWD_TYPE, NUMBER_TO_LETTER_TYPE -> {
                keyWidth = (w - keyPadding * (3 - 1)) / 3
                //计算通用含数字键盘的矩阵
                calculateCommonNumKeyRect()
            }
            //字母键盘一行按10个标准字母键计算
            else -> {
                keyWidth = (w - keyPadding * (10 - 1)) / 10
                calculateLetterKeyRect()
            }
        }
    }

    private fun calculateCommonNumKeyRect() {
        numberKeyRect.clear()
        for (i in 0 until keys.size) {
            val xPosition = i % 3
            val yPosition = i / 3
            val left = (keyWidth + keyPadding) * xPosition
            val right = keyWidth * (xPosition + 1) + xPosition * keyPadding
            val top = keyTopAndBottomPadding + yPosition * keyPadding + yPosition * keyHeight
            val bottom =
                keyTopAndBottomPadding + yPosition * keyPadding + (yPosition + 1) * keyHeight
            numberKeyRect.add(RectF(left, top, right, bottom))
        }
    }


    private fun calculateLetterKeyRect() {
        numberKeyRect.clear()
        for (i in 0 until keys.size) {
            when {
                i < 10 -> {
                    val xPosition = i % 10
                    val yPosition = 0
                    val left = (keyWidth + keyPadding) * xPosition
                    val right = keyWidth * (xPosition + 1) + xPosition * keyPadding
                    val top =
                        keyTopAndBottomPadding + yPosition * keyPadding + yPosition * keyHeight
                    val bottom =
                        keyTopAndBottomPadding + yPosition * keyPadding + (yPosition + 1) * keyHeight
                    numberKeyRect.add(RectF(left, top, right, bottom))
                }

                i in 10..18 -> {
                    val xPosition = (i - 10) % 9
                    val yPosition = 1
                    val left = keyWidth / 2 + (keyWidth + keyPadding) * xPosition
                    val right = keyWidth / 2 + keyWidth * (xPosition + 1) + xPosition * keyPadding
                    val top =
                        keyTopAndBottomPadding + yPosition * keyPadding + yPosition * keyHeight
                    val bottom =
                        keyTopAndBottomPadding + yPosition * keyPadding + (yPosition + 1) * keyHeight
                    numberKeyRect.add(RectF(left, top, right, bottom))
                }

                i in 19..27 -> {
                    val xPosition = (i - 19) % 9
                    val yPosition = 2

                    var left: Float
                    var right: Float
                    when (xPosition) {
                        8 -> {
                            left = (keyWidth + keyPadding) * xPosition
                            right = width.toFloat()
                        }

                        else -> {
                            left = (keyWidth + keyPadding) * xPosition
                            right = keyWidth * (xPosition + 1) + xPosition * keyPadding
                        }
                    }

                    val top =
                        keyTopAndBottomPadding + yPosition * keyPadding + yPosition * keyHeight
                    val bottom =
                        keyTopAndBottomPadding + yPosition * keyPadding + (yPosition + 1) * keyHeight
                    numberKeyRect.add(RectF(left, top, right, bottom))
                }

                //切换数字键
                i == 28 -> {
                    val left = 0f
                    val right = keyWidth / 2 + keyWidth * 2 + keyPadding
                    val top = keyTopAndBottomPadding + 3 * keyPadding + 3 * keyHeight
                    val bottom = keyTopAndBottomPadding + 3 * keyPadding + 4 * keyHeight
                    numberKeyRect.add(RectF(left, top, right, bottom))
                }
                //空格键
                i == 29 -> {
                    val left = keyWidth / 2 + keyWidth * 2 + 2 * keyPadding
                    val right = keyWidth / 2 + keyWidth * 7 + 6 * keyPadding
                    val top = keyTopAndBottomPadding + 3 * keyPadding + 3 * keyHeight
                    val bottom = keyTopAndBottomPadding + 3 * keyPadding + 4 * keyHeight
                    numberKeyRect.add(RectF(left, top, right, bottom))
                }
                //删除键
                i == 30 -> {
                    val left = keyWidth / 2 + keyWidth * (7) + 7 * keyPadding
                    val right = width.toFloat()
                    val top = keyTopAndBottomPadding + 3 * keyPadding + 3 * keyHeight
                    val bottom = keyTopAndBottomPadding + 3 * keyPadding + 4 * keyHeight
                    numberKeyRect.add(RectF(left, top, right, bottom))
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawNumberKeys(canvas)
        super.onDraw(canvas)
    }

    private fun changeCapital(capitalEnable: Boolean) {
        val lowercase = "abcdefghijklmnopqrstuvwxyz"
        keys.forEach {
            if (it.keyName.isNotEmpty() && lowercase.indexOf(it.keyName.lowercase()) != -1) {
                if (capitalEnable) {
                    it.keyName = it.keyName.uppercase()
                    it.keyValue -= 32
                } else {
                    it.keyName = it.keyName.lowercase()
                    it.keyValue += 32
                }
            }
        }
        this.capitalEnable = capitalEnable
    }

    private fun drawNumberKeys(canvas: Canvas) {
        Log.d("wgc", "ACTION__UP downPoint $downPoint")
        for (i in 0 until keys.size) {
            //绘制键盘框
            val rectF = numberKeyRect[i]
            val contains = KeyUtil.rectContainsPoint(downPoint, rectF)

            val keyEntity = keys[i]
            if (contains) {
                currentKeyEntity = keyEntity
                currentKeyDownPosition = i
            }

            rectPaint.color = if (contains) keyPressColor else keyNormalColor
            canvas.drawRect(rectF.left, rectF.top, rectF.right, rectF.bottom, rectPaint)

            //绘制键盘文字和图标
            val cx = rectF.left + (rectF.right - rectF.left) / 2
            val cy = rectF.top + (rectF.bottom - rectF.top) / 2


            when (keyEntity.keyValue) {
                -1 -> {
                    //切换大小写图标宽高比2:3
                    val path = Path()
                    path.moveTo(cx, cy - keyDrawableSize / 2)
                    path.lineTo(cx - keyDrawableSize / 2, cy)
                    path.lineTo(cx - keyDrawableSize / 4, cy)
                    path.lineTo(cx - keyDrawableSize / 4, cy + keyDrawableSize / 2)
                    path.lineTo(cx + keyDrawableSize / 4, cy + keyDrawableSize / 2)
                    path.lineTo(cx + keyDrawableSize / 4, cy)
                    path.lineTo(cx + keyDrawableSize / 2, cy)
                    path.close()
                    textPaint.style =
                        if (capitalEnable) Paint.Style.FILL_AND_STROKE else Paint.Style.STROKE
                    textPaint.strokeWidth = dp2px(2f).toFloat()
                    canvas.drawPath(path, textPaint)
                }

                -4399 -> {
                    val path = Path()
                    //绘制密码可图标
                    path.moveTo(cx - keyDrawableSize / 4 * 3, cy)
                    path.quadTo(cx, cy - keyDrawableSize, cx + keyDrawableSize / 4 * 3, cy)
                    path.quadTo(cx, cy + keyDrawableSize, cx - keyDrawableSize / 4 * 3, cy)
                    path.close()
                    if (pwdHide) {
                        path.moveTo(cx + keyDrawableSize / 2, cy - keyDrawableSize / 2)
                        path.lineTo(cx - keyDrawableSize / 2, cy + keyDrawableSize / 2)
                    }
                    textPaint.style = Paint.Style.STROKE
                    textPaint.strokeWidth = dp2px(2f).toFloat()
                    canvas.drawPath(path, textPaint)
                    canvas.drawCircle(cx, cy, keyDrawableSize / 4, textPaint)
                }

                -5 -> {
                    //删除图标宽高比3:2
                    val path = Path()
                    path.moveTo(cx - keyDrawableSize / 3 * 2, cy)
                    path.lineTo(cx - keyDrawableSize / 3, cy - keyDrawableSize / 2)
                    path.lineTo(cx + keyDrawableSize / 3 * 2, cy - keyDrawableSize / 2)
                    path.lineTo(cx + keyDrawableSize / 3 * 2, cy + keyDrawableSize / 2)
                    path.lineTo(cx - keyDrawableSize / 3, cy + keyDrawableSize / 2)
                    path.close()
                    path.moveTo(cx - keyDrawableSize / 4, cy - keyDrawableSize / 3)
                    path.lineTo(cx + keyDrawableSize / 2, cy + keyDrawableSize / 3)
                    path.moveTo(cx - keyDrawableSize / 4, cy + keyDrawableSize / 3)
                    path.lineTo(cx + keyDrawableSize / 2, cy - keyDrawableSize / 3)
                    textPaint.style = Paint.Style.STROKE
                    textPaint.strokeWidth = dp2px(2f).toFloat()
                    canvas.drawPath(path, textPaint)

                }

                -50 -> {
                    //隐藏图标宽高比4:3
                    val rectF = RectF(cx - keyDrawableSize / 3 * 4 / 2,
                                      cy - keyDrawableSize / 2,
                                      cx + keyDrawableSize / 3 * 4 / 2,
                                      cy + keyDrawableSize / 2)
                    textPaint.style = Paint.Style.STROKE
                    textPaint.strokeWidth = dp2px(2f).toFloat()
                    canvas.drawRoundRect(rectF, keyDrawableSize / 5, keyDrawableSize / 5, textPaint)
                    val path = Path()
                    path.moveTo(cx - keyDrawableSize / 2, cy - keyDrawableSize / 4)
                    path.lineTo(cx, cy + keyDrawableSize / 4)
                    path.lineTo(cx + keyDrawableSize / 2, cy - keyDrawableSize / 4)
                    canvas.drawPath(path, textPaint)
                }

                else -> {
                    textPaint.style = Paint.Style.FILL
                    //绘制文字
                    val text = keyEntity.keyName
                    val rect = Rect()
                    textPaint.getTextBounds(text, 0, text.length, rect)
                    //文字宽
                    val textWidth = rect.width()
                    //文字高
                    val textHeight = rect.height()
                    canvas.drawText(text, (cx - textWidth / 2), (cy + textHeight / 2), textPaint)
                }
            }
        }
    }

    private fun keyCallback(keyValue: Int) {

        when (keyValue) {
            46 -> {
                if (pointInputEnable) onKeyListener?.onKeyPress(keyValue)
            }

            -1 -> {
                capitalKeyChange = true
            }

            -2 -> {
                totalKeyChange = true
                keyboardType = NUMBER_TO_LETTER_TYPE
                onKeyListener?.onKeyboardTypeChange(NUMBER_TO_LETTER_TYPE)
            }

            -4 -> {
                totalKeyChange = true
                keyboardType = LETTER_TO_NUMBER_TYPE
                onKeyListener?.onKeyboardTypeChange(LETTER_TO_NUMBER_TYPE)
            }

            -5 -> onKeyListener?.onKeyDelete()

            -50 -> {
                onKeyListener?.onKeyComplete()
            }

            -4399 -> {
                pwdHide = !pwdHide
                onKeyListener?.onPwdStatusChange(pwdHide)
            }

            else -> onKeyListener?.onKeyPress(keyValue)
        }
    }


    private fun dp2px(dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    private fun sp2px(sp: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (sp * fontScale + 0.5f).toInt()
    }


    private var lastTime = 0L
    private var downTime = 0L

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {


        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downTime = event.downTime
                lastTime = 0L
                downPoint = PointF(event.x, event.y)
                //计算当前位置按键，必需提前计算，初次按下时会有问题
                calculateCurrentKey()
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                if (currentKeyDownPosition != -1) {
                    val downTime = event.downTime
                    val eventTime = event.eventTime
                    if (lastTime == 0L) lastTime = eventTime
                    val x = event.x
                    val y = event.y
                    val downRect = numberKeyRect[currentKeyDownPosition]
                    val contains = KeyUtil.rectContainsPoint(PointF(x, y), downRect)
                    if (contains && eventTime - lastTime > 100L) {
                        lastTime = eventTime
                        if (lastTime - downTime >= 800L) {
                            currentKeyEntity?.let { keyCallback(it.keyValue) }
                        }

                    }
                }

            }

            MotionEvent.ACTION_UP -> {
                downPoint = null
                currentKeyDownPosition = -1
                currentKeyEntity?.let { keyCallback(it.keyValue) }
                if (totalKeyChange) {
                    totalKeyChange = false
                    refreshKeyboard()
                }

                if (capitalKeyChange) {
                    capitalKeyChange = false
                    changeCapital(!capitalEnable)
                }
                invalidate()
            }
        }
        return true
    }

   private fun calculateCurrentKey() {
        for (i in 0 until keys.size) {
            //绘制键盘框
            val rectF = numberKeyRect[i]
            val contains = KeyUtil.rectContainsPoint(downPoint, rectF)

            val keyEntity = keys[i]
            if (contains) {
                currentKeyEntity = keyEntity
                currentKeyDownPosition = i
            }
        }
    }


    fun addOnKeyListener(listener: OnKeyListener) {
        onKeyListener = listener
    }

    interface OnKeyListener {
        fun onKeyboardTypeChange(type: Int)
        fun onKeyPress(keyValue: Int)
        fun onKeyDelete()
        fun onPwdStatusChange(pwdHide: Boolean)
        fun onKeyComplete()
    }
}

