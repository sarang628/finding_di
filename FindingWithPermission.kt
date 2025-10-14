package com.sarang.torang.di.finding_di

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.sarang.torang.RootNavController
import com.sryang.library.compose.workflow.BestPracticeViewModel
import com.sryang.library.compose.workflow.DescribePermissionDialog
import com.sryang.library.compose.workflow.MoveSystemSettingDialog
import com.sryang.library.compose.workflow.PermissonWorkFlow.CheckRationale
import com.sryang.library.compose.workflow.PermissonWorkFlow.DeniedPermission
import com.sryang.library.compose.workflow.PermissonWorkFlow.GrantedPermission
import com.sryang.library.compose.workflow.PermissonWorkFlow.InitialPermissionCheck
import com.sryang.library.compose.workflow.PermissonWorkFlow.RecognizeToUser
import com.sryang.library.compose.workflow.PermissonWorkFlow.RequestPermission
import com.sryang.library.compose.workflow.PermissonWorkFlow.ShowRationale
import com.sryang.library.compose.workflow.PermissonWorkFlow.SuggestSystemSetting
import com.sryang.library.compose.workflow.RationaleDialog

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun  findingWithPermission(
    viewModel: BestPracticeViewModel = BestPracticeViewModel(),
    permission : String = Manifest.permission.ACCESS_FINE_LOCATION,
    navController : RootNavController = RootNavController()
) : @Composable () -> Unit = {
    var timeDiff : Long by remember { mutableStateOf(0L) } // 영구 권한 거부 상태 체크를 위한 시간
    val requestPermission = rememberPermissionState(permission, { viewModel.permissionResult(it, System.currentTimeMillis() - timeDiff); })
    val state = viewModel.state
    var stateTxt by remember { mutableStateOf("RequestPermission") }

    when (state) {
        InitialPermissionCheck  /* 최초 권한 체크 */ -> { viewModel.initialPermissionCheck(requestPermission.status.isGranted) }
        RecognizeToUser         /* UX에 권한을 필요로 하는 정보 인지 시키기 */-> { DescribePermissionDialog(onYes = { viewModel.yesInRecognizeUser() }, onNo = { viewModel.noInRecognizeUser() }) }
        CheckRationale           /* rational 여부 확인 */-> { viewModel.checkRational(requestPermission.status.shouldShowRationale) }
        DeniedPermission        /* 권한 거부 */-> { stateTxt = "권한을 거부함." }
        GrantedPermission       /* 사용자가 권한을 허가했다면, 자원 접근 가능 */-> { stateTxt = "권한을 허용함." }
        RequestPermission       /* 런타임 권한 요청하기 */ -> { LaunchedEffect(state == RequestPermission) { requestPermission.launchPermissionRequest(); timeDiff = System.currentTimeMillis() } }
        SuggestSystemSetting    /* 권한 거부 상태에서 요청 시 */ -> { MoveSystemSettingDialog(onMove = { viewModel.onMoveInSystemDialog() }, onDeny = {viewModel.noInSystemDialog()}) }
        ShowRationale           /* rationale을 표시 */ -> { RationaleDialog({ viewModel.yesRationale() }, {viewModel.noRationale()}) }
    }

    Box {
        Text(state.toString().split("$")[1].split("@")[0])
        Find(navController = navController, isGrantedPermission = state == GrantedPermission, onRequestPermission = {viewModel.request()})
    }
}