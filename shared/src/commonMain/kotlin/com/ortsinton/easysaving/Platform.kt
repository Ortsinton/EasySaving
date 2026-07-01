package com.ortsinton.easysaving

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform