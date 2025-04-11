package com.sample.apk.installer

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
                val inputStream = context.resources.openRawResource(R.raw.sample_app_1_0)
                val file = File(context.cacheDir, "sample_1_0.apk")
                val isSuccess = getApkFileFromRaw(inputStream, file)

                if (isSuccess) {
                    getPackageFromApkFile(context, file)?.let {
                        try {
                            Log.i("CheckInstallation", "PackageName: $it")
                            context.packageManager.getPackageInfo(it, PackageManager.GET_META_DATA)
                            Toast.makeText(context, "설치되어있음", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "설치되어있지 않음", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
        ) {
            Text("설치 여부 확인")
        }
        Button(
            onClick = {
                val inputStream = context.resources.openRawResource(R.raw.sample_app_1_0)
                val file = File(context.cacheDir, "sample_1_0.apk")
                val isSuccess = getApkFileFromRaw(inputStream, file)

                if (isSuccess) {
                    CoroutineScope(Dispatchers.IO).launch {
                        installApkWithPackageInstaller(context, file)
                    }
                }
//                if (isSuccess) installApk(context, file) { activity?.startActivity(it) }
            },
        ) {
            Text("이전 버전 설치")
        }
        Button(
            onClick = {
                val inputStream = context.resources.openRawResource(R.raw.sample_app_1_1)
                val file = File(context.cacheDir, "sample_1_1.apk")
                val isSuccess = getApkFileFromRaw(inputStream, file)

                if (isSuccess) {
                    CoroutineScope(Dispatchers.IO).launch {
                        installApkWithPackageInstaller(context, file)
                    }
                }
//                if (isSuccess) installApk(context, file) { activity?.startActivity(it) }
            },
        ) {
            Text("업데이트")
        }
    }
}

private fun getPackageFromApkFile(context: Context, file: File): String? {
    try {
        val packageManager = context.packageManager
        val packageInformation = packageManager.getPackageArchiveInfo(file.absolutePath, PackageManager.GET_META_DATA)

        return packageInformation?.packageName
    } catch (e: Exception) {
        Log.e("GetPackageFromApk", "Error: ${e.message}")
        return null
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
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity.invoke(intent)
    } catch (e: Exception) {
        Log.e("InstallApk", "Error: ${e.message}")
    }
}

@SuppressLint("ServiceCast")
fun installApkWithPackageInstaller(context: Context, apkFile: File) {
    val packageInstaller = context.packageManager.packageInstaller

    val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
    val sessionId = packageInstaller.createSession(params)
    val session = packageInstaller.openSession(sessionId)

    apkFile.inputStream().use { input ->
        session.openWrite("app_session", 0, -1).use { output ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var totalBytes = 0L
            var length: Int

            while (input.read(buffer).also { length = it } != -1) {
                output.write(buffer, 0, length)
                totalBytes += length
                // TODO: Update Notification progress here using totalBytes and apkFile.length()
            }
            session.fsync(output)
        }
    }

    val intent = Intent(context, ApkInstallReceiver::class.java)
    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        sessionId,
        intent,
        flags,
    )

    session.commit(pendingIntent.intentSender)
    session.close()
}


@Composable
@Preview
fun MainScreenPreview() {
    ApkInstallerTheme {
        MainScreen()
    }
}
