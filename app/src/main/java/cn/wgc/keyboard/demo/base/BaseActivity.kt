package cn.wgc.keyboard.demo.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

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
        super.onCreate(savedInstanceState)
        _binding = loadViewBinding()
        setContentView(_binding.root)
        initListener()
        initView()
        initData()
    }

}