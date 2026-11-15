package com.sean.ratel.android.data.filter

import org.junit.Assert.assertEquals
import org.junit.Test

class TextNormalizerTest {
    @Test
    fun `null and empty become empty`() {
        assertEquals("", TextNormalizer.normalize(null))
        assertEquals("", TextNormalizer.normalize(""))
    }

    @Test
    fun `ascii is lowercased`() {
        assertEquals("boy band", TextNormalizer.normalize("Boy BAND"))
    }

    @Test
    fun `full width letters are unified`() {
        assertEquals("bts", TextNormalizer.normalize("ＢＴＳ"))
    }

    @Test
    fun `latin accents are removed`() {
        assertEquals("l'idole cafe eleve", TextNormalizer.normalize("L'Idole Café Elève"))
        assertEquals("nino", TextNormalizer.normalize("Niño"))
        assertEquals("francais", TextNormalizer.normalize("Français"))
    }

    @Test
    fun `hangul is untouched`() {
        assertEquals("먹방 직캠", TextNormalizer.normalize("먹방 직캠"))
        assertEquals("방탄소년단", TextNormalizer.normalize("방탄소년단"))
    }

    @Test
    fun `japanese voiced marks are kept`() {
        // 탁점을 지우면 "ガ" 가 "カ" 가 되어 다른 단어가 된다
        assertEquals("モッパン", TextNormalizer.normalize("モッパン"))
        assertEquals("ゲーム", TextNormalizer.normalize("ゲーム"))
        assertEquals("が", TextNormalizer.normalize("が"))
    }

    @Test
    fun `thai tone and vowel marks are kept`() {
        assertEquals("มุกบัง", TextNormalizer.normalize("มุกบัง"))
    }

    @Test
    fun `half width katakana is composed`() {
        assertEquals("ガ", TextNormalizer.normalize("ｶﾞ"))
    }
}
