package com.openscan.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.openscan.app.ui.capture.CaptureScreen
import com.openscan.app.ui.crop.CropScreen
import com.openscan.app.ui.edit.EditScreen
import com.openscan.app.ui.gallery.GalleryScreen
import com.openscan.app.ui.home.HomeScreen
import com.openscan.app.ui.review.ReviewScreen

object Routes {
    const val HOME = "home"
    const val CAPTURE = "capture"
    const val REVIEW = "review/{documentId}"
    const val EDIT = "edit/{pageId}/{documentId}"
    const val CROP = "crop/{pageId}/{documentId}"
    const val GALLERY = "gallery"

    fun review(documentId: Long) = "review/$documentId"
    fun edit(pageId: Long, documentId: Long) = "edit/$pageId/$documentId"
    fun crop(pageId: Long, documentId: Long) = "crop/$pageId/$documentId"
}

@Composable
fun OpenScanNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToCapture = { navController.navigate(Routes.CAPTURE) },
                onNavigateToGallery = { navController.navigate(Routes.GALLERY) },
                onDocumentSaved = { docId ->
                    navController.navigate(Routes.review(docId)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }
        composable(Routes.CAPTURE) {
            CaptureScreen(
                onNavigateBack = { navController.popBackStack() },
                onDocumentSaved = { docId ->
                    navController.navigate(Routes.review(docId)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }
        composable(
            route = Routes.REVIEW,
            arguments = listOf(navArgument("documentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getLong("documentId") ?: return@composable
            ReviewScreen(
                documentId = documentId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { pageId, docId ->
                    navController.navigate(Routes.edit(pageId, docId))
                },
                onNavigateToCrop = { pageId, docId ->
                    navController.navigate(Routes.crop(pageId, docId))
                },
                onNavigateToCamera = {
                    navController.navigate(Routes.CAPTURE) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }
        composable(
            route = Routes.EDIT,
            arguments = listOf(
                navArgument("pageId") { type = NavType.LongType },
                navArgument("documentId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val pageId = backStackEntry.arguments?.getLong("pageId") ?: return@composable
            val documentId = backStackEntry.arguments?.getLong("documentId") ?: return@composable
            EditScreen(
                pageId = pageId,
                documentId = documentId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.CROP,
            arguments = listOf(
                navArgument("pageId") { type = NavType.LongType },
                navArgument("documentId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val pageId = backStackEntry.arguments?.getLong("pageId") ?: return@composable
            val documentId = backStackEntry.arguments?.getLong("documentId") ?: return@composable
            CropScreen(
                pageId = pageId,
                documentId = documentId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.GALLERY) {
            GalleryScreen(
                onNavigateBack = { navController.popBackStack() },
                onDocumentSelected = { docId ->
                    navController.navigate(Routes.review(docId))
                }
            )
        }
    }
}
