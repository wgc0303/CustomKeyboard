package cn.wgc.keyboard.demo

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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

@Suppress("DEPRECATION")
@SuppressLint("MissingInflatedId")
class TestDialog(context: Activity) : BaseDialog<DialogTestBinding>(context, R.style.dialogStyle) {

//    override fun setOnShowListener(listener: DialogInterface.OnShowListener?) {
//        super.setOnShowListener(listener)
//        KeyboardUtil.handDialogKeyboardStatus(this, binding.rlRoot, true, binding.etLetter, binding.etIdNumber, binding.etNumber)
//    }


/*    *//**
     *
     * 重写show 屏蔽底部导航栏
     *//*
    override fun show() {
        this.window!!.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        super.show()
        fullScreenImmersive(window!!.decorView)
        this.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun fullScreenImmersive(view: View) {
        val uiOptions =
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_FULLSCREEN)
        view.systemUiVisibility = uiOptions
    }*/


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
        dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        KeyboardUtil.handDialogKeyboardStatus(this, binding.root, false, binding.etLetter, binding.etIdNumber, binding.etNumber)
    }

    override fun initData() {
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        KeyboardUtil.dispatchTouchEvent(ev, this)
        return super.dispatchTouchEvent(ev)
    }
}