package com.sample.apk.installer

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.FileProvider
import com.sample.apk.installer.ui.theme.ApkInstallerTheme
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val activity = LocalActivity.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
            },
        ) {
            Text("설치 여부 확인")
        }
        Button(
            onClick = {
                val inputStream = context.resources.openRawResource(R.raw.sample_app_1_0)
                val file = File(context.cacheDir, "sample_1_0.apk")
                val isSuccess = getApkFileFromRaw(inputStream, file)

                if (isSuccess) installApk(context, file) { activity?.startActivity(it) }
            },
        ) {
            Text("이전 버전 설치")
        }
        Button(
            onClick = {
                val inputStream = context.resources.openRawResource(R.raw.sample_app_1_1)
                val file = File(context.cacheDir, "sample_1_1.apk")
                val isSuccess = getApkFileFromRaw(inputStream, file)

                if (isSuccess) installApk(context, file) { activity?.startActivity(it) }
            },
        ) {
            Text("업데이트")
        }
    }
}

private fun getApkFileFromRaw(apkInputStream: InputStream, saveFile: File): Boolean {
    try {
        val outputStream = FileOutputStream(saveFile)
        apkInputStream.copyTo(outputStream)
        apkInputStream.close()
        outputStream.close()
        return true
    } catch (e: Exception) {
        Log.e("GetApkFile", "Error: ${e.message}")
        return false
    }
}

private fun installApk(
    context: Context,
    file: File,
    startActivity: (Intent) -> Unit,
) {
    try {
        if (file.exists().not()) throw IllegalArgumentException("File is not exits.")
        if (file.extension != "apk") throw IllegalArgumentException("File extension is not apk.")

        val intent = Intent(Intent.ACTION_VIEW).apply {
            val apkURI = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileProvider",
                file,
            )
            setDataAndType(apkURI, "application/vnd.android.package-archive")

            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity.invoke(intent)
    } catch (e: Exception) {
        Log.e("InstallApk", "Error: ${e.message}")
    }
}

@Composable
@Preview
fun MainScreenPreview() {
    ApkInstallerTheme {
        MainScreen()
    }
}
