package com.openscan.app.scanner

sealed class LensState {
    data object Idle : LensState()
    data object Capturing : LensState()
    data class Reviewing(val documentId: Long, val pageCount: Int) : LensState()
    data class Editing(val pageId: Long, val documentId: Long) : LensState()
    data class Cropping(val pageId: Long, val documentId: Long) : LensState()
    data class Exporting(val documentId: Long, val format: ExportFormat) : LensState()
}

enum class ExportFormat {
    PDF, JPEG, PNG
}

    sealed class LensEvent {
    data object OpenCamera : LensEvent()
    data object PageCaptured : LensEvent()
    data class PageAccepted(val pageId: Long) : LensEvent()
    data class EditPage(val pageId: Long, val documentId: Long) : LensEvent()
    data class CropPage(val pageId: Long, val documentId: Long) : LensEvent()
    data class ExportDocument(val documentId: Long, val format: ExportFormat) : LensEvent()
    data object Back : LensEvent()
    data object Done : LensEvent()
}

class LensStateMachine {
    private var currentState: LensState = LensState.Idle

    fun transition(event: LensEvent): LensState {
        currentState = when (val state = currentState) {
            LensState.Idle -> when (event) {
                LensEvent.OpenCamera -> LensState.Capturing
                else -> state
            }
            LensState.Capturing -> when (event) {
                is LensEvent.PageCaptured -> {
                    // Stay in capturing until user accepts
                    state
                }
                is LensEvent.PageAccepted -> {
                    // Transition to reviewing with document ID
                    LensState.Reviewing(documentId = event.pageId, pageCount = 1)
                }
                LensEvent.Back -> LensState.Idle
                else -> state
            }
            is LensState.Reviewing -> when (event) {
                is LensEvent.EditPage -> LensState.Editing(event.pageId, event.documentId)
                is LensEvent.ExportDocument -> LensState.Exporting(event.documentId, event.format)
                LensEvent.OpenCamera -> LensState.Capturing
                LensEvent.Back -> LensState.Idle
                else -> state
            }
            is LensState.Editing -> when (event) {
                is LensEvent.PageAccepted -> LensState.Reviewing(
                    documentId = state.documentId,
                    pageCount = -1
                )
                LensEvent.Back -> state
                else -> state
            }
            is LensState.Cropping -> when (event) {
                is LensEvent.PageAccepted -> LensState.Reviewing(
                    documentId = state.documentId,
                    pageCount = -1
                )
                LensEvent.Back -> LensState.Reviewing(
                    documentId = state.documentId,
                    pageCount = -1
                )
                else -> state
            }
            is LensState.Exporting -> when (event) {
                LensEvent.Done -> LensState.Idle
                else -> state
            }
        }
        return currentState
    }

    fun getState(): LensState = currentState
}
