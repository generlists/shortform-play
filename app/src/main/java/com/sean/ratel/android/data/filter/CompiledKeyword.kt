package com.sean.ratel.android.data.filter

/**
 * 미리 정규화해 둔 키워드 하나. [isIn] 은 이미 정규화된 문장(TextNormalizer.normalize)에서 찾는다.
 *
 * 언어별로 찾는 방식이 다르다.
 * - 한국어·일본어·중국어·태국어처럼 띄어쓰기로 단어가 나뉘지 않는 글자가 하나라도 있으면 **부분 문자열**로 찾는다
 *   ("먹방은" 에서 "먹방"). 조사가 붙기 때문이다.
 * - 영어·프랑스어·인도네시아어 등 띄어쓰는 글자만 있으면 **단어 경계**로 찾는다("idol" 이 "idolize" 에 걸리지 않게).
 *   복수형 s 는 허용한다("idols"). 띄어쓰지 않는 글자가 붙어 있는 경우는 경계로 본다("BTS직캠").
 */
class CompiledKeyword private constructor(
    val original: String,
    private val needle: String,
    private val useWordBoundary: Boolean,
) {
    fun isIn(haystack: String): Boolean {
        if (haystack.isEmpty()) return false
        if (!useWordBoundary) return haystack.contains(needle)
        var from = 0
        while (true) {
            val start = haystack.indexOf(needle, from)
            if (start < 0) return false
            if (isBoundaryBefore(haystack, start) && isBoundaryAfter(haystack, start + needle.length)) {
                return true
            }
            from = start + 1
        }
    }

    private fun isBoundaryBefore(
        text: String,
        index: Int,
    ): Boolean = index == 0 || !isSpacedWordChar(text[index - 1])

    private fun isBoundaryAfter(
        text: String,
        index: Int,
    ): Boolean {
        if (index >= text.length || !isSpacedWordChar(text[index])) return true
        // 복수형 s: "idol" → "idols"
        return text[index] == 's' && (index + 1 >= text.length || !isSpacedWordChar(text[index + 1]))
    }

    companion object {
        /** 이보다 짧은 키워드는 무시한다(한 글자 키워드가 거의 모든 영상을 숨기는 사고 방지). */
        const val MIN_LENGTH = 2

        /** 정규화 후 너무 짧거나 비면 null. */
        fun of(raw: String?): CompiledKeyword? {
            val normalized = TextNormalizer.normalize(raw).trim().split(WHITESPACE).joinToString(" ")
            if (normalized.length < MIN_LENGTH) return null
            return CompiledKeyword(
                original = raw.orEmpty().trim(),
                needle = normalized,
                useWordBoundary = normalized.none { isUnspacedScript(it) },
            )
        }

        private val WHITESPACE = Regex("\\s+")

        private val UNSPACED_SCRIPTS =
            setOf(
                Character.UnicodeScript.HAN,
                Character.UnicodeScript.HIRAGANA,
                Character.UnicodeScript.KATAKANA,
                Character.UnicodeScript.HANGUL,
                Character.UnicodeScript.THAI,
                Character.UnicodeScript.LAO,
                Character.UnicodeScript.KHMER,
                Character.UnicodeScript.MYANMAR,
            )

        private fun isUnspacedScript(ch: Char): Boolean = Character.UnicodeScript.of(ch.code) in UNSPACED_SCRIPTS

        /** 단어 경계를 판단할 때 "단어를 이루는 글자". 한글·한자 등 띄어쓰지 않는 글자는 경계로 취급한다. */
        private fun isSpacedWordChar(ch: Char): Boolean = ch.isLetterOrDigit() && !isUnspacedScript(ch)
    }
}
