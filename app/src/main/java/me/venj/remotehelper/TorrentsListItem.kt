package me.venj.remotehelper

import java.io.Serializable

data class TorrentsListItem(val title: String, val count: Int) : Serializable {
    override fun toString(): String {
        return "${this.title} (${this.count})"
    }
}
