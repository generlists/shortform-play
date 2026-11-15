package com.sean.ratel.android.data.filter

import java.text.Normalizer
import java.util.Locale

/**
 * 필터 비교용 문자열 정규화.
 *
 * - 전각/반각, 호환 문자를 통일하고(NFKC) 소문자로 바꾼다.
 * - **라틴 문자의 악센트만** 없앤다(프랑스어 é → e). 한글·일본어(탁점)·태국어의 결합 기호는 뜻이 바뀌므로 그대로 둔다.
 */
object TextNormalizer {
    fun normalize(input: String?): String {
        if (input.isNullOrEmpty()) return ""
        val lower = Normalizer.normalize(input, Normalizer.Form.NFKC).lowercase(Locale.ROOT)
        return stripLatinAccents(lower)
    }

    private fun stripLatinAccents(text: String): String {
        if (text.all { it.code < ASCII_LIMIT }) return text
        val decomposed = Normalizer.normalize(text, Normalizer.Form.NFD)
        val out = StringBuilder(decomposed.length)
        var lastBaseIsLatin = false
        for (ch in decomposed) {
            if (Character.getType(ch) == Character.NON_SPACING_MARK.toInt()) {
                // 라틴 글자에 붙은 악센트만 버린다. 가나 탁점, 태국어 모음·성조 기호는 남긴다
                if (!lastBaseIsLatin) out.append(ch)
            } else {
                out.append(ch)
                lastBaseIsLatin = Character.UnicodeScript.of(ch.code) == Character.UnicodeScript.LATIN
            }
        }
        return Normalizer.normalize(out, Normalizer.Form.NFC)
    }

    private const val ASCII_LIMIT = 128
}
