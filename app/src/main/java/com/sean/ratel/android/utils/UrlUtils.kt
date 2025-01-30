package com.sean.ratel.android.utils

object UrlUtils {
    /**
     * 유효한 URL 예시
     * 기본 YouTube 비디오 URL 형식:
     *
     * https://www.youtube.com/watch?v=Ks-_Mh1QhMc
     * http://www.youtube.com/watch?v=Ks-_Mh1QhMc
     * https://youtube.com/watch?v=Ks-_Mh1QhMc
     * http://youtube.com/watch?v=Ks-_Mh1QhMc
     * YouTube 단축 URL 형식:
     *
     * https://youtu.be/Ks-_Mh1QhMc
     * http://youtu.be/Ks-_Mh1QhMc
     * 임베드 URL 형식:
     *
     * https://www.youtube.com/embed/Ks-_Mh1QhMc
     * http://www.youtube.com/embed/Ks-_Mh1QhMc
     * YouTube 모바일 URL 형식:
     *
     * https://m.youtube.com/watch?v=Ks-_Mh1QhMc
     * http://m.youtube.com/watch?v=Ks-_Mh1QhMc
     * 채널 비디오 형식:
     *
     * https://www.youtube.com/user/YouTubeUserName#p/a/u/1/Ks-_Mh1QhMc
     */
    fun parseYoutubeUrlForId(url: String): String? {
        val pattern =
            "http(?:s)?:\\/\\/(?:m.)?(?:www\\.)?youtu(?:\\.be\\/|be" +
                "\\.com\\/(?:watch\\?(?:feature=youtu.be" +
                "\\&)?v=|v\\/|embed\\/|user\\/(?:[\\w#]+\\/)+))([^&#?\\n]+)"
        val regex = Regex(pattern)
        return regex.find(url)?.run {
            for (match in groups) {
                if (match?.value?.contains("http") == false) return@run match.value
            }
            return@run null
        }
    }

    fun parseFacebookVideoUrlForId(url: String): String? {
        val matches = url.split("/")
        if (matches.size > 3) {
            return if (matches[matches.size - 1].isNotBlank()) {
                matches[matches.size - 1]
            } else {
                matches[matches.size - 2]
            }
        }
        return url
    }
}
