package cn.wgc.custom.keyboard.util

import android.content.Context
import android.graphics.PointF
import android.graphics.RectF
import cn.wgc.custom.keyboard.entity.KeyEntity

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
object KeyUtil {

    //生成带隐藏数字键盘keys
    fun generateNumKeyEntities(): ArrayList<KeyEntity> {
        return arrayListOf<KeyEntity>().apply {
            add(KeyEntity(49, "1"))
            add(KeyEntity(50, "2"))
            add(KeyEntity(51, "3"))
            add(KeyEntity(52, "4"))
            add(KeyEntity(53, "5"))
            add(KeyEntity(54, "6"))
            add(KeyEntity(55, "7"))
            add(KeyEntity(56, "8"))
            add(KeyEntity(57, "9"))
//            add(KeyEntity(46, "."))
            add(KeyEntity(-50, "hide"))
            add(KeyEntity(48, "0"))
            add(KeyEntity(-5, "delete"))
        }
    }

    //生成数字密码键盘keys
    fun generatePwdKeyEntities(): ArrayList<KeyEntity> {
        return arrayListOf<KeyEntity>().apply {
            add(KeyEntity(49, "1"))
            add(KeyEntity(50, "2"))
            add(KeyEntity(51, "3"))
            add(KeyEntity(52, "4"))
            add(KeyEntity(53, "5"))
            add(KeyEntity(54, "6"))
            add(KeyEntity(55, "7"))
            add(KeyEntity(56, "8"))
            add(KeyEntity(57, "9"))
            add(KeyEntity(-4399, "show"))
            add(KeyEntity(48, "0"))
            add(KeyEntity(-5, "delete"))
        }
    }

    //生成打乱的数字密码键盘keys
    fun generateShufflePwdKeyEntities(): ArrayList<KeyEntity> {
        val temp = arrayListOf<KeyEntity>().apply {
            add(KeyEntity(49, "1"))
            add(KeyEntity(50, "2"))
            add(KeyEntity(51, "3"))
            add(KeyEntity(52, "4"))
            add(KeyEntity(53, "5"))
            add(KeyEntity(54, "6"))
            add(KeyEntity(55, "7"))
            add(KeyEntity(56, "8"))
            add(KeyEntity(57, "9"))
            add(KeyEntity(48, "0"))
        }
        temp.shuffle()
        val result = arrayListOf<KeyEntity>()
        for (i in 0 until temp.size) {
            result.add(temp[i])
            if (i == temp.size - 1) {
                result.add(KeyEntity(-5, "delete"))
            } else if (i == temp.size - 2) {
                result.add(KeyEntity(-4399, "show"))
            }
        }
        return result
    }

    //生成身份证键盘keys
    fun generateIdNumKeyEntities(): ArrayList<KeyEntity> {
        return arrayListOf<KeyEntity>().apply {
            add(KeyEntity(49, "1"))
            add(KeyEntity(50, "2"))
            add(KeyEntity(51, "3"))
            add(KeyEntity(52, "4"))
            add(KeyEntity(53, "5"))
            add(KeyEntity(54, "6"))
            add(KeyEntity(55, "7"))
            add(KeyEntity(56, "8"))
            add(KeyEntity(57, "9"))
            add(KeyEntity(88, "X"))
            add(KeyEntity(48, "0"))
            add(KeyEntity(-5, "delete"))
        }
    }

    //生成可切换字母的数字键盘keys
    fun generateNum2LetterKeyEntities(): ArrayList<KeyEntity> {
        return arrayListOf<KeyEntity>().apply {
            add(KeyEntity(49, "1"))
            add(KeyEntity(50, "2"))
            add(KeyEntity(51, "3"))
            add(KeyEntity(52, "4"))
            add(KeyEntity(53, "5"))
            add(KeyEntity(54, "6"))
            add(KeyEntity(55, "7"))
            add(KeyEntity(56, "8"))
            add(KeyEntity(57, "9"))
            add(KeyEntity(-4, "ABC"))
            add(KeyEntity(48, "0"))
            add(KeyEntity(-5, "delete"))
        }
    }


    //生成可切换数字的字母键盘keys
    fun generateLetter2NumKeyEntities(): ArrayList<KeyEntity> {
        return arrayListOf<KeyEntity>().apply {
            add(KeyEntity(81, "Q"))
            add(KeyEntity(87, "W"))
            add(KeyEntity(69, "E"))
            add(KeyEntity(82, "R"))
            add(KeyEntity(84, "T"))
            add(KeyEntity(89, "Y"))
            add(KeyEntity(85, "U"))
            add(KeyEntity(73, "I"))
            add(KeyEntity(79, "O"))
            add(KeyEntity(80, "P"))
            add(KeyEntity(65, "A"))
            add(KeyEntity(83, "S"))
            add(KeyEntity(68, "D"))
            add(KeyEntity(70, "F"))
            add(KeyEntity(71, "G"))
            add(KeyEntity(72, "H"))
            add(KeyEntity(74, "J"))
            add(KeyEntity(75, "K"))
            add(KeyEntity(76, "L"))
            add(KeyEntity(-1, "UP"))
            add(KeyEntity(90, "Z"))
            add(KeyEntity(88, "X"))
            add(KeyEntity(67, "C"))
            add(KeyEntity(86, "V"))
            add(KeyEntity(66, "B"))
            add(KeyEntity(78, "N"))
            add(KeyEntity(77, "M"))
            add(KeyEntity(-5, "delete"))
            add(KeyEntity(-2, "123"))
            add(KeyEntity(32, "space"))
//            add(KeyEntity(46, "."))
            add(KeyEntity(-50, "hide"))
        }
    }

    fun rectContainsPoint(point: PointF?, rect: RectF): Boolean {
        if (point == null) return false
        return (point.x >= rect.left && point.x <= rect.right && point.y >= rect.top && point.y <= rect.bottom)
    }


    fun dp2px(context: Context, dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun sp2px(context: Context, spValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }


}