package com.nakaharadev.roleworld.fragment

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView
import android.widget.Toast
import androidx.transition.Visibility
import androidx.viewpager2.widget.ViewPager2
import com.nakaharadev.roleworld.R
import com.nakaharadev.roleworld.ui.adapter.AuthPagerAdapter
import com.nakaharadev.roleworld.util.IOnKeyboardVisibilityListener


class AuthFragment : DefaultFragment(R.layout.auth_layout), IOnKeyboardVisibilityListener {
     private lateinit var pagerAdapter: AuthPagerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initPager()
        setKeyboardVisibilityListener(this)
    }

    private fun setKeyboardVisibilityListener(onKeyboardVisibilityListener: IOnKeyboardVisibilityListener) {
        val parentView = requireActivity().window.decorView.findViewById<ViewGroup>(android.R.id.content)

        parentView.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            private var alreadyOpen = false
            private val defaultKeyboardHeightDP = 100
            private val EstimatedKeyboardDP =
                defaultKeyboardHeightDP + 48
            private val rect = Rect()

            override fun onGlobalLayout() {
                val estimatedKeyboardHeight = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    EstimatedKeyboardDP.toFloat(),
                    parentView.resources.displayMetrics
                ).toInt()
                parentView.getWindowVisibleDisplayFrame(rect)
                val heightDiff = parentView.rootView.height - (rect.bottom - rect.top)
                val isShown = heightDiff >= estimatedKeyboardHeight

                if (isShown == alreadyOpen) {
                    Log.i("Keyboard state", "Ignoring global layout change...")
                    return
                }
                alreadyOpen = isShown
                onKeyboardVisibilityListener.onVisibilityChanged(isShown)
            }
        })
    }

    private fun initPager() {
        val viewPager = findViewById<ViewPager2>(R.id.auth_pager)

        pagerAdapter = AuthPagerAdapter(this) {
            when (it) {
                "to_sign_in" -> {
                    Log.i("PagerAdapterCallback", it)
                    viewPager.currentItem = 0
                }
                "to_sign_up" -> {
                    Log.i("PagerAdapterCallback", it)
                    viewPager.currentItem = 1
                }
            }
        }

        viewPager.adapter = pagerAdapter
    }

    companion object {
        lateinit var onAuthCallback: (settingsSet: AuthSettingsSet) -> Unit

        data class AuthSettingsSet(
            val userName: String,
            val email: String,
            val password: String,
            val theme: String,
            val lang: String
        )
    }

    override fun onVisibilityChanged(visible: Boolean) {
        findViewById<ImageView>(R.id.auth_header).visibility =
            if (visible) View.GONE
            else View.VISIBLE
    }
}