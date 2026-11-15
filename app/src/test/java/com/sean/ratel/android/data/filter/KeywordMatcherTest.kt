package com.sean.ratel.android.data.filter

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KeywordMatcherTest {
    private fun matches(
        keyword: String,
        text: String,
    ): Boolean {
        val compiled = CompiledKeyword.of(keyword) ?: return false
        return compiled.isIn(TextNormalizer.normalize(text))
    }

    @Test
    fun `too short or blank keywords are ignored`() {
        assertNull(CompiledKeyword.of(null))
        assertNull(CompiledKeyword.of(""))
        assertNull(CompiledKeyword.of("   "))
        assertNull(CompiledKeyword.of("a"))
        assertNull(CompiledKeyword.of("정"))
        assertNotNull(CompiledKeyword.of("ai"))
        assertNotNull(CompiledKeyword.of("먹방"))
    }

    @Test
    fun `hangul keyword matches as substring even with particles`() {
        assertTrue(matches("먹방", "오늘의 먹방은 이거다"))
        assertTrue(matches("직캠", "BTS직캠 모음"))
        assertFalse(matches("먹방", "맛집 탐방"))
    }

    @Test
    fun `latin keyword uses word boundary`() {
        assertTrue(matches("idol", "K-pop idol group"))
        assertFalse(matches("idol", "Idolize everything"))
        assertFalse(matches("ive", "Live concert"))
        assertTrue(matches("ive", "IVE new song"))
    }

    @Test
    fun `plural s is allowed`() {
        assertTrue(matches("idol", "best idols ever"))
        assertTrue(matches("boy band", "top boy bands"))
        assertFalse(matches("idol", "idolsx"))
    }

    @Test
    fun `latin keyword next to hangul counts as a boundary`() {
        assertTrue(matches("bts", "BTS직캠"))
        assertTrue(matches("bts", "직캠BTS"))
    }

    @Test
    fun `accents and case are ignored`() {
        assertTrue(matches("idole", "L'Idole du jour"))
        assertTrue(matches("FRANCAIS", "cours de français"))
    }

    @Test
    fun `mixed script keyword matches as substring`() {
        assertTrue(matches("BTS 직캠", "오늘 bts 직캠 영상"))
    }

    @Test
    fun `japanese and thai keywords match as substring`() {
        assertTrue(matches("モッパン", "大人気モッパン動画"))
        assertTrue(matches("มุกบัง", "รายการมุกบังสุดฮา"))
        // 탁점이 다른 단어는 걸리지 않는다
        assertFalse(matches("ゲーム", "ケーム"))
    }

    @Test
    fun `whitespace in keyword is collapsed`() {
        assertTrue(matches("boy   band", "A boy band"))
    }
}
