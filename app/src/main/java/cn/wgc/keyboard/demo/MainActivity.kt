package cn.wgc.keyboard.demo

import android.view.MotionEvent
import cn.wgc.custom.keyboard.util.KeyboardUtil
import cn.wgc.keyboard.demo.base.BaseActivity
import cn.wgc.keyboard.demo.databinding.ActivityMainBinding


class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun loadViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initListener() {
        binding.btnDialog.setOnClickListener {
            TestDialog(this).show()
        }
    }

    override fun initView() {
        KeyboardUtil.handScrollViewFocusable(binding.sv)
    }

    override fun initData() {
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        KeyboardUtil.dispatchTouchEvent(ev, this)
        return super.dispatchTouchEvent(ev)
    }
}