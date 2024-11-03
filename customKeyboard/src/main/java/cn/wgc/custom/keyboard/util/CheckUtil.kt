package cn.wgc.custom.keyboard.util

import android.text.TextUtils

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
object CheckUtil {
 /**
  * 校验中文名字
  */
 fun checkChineseName(name: String): Boolean {
  return !TextUtils.isEmpty(name) && name.matches("[\\u4E00-\\u9FA5]{2,}(?:·[\\u4E00-\\u9FA5]{2,})*".toRegex())
 }


 fun checkPhone(mobile: String): Boolean {
  return !TextUtils.isEmpty(mobile) && mobile.matches("^[1]\\d{10}$".toRegex())
 }

 /**
  * 15位或者18位不为空的身份号码
  */
 fun checkIDNum(idNo: String?): Boolean {
  var idNum = idNo
  // 校验码列表
  val matchCodes = charArrayOf('1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2')
  // 17位加权因子
  val ratios = intArrayOf(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2)
  val numZero: Int = '0'.code
  val idLen = 17

  if (idNum == null || idNum.isEmpty()) {
   return false
  }
  idNum = idNum.trim { it <= ' ' }
  if (idNum.length != 18) {
   return false
  }
  //如果包含***说明该身份号码已经校验过，
  if (idNum.contains("***")) {
   return true
  }
  // 获取身份号码字符数组
  val idChars = idNum.toCharArray()
  // 获取最后一位（身份证校验码）
  val verifyCode = idChars[idLen]
  // 身份号码第1-17加权和
  var idSum = 0
  for (i in 0 until idLen) {
   val value = idChars[i].toInt() - numZero
   idSum += value * ratios[i]
  }
  // 取余
  val residue = idSum % 11
  return if (residue < 0) {
   false
  } else Character.toUpperCase(verifyCode) == matchCodes[residue]

 }
}