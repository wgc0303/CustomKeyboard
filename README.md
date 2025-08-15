# 使用

在module 的 build.gradle 添加依赖

```groovy
implementation 'io.github.wgc0303:CustomKeyboard:1.1.5'
```

在你的xml布局是添加控件

```xml
        <cn.wgc.custom.keyboard.view.KeyboardEditText
            android:layout_width="match_parent"
            android:layout_height="60dp"
            app:keyboardType="letter2number"
            app:pointInputEnable="true" />
```

# KeyboardEditText属性说明

| 字段               | 说明                                                                                                          |
|:----------------:|:-----------------------------------------------------------------------------------------------------------:|
| keyboardType     | 键盘类型，可选属性：number（数字键盘）、idNumber（身份证键盘）、number2letter（数字键盘转字母）、letter2number（字母转数字键盘）、numPwd（数字密码键盘，含显示隐藏按钮） |
| pointInputEnable | 是否使用点来替换隐藏按钮 ，true(替换)、false(不替换)                                                                           |

# Dialog中使用需要特殊处理

1、dialogWindow的高度属性必须设置成ViewGroup.LayoutParams.MATCH_PAREN，键盘的位置才能在底部位置。

```kotlin
        dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                               ViewGroup.LayoutParams.MATCH_PARENT)


```

2、需要将dialog设置进自定义的KeyboardEditText内

```kotlin
    //这样它们就能自我管理键盘的显示和滚动。
    binding.etLetter.addDialogWindow(this)
    binding.etIdNumber.addDialogWindow(this)
    binding.etNumber.addDialogWindow(this)
```

# 修改键盘参数

如键盘字体大小颜色等参数不符合你的要求，可以在layout下新建view_content_keyboard.xml文件,覆盖掉框架原有布局，保留id即可

```xml
<?xml version="1.0" encoding="utf-8"?>
<cn.wgc.custom.keyboard.view.CustomKeyboard
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/view_keyboard"
    android:layout_width="match_parent"
    android:layout_height="284dp"
    android:background="@color/gray"
    app:keyDrawableSize="18dp"
    app:keyNormalColor="@color/white"
    app:keyPadding="1.5dp"
    app:keyPressColor="@color/gray"
    app:keyTextColor="@color/black"
    app:keyTextSize="24sp"
    app:keyTopPadding="0.5dp" />
```

# 键盘参数说明

| 字段              | 说明      |
|:---------------:|:-------:|
| keyPadding      | 按键之间的间距 |
| keyTopPadding   | 按键顶部间距  |
| keyTextSize     | 字体大小    |
| keyTextColor    | 字体颜色    |
| keyNormalColor  | 按键正常颜色  |
| keyDrawableSize | 按键中图片大小 |
| keyPressColor   | 按键按下颜色  |

# 其他的特殊处理

1、在Actvity或dialog中点击非EditText隐藏键盘（含系统键盘），重写dispatchTouchEvent，并做以下处理

```kotlin
KeyboardUtil.dispatchTouchEvent(ev, this)
```

2、解决ScrollView中自动获取焦点，弹出键盘的问题（含系统键盘）

```kotlin
 KeyboardUtil.handScrollViewFocusable(scrollView)
```
