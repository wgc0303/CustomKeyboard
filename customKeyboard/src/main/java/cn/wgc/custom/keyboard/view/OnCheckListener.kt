package cn.wgc.custom.keyboard.view

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
interface OnCheckListener {
    fun checkIdNumResult(isIdNum: Boolean) {

    }

    fun checkPhoneResult(isPhone: Boolean) {

    }

    fun checkPhoneCodeResult(isPhoneCode: Boolean) {

    }

    fun checkNameResult(isName: Boolean) {

    }
}