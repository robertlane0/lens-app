package com.openscan.app.ui.crop

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropScreen(
    pageId: Long,
    documentId: Long,
    onNavigateBack: () -> Unit,
    viewModel: CropViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(pageId) {
        viewModel.loadPage(pageId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.stage == CropStage.PERSPECTIVE) "Perspective Crop"
                        else "Adjust Crop"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.stage == CropStage.STANDARD) {
                        IconButton(onClick = { viewModel.saveCrop(onNavigateBack) }) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    }
                }
            )
        },
        bottomBar = {
            androidx.compose.material3.BottomAppBar(
                modifier = Modifier.height(64.dp)
            ) {
                when (state.stage) {
                    CropStage.PERSPECTIVE -> {
                        Text(
                            "Drag corners to align with document edges",
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                        androidx.compose.material3.FilledTonalButton(
                            onClick = { viewModel.applyPerspectiveCorrection { } },
                            enabled = state.corners.size == 4 && !state.isLoading,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null)
                            Text("  Correct", modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                    CropStage.STANDARD -> {
                        Text(
                            "Adjust crop area, then tap Save",
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                        androidx.compose.material3.FilledTonalButton(
                            onClick = { viewModel.saveCrop(onNavigateBack) },
                            enabled = !state.isLoading,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Text("  Save", modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else if (state.displayBitmap != null) {
            val bitmap = state.displayBitmap!!

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                when (state.stage) {
                    CropStage.PERSPECTIVE -> {
                        PerspectiveCropView(
                            bitmap = bitmap,
                            corners = state.corners,
                            onCornerDrag = { index, norm -> viewModel.updateCorner(index, norm) }
                        )
                    }
                    CropStage.STANDARD -> {
                        StandardCropView(
                            bitmap = bitmap,
                            cropRect = state.cropRect,
                            onRectChanged = { rect -> viewModel.updateCropRect(rect) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PerspectiveCropView(
    bitmap: android.graphics.Bitmap,
    corners: List<Offset>,
    onCornerDrag: (Int, Offset) -> Unit
) {
    var containerSize by remember { mutableStateOf(Size.Zero) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(corners) {
                if (corners.size != 4) return@pointerInput
                var dragIndex = -1

                detectDragGestures(
                    onDragStart = { pos ->
                        dragIndex = findNearestCorner(pos, corners, containerSize, bitmap)
                    },
                    onDrag = { change, _ ->
                        if (dragIndex >= 0) {
                            val norm = screenToNorm(change.position, containerSize, bitmap)
                            onCornerDrag(dragIndex, norm)
                        }
                    },
                    onDragEnd = { dragIndex = -1 },
                    onDragCancel = { dragIndex = -1 }
                )
            }
    ) {
        containerSize = size

        if (corners.size != 4) return@Canvas

        val (scale, offsetX, offsetY) = computeLayout(size, bitmap.width, bitmap.height)

        drawImage(
            image = bitmap.asImageBitmap(),
            dstOffset = IntOffset(offsetX.toInt(), offsetY.toInt()),
            dstSize = IntSize(
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt()
            )
        )

        val screenPts = corners.map { c ->
            Offset(
                offsetX + c.x * bitmap.width * scale,
                offsetY + c.y * bitmap.height * scale
            )
        }

        val polygonPath = Path().apply {
            moveTo(screenPts[0].x, screenPts[0].y)
            for (i in 1..3) lineTo(screenPts[i].x, screenPts[i].y)
            close()
        }
        drawPath(polygonPath, Color(0x6684C1FF), style = Stroke(width = 3f))

        screenPts.forEachIndexed { i, pt ->
            drawCircle(Color.White, 20f, pt)
            drawCircle(Color(0xFF1976D2), 16f, pt)
            drawCircle(Color.White, 6f, pt)
        }
    }
}

@Composable
private fun StandardCropView(
    bitmap: android.graphics.Bitmap,
    cropRect: RectF,
    onRectChanged: (RectF) -> Unit
) {
    var containerSize by remember { mutableStateOf(Size.Zero) }
    var dragMode by remember { mutableStateOf(DragMode.NONE) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(cropRect) {
                detectDragGestures(
                    onDragStart = { pos ->
                        dragMode = detectCropDragMode(pos, cropRect, containerSize, bitmap)
                    },
                    onDrag = { change, delta ->
                        val (scale, offsetX, offsetY) = computeLayout(
                            containerSize, bitmap.width, bitmap.height
                        )
                        val normDelta = Offset(
                            delta.x / (bitmap.width * scale),
                            delta.y / (bitmap.height * scale)
                        )

                        val r = cropRect
                        var newRect = when (dragMode) {
                            DragMode.TOP_LEFT -> RectF(
                                r.left + normDelta.x, r.top + normDelta.y,
                                r.right, r.bottom
                            )
                            DragMode.TOP_RIGHT -> RectF(
                                r.left, r.top + normDelta.y,
                                r.right + normDelta.x, r.bottom
                            )
                            DragMode.BOTTOM_RIGHT -> RectF(
                                r.left, r.top,
                                r.right + normDelta.x, r.bottom + normDelta.y
                            )
                            DragMode.BOTTOM_LEFT -> RectF(
                                r.left + normDelta.x, r.top,
                                r.right, r.bottom + normDelta.y
                            )
                            DragMode.TOP -> RectF(
                                r.left, r.top + normDelta.y,
                                r.right, r.bottom
                            )
                            DragMode.RIGHT -> RectF(
                                r.left, r.top,
                                r.right + normDelta.x, r.bottom
                            )
                            DragMode.BOTTOM -> RectF(
                                r.left, r.top,
                                r.right, r.bottom + normDelta.y
                            )
                            DragMode.LEFT -> RectF(
                                r.left + normDelta.x, r.top,
                                r.right, r.bottom
                            )
                            DragMode.MOVE -> RectF(
                                r.left + normDelta.x, r.top + normDelta.y,
                                r.right + normDelta.x, r.bottom + normDelta.y
                            )
                            DragMode.NONE -> r
                        }

                        newRect = RectF(
                            newRect.left.coerceIn(0f, newRect.right - 0.01f),
                            newRect.top.coerceIn(0f, newRect.bottom - 0.01f),
                            newRect.right.coerceIn(newRect.left + 0.01f, 1f),
                            newRect.bottom.coerceIn(newRect.top + 0.01f, 1f)
                        )
                        onRectChanged(newRect)
                    },
                    onDragEnd = { dragMode = DragMode.NONE },
                    onDragCancel = { dragMode = DragMode.NONE }
                )
            }
    ) {
        containerSize = size

        val (scale, offsetX, offsetY) = computeLayout(size, bitmap.width, bitmap.height)

        drawImage(
            image = bitmap.asImageBitmap(),
            dstOffset = IntOffset(offsetX.toInt(), offsetY.toInt()),
            dstSize = IntSize(
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt()
            )
        )

        val left = offsetX + cropRect.left * bitmap.width * scale
        val top = offsetY + cropRect.top * bitmap.height * scale
        val right = offsetX + cropRect.right * bitmap.width * scale
        val bottom = offsetY + cropRect.bottom * bitmap.height * scale

        drawRect(Color(0x44000000), Offset.Zero, Size(size.width, top))
        drawRect(Color(0x44000000), Offset(0f, bottom), Size(size.width, size.height - bottom))
        drawRect(Color(0x44000000), Offset(0f, top), Size(left, bottom - top))
        drawRect(Color(0x44000000), Offset(right, top), Size(size.width - right, bottom - top))

        drawRect(
            Color(0xFF84C1FF),
            Offset(left, top),
            Size(right - left, bottom - top),
            style = Stroke(width = 2f)
        )

        val handles = listOf(
            Offset(left, top),
            Offset(right, top),
            Offset(right, bottom),
            Offset(left, bottom),
            Offset((left + right) / 2f, top),
            Offset(right, (top + bottom) / 2f),
            Offset((left + right) / 2f, bottom),
            Offset(left, (top + bottom) / 2f)
        )
        handles.forEach { pt ->
            drawCircle(Color.White, 14f, pt)
            drawCircle(Color(0xFF1976D2), 10f, pt)
        }
    }
}

private enum class DragMode {
    NONE, TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT,
    TOP, RIGHT, BOTTOM, LEFT, MOVE
}

private fun detectCropDragMode(
    pos: Offset,
    rect: RectF,
    containerSize: Size,
    bitmap: android.graphics.Bitmap
): DragMode {
    val (scale, offsetX, offsetY) = computeLayout(containerSize, bitmap.width, bitmap.height)
    val threshold = 30f

    val l = offsetX + rect.left * bitmap.width * scale
    val t = offsetY + rect.top * bitmap.height * scale
    val r = offsetX + rect.right * bitmap.width * scale
    val b = offsetY + rect.bottom * bitmap.height * scale

    val handlePositions = listOf(
        Offset(l, t) to DragMode.TOP_LEFT,
        Offset(r, t) to DragMode.TOP_RIGHT,
        Offset(r, b) to DragMode.BOTTOM_RIGHT,
        Offset(l, b) to DragMode.BOTTOM_LEFT,
        Offset((l + r) / 2f, t) to DragMode.TOP,
        Offset(r, (t + b) / 2f) to DragMode.RIGHT,
        Offset((l + r) / 2f, b) to DragMode.BOTTOM,
        Offset(l, (t + b) / 2f) to DragMode.LEFT
    )

    for ((hp, mode) in handlePositions) {
        if ((pos - hp).getDistance() <= threshold) return mode
    }

    if (pos.x in l..r && pos.y in t..b) return DragMode.MOVE

    return DragMode.NONE
}

private fun findNearestCorner(
    pos: Offset,
    corners: List<Offset>,
    containerSize: Size,
    bitmap: android.graphics.Bitmap
): Int {
    val (scale, offsetX, offsetY) = computeLayout(containerSize, bitmap.width, bitmap.height)
    var best = 0
    var bestDist = Float.MAX_VALUE
    corners.forEachIndexed { i, c ->
        val sx = offsetX + c.x * bitmap.width * scale
        val sy = offsetY + c.y * bitmap.height * scale
        val dist = (pos - Offset(sx, sy)).getDistance()
        if (dist < bestDist) {
            bestDist = dist
            best = i
        }
    }
    return best
}

private fun screenToNorm(
    pos: Offset,
    containerSize: Size,
    bitmap: android.graphics.Bitmap
): Offset {
    val (scale, offsetX, offsetY) = computeLayout(containerSize, bitmap.width, bitmap.height)
    val ix = (pos.x - offsetX) / (bitmap.width * scale)
    val iy = (pos.y - offsetY) / (bitmap.height * scale)
    return Offset(ix.coerceIn(0f, 1f), iy.coerceIn(0f, 1f))
}

private fun computeLayout(
    containerSize: Size,
    bmpW: Int,
    bmpH: Int
): Triple<Float, Float, Float> {
    val scale = minOf(
        containerSize.width / bmpW,
        containerSize.height / bmpH
    )
    val offsetX = (containerSize.width - bmpW * scale) / 2f
    val offsetY = (containerSize.height - bmpH * scale) / 2f
    return Triple(scale, offsetX, offsetY)
}
