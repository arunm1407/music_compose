package com.example.myapplication.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class DragReorderItemTest {

    @Test
    fun moveItem_movesElementToNewIndex() {
        val list = listOf("A", "B", "C", "D")

        assertEquals(listOf("A", "C", "B", "D"), list.moveItem(1, 2))
        assertEquals(listOf("A", "C", "B", "D"), list.moveItem(2, 1))
    }

    @Test
    fun syncOrderedListByIds_preservesLocalOrderForSameSongSet() {
        val local = listOf(
            song("1", "First"),
            song("3", "Third"),
            song("2", "Second"),
        )
        val incoming = listOf(
            song("1", "First updated"),
            song("2", "Second updated"),
            song("3", "Third updated"),
        )

        val synced = syncOrderedListByIds(local, incoming) { it.id }

        assertEquals(listOf("1", "3", "2"), synced.map { it.id })
        assertEquals("Third updated", synced[1].title)
    }

    @Test
    fun syncOrderedListByIds_replacesListWhenSongsAddedOrRemoved() {
        val local = listOf(song("1", "A"), song("2", "B"))
        val incoming = listOf(song("1", "A"), song("2", "B"), song("3", "C"))

        val synced = syncOrderedListByIds(local, incoming) { it.id }

        assertEquals(listOf("1", "2", "3"), synced.map { it.id })
    }

    private fun song(id: String, title: String) = TestSong(id, title)

    private data class TestSong(val id: String, val title: String)
}
