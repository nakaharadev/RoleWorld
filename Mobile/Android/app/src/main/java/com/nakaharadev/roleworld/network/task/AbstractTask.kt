package com.nakaharadev.roleworld.network.task

import com.nakaharadev.roleworld.network.data.AbstractResponse

abstract class AbstractTask {
    abstract var callback: (AbstractResponse) -> Unit

    @Throws(Exception::class)
    abstract fun task()
}