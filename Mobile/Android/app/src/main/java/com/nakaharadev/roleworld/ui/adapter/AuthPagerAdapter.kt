package com.nakaharadev.roleworld.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nakaharadev.roleworld.R
import com.nakaharadev.roleworld.fragment.SignInFragment
import com.nakaharadev.roleworld.fragment.SignUpFragment

class AuthPagerAdapter(fragment: Fragment, private val fragmentCallback: (String) -> Unit) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SignInFragment(R.layout.auth_sign_in_layout, fragmentCallback)
            1 -> SignUpFragment(R.layout.auth_sign_up_layout, fragmentCallback)
            else -> SignInFragment(R.layout.auth_sign_in_layout, fragmentCallback)
        }
    }
}