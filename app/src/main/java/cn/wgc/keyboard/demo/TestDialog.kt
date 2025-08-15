package cn.wgc.keyboard.demo

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import cn.wgc.custom.keyboard.util.KeyboardUtil
import cn.wgc.keyboard.demo.base.BaseDialog
import cn.wgc.keyboard.demo.databinding.DialogTestBinding
import androidx.core.graphics.drawable.toDrawable
import cn.wgc.custom.keyboard.view.KeyboardEditText


@SuppressLint("MissingInflatedId")
class TestDialog(context: Activity) : BaseDialog<DialogTestBinding>(context, R.style.dialogStyle) {

    override fun loadViewBinding(): DialogTestBinding {
        return DialogTestBinding.inflate(layoutInflater)
    }

    override fun initListener() {
    }

    override fun initView() {
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        val dialogWindow = window!!
        val lp = dialogWindow.attributes
        lp.gravity = Gravity.CENTER
        dialogWindow.attributes = lp
        dialogWindow.statusBarColor =
            ContextCompat.getColor(context, R.color.dialog_status_bar_color)
        dialogWindow.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                               ViewGroup.LayoutParams.MATCH_PARENT)

        //这样它们就能自我管理键盘的显示和滚动。
        binding.etLetter.addDialogWindow(this)
        binding.etIdNumber.addDialogWindow(this)
        binding.etNumber.addDialogWindow(this)
        binding.etLetter.addONKeyboardStatusChangeListener(object : KeyboardEditText.OnKeyboardStatusChangeListener{
            override fun onKeyBoardShow() {
                initImmersionBar(false)
            }

            override fun onKeyBoardHide() {
                initImmersionBar(true)
            }
        })

        binding.etIdNumber.addONKeyboardStatusChangeListener(object : KeyboardEditText.OnKeyboardStatusChangeListener{
            override fun onKeyBoardShow() {
                initImmersionBar(false)
            }

            override fun onKeyBoardHide() {
                initImmersionBar(true)
            }
        })

        binding.etNumber.addONKeyboardStatusChangeListener(object : KeyboardEditText.OnKeyboardStatusChangeListener{
            override fun onKeyBoardShow() {
                initImmersionBar(false)
            }

            override fun onKeyBoardHide() {
                initImmersionBar(true)
            }
        })
    }

    override fun initData() {
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        KeyboardUtil.dispatchTouchEvent(ev, this)
        return super.dispatchTouchEvent(ev)
    }
}