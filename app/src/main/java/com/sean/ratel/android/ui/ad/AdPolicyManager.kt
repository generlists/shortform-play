package com.sean.ratel.android.ui.ad

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class AdPolicyManager
    @Inject
    constructor() {
        private var actionCount = 0
        private var adCount = 0
        private var lastAdAction = -10

        fun shouldShowAd(): Boolean {
            actionCount++

            // 연속 광고 방지 (최소 2번 행동)
            if (actionCount - lastAdAction < 2) {
                return false
            }

            // 광고 상한
            if (adCount >= MAX_AD_SHOW_COUNT) {
                return false
            }

            val showAd = Random.nextInt(SEED_AD_COUNT) < 2

            if (showAd) {
                adCount++
                lastAdAction = actionCount
            }

            // 10번마다 광고 카운트 초기화
            if (actionCount % SEED_AD_COUNT == 0) {
                adCount = 0
            }

            return showAd
        }

        companion object {
            private val SEED_AD_COUNT = 10
            private val MAX_AD_SHOW_COUNT = 5
        }
    }
