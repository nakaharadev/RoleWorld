package com.nakaharadev.roleworld.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

open class DefaultFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes) {
    private lateinit var fvbi: ((Int) -> View)

    protected fun <T : View> findViewById(@IdRes id: Int): T { return fvbi(id) as T }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fvbi = view::findViewById
    }
}