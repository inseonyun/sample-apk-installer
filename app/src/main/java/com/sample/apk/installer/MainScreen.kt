package com.sample.apk.installer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.sample.apk.installer.ui.theme.ApkInstallerTheme

@Composable
fun MainScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {},
        ) {
            Text("설치 여부 확인")
        }
        Button(
            onClick = {},
        ) {
            Text("이전 버전 설치")
        }
        Button(
            onClick = {},
        ) {
            Text("업데이트")
        }
    }
}

@Composable
@Preview
fun MainScreenPreview() {
    ApkInstallerTheme {
        MainScreen()
    }
}
