package cn.wgc.keyboard.demo.base

import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import cn.wgc.keyboard.demo.R
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar

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

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {


    private lateinit var _binding: VB
    protected val binding get() = _binding

    protected abstract fun loadViewBinding(): VB
    protected abstract fun initListener()
    protected abstract fun initView()
    protected abstract fun initData()

    override fun onCreate(savedInstanceState: Bundle?) {
//        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        _binding = loadViewBinding()
        setContentView(_binding.root)

        ImmersionBar.with(this)
            .transparentBar()
            .fullScreen(true)
            .statusBarDarkFont(true)
            .navigationBarEnable(false)
            .navigationBarDarkIcon(true)
            .init()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initListener()
        initView()
        initData()
    }

}