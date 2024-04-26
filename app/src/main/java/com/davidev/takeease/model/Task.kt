package com.davidev.takeease.model

data class Task(
    val id: Int,
    val title: String,
    val date: String,
    val hour: String,
    var isComplete: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Task

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }
}