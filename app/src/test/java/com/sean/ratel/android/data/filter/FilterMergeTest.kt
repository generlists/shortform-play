package com.sean.ratel.android.data.filter

import org.junit.Assert.assertEquals
import org.junit.Test

class FilterMergeTest {
    @Test
    fun `union keeps base order and version and drops duplicates`() {
        val base = ExcludeFilter(topics = listOf("a", "b"), keywords = listOf("직캠"), blockedChannels = listOf("c1"), version = 5)
        val other = ExcludeFilter(topics = listOf("b", "c"), keywords = listOf("리액션", "직캠"), exceptions = listOf("v1"), version = 2)
        val merged = FilterMerge.union(base, other)
        assertEquals(listOf("a", "b", "c"), merged.topics)
        assertEquals(listOf("직캠", "리액션"), merged.keywords)
        assertEquals(listOf("c1"), merged.blockedChannels)
        assertEquals(listOf("v1"), merged.exceptions)
        assertEquals(5, merged.version)
    }

    @Test
    fun `sanitize trims dedupes keywords case-insensitively and collapses spaces`() {
        val filter = ExcludeFilter(keywords = listOf("  Boy   Band ", "boy band", "", "  ", "직캠"))
        assertEquals(listOf("Boy Band", "직캠"), FilterLimits.sanitize(filter).keywords)
    }

    @Test
    fun `sanitize drops invalid ids and bad keywords`() {
        val filter =
            ExcludeFilter(
                topics = listOf("ok_id", "has space", "한글", ""),
                keywords = listOf("a".repeat(FilterLimits.KEYWORD_LEN_MAX + 1), "bad\u0000word", "good"),
                blockedChannels = listOf("UC-1", "a/b"),
                exceptions = listOf("v" + "x".repeat(FilterLimits.ID_LEN_MAX)),
            )
        val clean = FilterLimits.sanitize(filter)
        assertEquals(listOf("ok_id"), clean.topics)
        assertEquals(listOf("good"), clean.keywords)
        assertEquals(listOf("UC-1"), clean.blockedChannels)
        assertEquals(emptyList<String>(), clean.exceptions)
    }

    @Test
    fun `sanitize truncates to the server limits`() {
        val filter =
            ExcludeFilter(
                topics = (1..FilterLimits.TOPICS_MAX + 5).map { "t$it" },
                keywords = (1..FilterLimits.KEYWORDS_MAX + 5).map { "kw$it" },
                blockedChannels = (1..FilterLimits.CHANNELS_MAX + 5).map { "c$it" },
                exceptions = (1..FilterLimits.EXCEPTIONS_MAX + 5).map { "e$it" },
            )
        val clean = FilterLimits.sanitize(filter)
        assertEquals(FilterLimits.TOPICS_MAX, clean.topics.size)
        assertEquals(FilterLimits.KEYWORDS_MAX, clean.keywords.size)
        assertEquals(FilterLimits.CHANNELS_MAX, clean.blockedChannels.size)
        assertEquals(FilterLimits.EXCEPTIONS_MAX, clean.exceptions.size)
    }
}
