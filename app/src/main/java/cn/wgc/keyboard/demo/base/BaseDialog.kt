package cn.wgc.keyboard.demo.base

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewbinding.ViewBinding
import cn.wgc.keyboard.demo.R

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

abstract class BaseDialog<VB : ViewBinding>(activity: Activity, @StyleRes themeResId: Int) :
    Dialog(activity, themeResId) {

//    init {
//        setOwnerActivity(activity)
//    }

    private lateinit var _binding: VB
    protected val binding get() = _binding

    protected abstract fun loadViewBinding(): VB
    protected abstract fun initListener()
    protected abstract fun initView()
    protected abstract fun initData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = loadViewBinding()
        setContentView(_binding.root)
        initImmersionBar(true)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initListener()
        initView()
        initData()
    }

    open fun initImmersionBar(isTransparent: Boolean) {
        // 控制图标颜色（深色图标适合浅背景，浅色图标适合深背景）
        val insetsController = WindowInsetsControllerCompat(window, window!!.decorView)
        insetsController.isAppearanceLightStatusBars = true // true = 状态栏图标为深色
        insetsController.isAppearanceLightNavigationBars = true // false = 导航栏图标为浅色
        //Android 15设置导航栏透明,Android15启用EdgeToEdge状态栏默认透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window?.isNavigationBarContrastEnforced = if (isTransparent) false else true
        } else {
            // 透明状态栏和导航栏
            window?.statusBarColor = Color.TRANSPARENT
            window?.navigationBarColor = ContextCompat.getColor(context,
                                                                if (isTransparent) R.color.transparent01 else R.color.white)
        }
    }


}