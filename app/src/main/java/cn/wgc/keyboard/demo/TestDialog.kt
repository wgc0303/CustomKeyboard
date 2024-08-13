package cn.wgc.keyboard.demo

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import cn.wgc.custom.keyboard.util.KeyboardUtil
import cn.wgc.keyboard.demo.base.BaseDialog
import cn.wgc.keyboard.demo.databinding.DialogTestBinding


/**
 * <pre>
 *
 *     author : wgc
 *     time   : 2024/07/29
 *     desc   :
 *     version: 1.0
 *
 * </pre>
 */

@SuppressLint("MissingInflatedId")
class TestDialog(context: Activity) : BaseDialog<DialogTestBinding>(context, R.style.dialogStyle) {

//    override fun setOnShowListener(listener: DialogInterface.OnShowListener?) {
//        super.setOnShowListener(listener)
//        KeyboardUtil.handDialogKeyboardStatus(this, binding.rlRoot, true, binding.etLetter, binding.etIdNumber, binding.etNumber)
//    }

    override fun loadViewBinding(): DialogTestBinding {
        return DialogTestBinding.inflate(layoutInflater)
    }

    override fun initListener() {
    }

    override fun initView() {
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        val dialogWindow = window
        val lp = dialogWindow!!.attributes
        lp.gravity = Gravity.CENTER
        dialogWindow.attributes = lp
        dialogWindow.statusBarColor =
            ContextCompat.getColor(context, R.color.dialog_status_bar_color)
        dialogWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                               ViewGroup.LayoutParams.WRAP_CONTENT)
        KeyboardUtil.handDialogKeyboardStatus(this,
                                              binding.root,
                                              false,
                                              binding.etLetter,
                                              binding.etIdNumber,
                                              binding.etNumber)
    }

    override fun initData() {
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        KeyboardUtil.dispatchTouchEvent(ev, this)
        return super.dispatchTouchEvent(ev)
    }
}