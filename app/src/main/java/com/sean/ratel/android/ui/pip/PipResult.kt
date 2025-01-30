package com.sean.ratel.android.ui.pip

/**
 * This is a class for the result of [PlayerPluginFacade.enterPipMode].
 */
sealed class PipResult {
    /**
     * Define success
     */
    object Success : PipResult()

    /**
     * Define access failure without pip permission
     */
    object NoPermission : PipResult()

    /**
     * Defines access failure because it does not have a SystemFeature
     */
    object NoSystemFeature : PipResult()

    /**
     * Define access failure for unknown reason
     */
    object UnKnownReason : PipResult()
}
