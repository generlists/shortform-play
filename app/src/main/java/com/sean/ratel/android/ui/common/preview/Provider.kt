package com.sean.ratel.android.ui.common.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.ShortsChannelModel
import com.sean.ratel.android.data.dto.ShortsVideoModel

class MainParameterProvider : PreviewParameterProvider<List<MainShortsModel>> {
    override val values: Sequence<List<MainShortsModel>>
        get() {
            val shortModel1 =
                ShortsVideoModel(
                    "rSYCg7L9MRk",
                    "https://i.ytimg.com/vi/OsU-XFbipV0/hqdefault.jpg",
                    "커몬2 #잔망루피 #Shorts #쇼츠ㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁ",
                    "",
                    "13:00",
                    "17",
                    "",
                    "",
                    "",
                    "",
                )
            val shorChanneltModel1 =
                ShortsChannelModel(
                    "UC5vaI7lS29D2RJ83k9hmQOA",
                    " https://yt3.ggpht.com/" +
                        "aOgjye3sMIxNl2SW2wAQZpZWUXzZ5Rg0rNITacRQKVfXvF9cnPWb77G3_gH5s2Zyw241BXWYWg=" +
                        "s88-c-k-c0x00ffffff-no-rj",
                    "뜬뜬",
                    "요리 예능프로그램으로대한민국에 예능을 꿈꾼다",
                    "",
                )
            val shortModel2 =
                ShortsVideoModel(
                    "rSYCg7L9MRk",
                    "https://i.ytimg.com/vi/OsU-XFbipV0/hqdefault.jpg",
                    "커몬2 #잔망루피 #Shorts #쇼츠",
                    "",
                    "01:22:23",
                    "17",
                    "",
                    "",
                    "",
                    "",
                )

            val shorChanneltModel2 =
                ShortsChannelModel(
                    "UC5vaI7lS29D2RJ83k9hmQOA",
                    " https://yt3.ggpht.com/aOgjye3sMIxNl2SW2wAQZpZWUXzZ5Rg0rNITacRQKVfXvF9cnPWb77G3_gH5s2Zyw241BXWYWg" +
                        "=s88-c-k-c0x00ffffff-no-rj",
                    "뜬뜬",
                    "요리 예능프로그램으로대한민국에 예능을 꿈꾼다",
                    " ",
                )

            val shortModel3 =
                ShortsVideoModel(
                    "rSYCg7L9MRk",
                    "https://i.ytimg.com/vi/OsU-XFbipV0/hqdefault.jpg",
                    "커몬2 #잔망루피 #Shorts #쇼츠",
                    "",
                    "01:22:23",
                    "17",
                    "",
                    "",
                    "",
                    "",
                )

            val shorChanneltModel3 =
                ShortsChannelModel(
                    "UC5vaI7lS29D2RJ83k9hmQOA",
                    " https://yt3.ggpht.com/aOgjye3sMIxNl2SW2wAQZpZWUXzZ5Rg0rNITacRQKVfXvF9cnPWb77G3_gH5s2Zyw241BXWYWg" +
                        "=s88-c-k-c0x00ffffff-no-rj",
                    "뜬뜬",
                    "요리 예능프로그램으로대한민국에 예능을 꿈꾼다",
                    "",
                )
            val mainShortsModel1 = MainShortsModel(0)
            mainShortsModel1.shortsVideoModel = shortModel1
            mainShortsModel1.shortsChannelModel = shorChanneltModel1

            val mainShortsModel2 = MainShortsModel(1)

            mainShortsModel2.shortsVideoModel = shortModel2
            mainShortsModel2.shortsChannelModel = shorChanneltModel2

            val mainShortsModel3 = MainShortsModel(1)

            mainShortsModel3.shortsVideoModel = shortModel3
            mainShortsModel3.shortsChannelModel = shorChanneltModel3

            return sequenceOf(listOf(mainShortsModel1, mainShortsModel2, mainShortsModel3))
        }
}

// categoryIndex:Int,horizontalItems:List<List<MainShortsModel?>>?
class ShortsVideoParameterProvider : PreviewParameterProvider<List<List<MainShortsModel?>>?> {
    override val values: Sequence<List<List<MainShortsModel>>>
        get() {
            val shortModel1 =
                ShortsVideoModel(
                    "rSYCg7L9MRk",
                    "https://i.ytimg.com/vi/OsU-XFbipV0/hqdefault.jpg",
                    "커몬2 남친이 얻어 먹으려는 수법 #잔망루피 #Shorts #쇼츠#ABCDEFG#HIGKLMN",
                    "",
                    "13:00",
                    "17",
                    "",
                    "",
                    "",
                    "",
                )
            val shorChanneltModel1 =
                ShortsChannelModel(
                    "UC5vaI7lS29D2RJ83k9hmQOA",
                    " https://yt3.ggpht.com/aOgjye3sMIxNl2SW2wAQZpZWUXzZ5Rg0rNITacRQKVfXvF9cnPWb77G3_gH5s2Zyw241BXWYWg" +
                        "=s88-c-k-c0x00ffffff-no-rj",
                    "뜬뜬",
                    "요리 예능프로그램으로대한민국에 예능을 꿈꾼다",
                    "",
                )
            val mainShortsModel1 = MainShortsModel(0)
            mainShortsModel1.shortsVideoModel = shortModel1
            mainShortsModel1.shortsChannelModel = shorChanneltModel1

            val shortModel2 =
                ShortsVideoModel(
                    "rSYCg7L9MRk",
                    "https://i.ytimg.com/vi/OsU-XFbipV0/hqdefault.jpg",
                    "커몬2 #잔망루피 #Shorts #쇼츠",
                    "",
                    "01:22:23",
                    "17",
                    "",
                    "",
                    "",
                    "",
                )

            val shorChanneltModel2 =
                ShortsChannelModel(
                    "UC5vaI7lS29D2RJ83k9hmQOA",
                    " https://yt3.ggpht.com/aOgjye3sMIxNl2SW2wAQZpZWUXzZ5Rg0rNITacRQKVfXvF9cnPWb77G3_gH5s2Zyw241BXWYWg" +
                        "=s88-c-k-c0x00ffffff-no-rj",
                    "뜬뜬",
                    "요리 예능프로그램으로대한민국에 예능을 꿈꾼다",
                    "",
                )
            val mainShortsModel2 = MainShortsModel(1)
            mainShortsModel1.shortsVideoModel = shortModel2
            mainShortsModel1.shortsChannelModel = shorChanneltModel2

            return sequenceOf(listOf(listOf(mainShortsModel1, mainShortsModel2)))
        }
}
