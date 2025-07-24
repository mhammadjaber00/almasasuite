package io.yavero.almasasuite

class JSPlatform : Platform {
    override val name: String = "JavaScript"
}

actual fun getPlatform(): Platform = JSPlatform()