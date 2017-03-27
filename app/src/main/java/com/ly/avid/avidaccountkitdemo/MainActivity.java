package com.ly.avid.avidaccountkitdemo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

import ly.avid.accountkit.AvidAccoundType;
import ly.avid.accountkit.AvidUser;
import ly.avid.accountkit.AvidUserManager;
import ly.avid.accountkit.callback.AvidAccountLoginCallback;
import ly.avid.accountkit.callback.AvidAccountUploadCallback;

public class MainActivity extends AppCompatActivity {

    public static boolean SHOW_MSG = true;
    public static int APP_REQUEST_CODE = 99;
    private Context mContext;
    CallbackManager callbackManager;

    //是否跳转过应用程序信息详情页
    private boolean mIsJump2Settings = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                showMsg("Facebook login success, " + loginResult.getAccessToken().getToken());
                uploadGameUserInfo("sober_fb0", AvidAccoundType.TYPE_FACEBOOK, loginResult.getAccessToken().getToken());
            }

            @Override
            public void onCancel() {
                showMsg("Facebook login cancel.");
            }

            @Override
            public void onError(FacebookException error) {
                showMsg("Facebook login error, errorinfo:" + error.getMessage());
            }
        });

        findViewById(R.id.btn_login_fb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(mContext, MainActivity.class));
                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile"));
            }
        });

        findViewById(R.id.btn_login_sms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(mContext, AccountKitActivity.class);
                AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                        new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE, AccountKitActivity.ResponseType.CODE); // or .ResponseType.TOKEN
                // ... perform additional configuration ...
                intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configurationBuilder.build());
                startActivityForResult(intent, APP_REQUEST_CODE);
            }
        });

        findViewById(R.id.btn_login_email).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(mContext, AccountKitActivity.class);
                AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                        new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.EMAIL, AccountKitActivity.ResponseType.CODE); // or .ResponseType.TOKEN
                // ... perform additional configuration ...
                intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configurationBuilder.build());
                startActivityForResult(intent, APP_REQUEST_CODE);
            }
        });

        findViewById(R.id.btn_login_avid_sms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AvidUserManager.phoneLogin(new AvidAccountLoginCallback() {
                    @Override
                    public void onSuccess(final AvidUser avidUser) {
                        showMsg("phone login onSuccess: accountType: " + avidUser.accountType + "\n" + " number: " + avidUser.phoneNumber);
                        uploadGameUserInfo("sober_avid_sms", avidUser.accountType, null);
                    }

                    @Override
                    public void onFail(String errorMessage) {
                        showMsg("phone login 登录失败回调," + errorMessage);
                    }
                });
            }
        });

        findViewById(R.id.btn_login_avid_email).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AvidUserManager.emailLogin(new AvidAccountLoginCallback() {
                    @Override
                    public void onSuccess(final AvidUser avidUser) {
                        showMsg("email login onSuccess: accountType: " + avidUser.accountType + "\n" + " email: " + avidUser.email);
                        uploadGameUserInfo("sober_avid_email", avidUser.accountType, null);
                    }

                    @Override
                    public void onFail(String errorMessage) {
                        showMsg("email login 登录失败回调, "+errorMessage);
                    }
                });
            }
        });

        findViewById(R.id.btn_login_guest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMsg("SDK_INT:"+Build.VERSION.SDK_INT+", M:"+Build.VERSION_CODES.M);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!(checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)) {
                            showMsg("Please grant the permission this time");
                        }
                        requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_SMS}, 1);
                    }else {
                        getDeviceId();
                    }
                }else {
                    getDeviceId();
                }
                //uploadGameUserInfo("sober_guest", AvidAccoundType.TYPE_GUEST, null);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsJump2Settings) {
            onRecheckPermission();
            mIsJump2Settings = false;
        }
    }

    //如果从设置界面返回，则重新申请权限
    public void onRecheckPermission() {
        showMsg("onRecheckPermission");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            int grantResult = grantResults[0];
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                getDeviceId();
            }else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_PHONE_STATE)) {
                    //showMsg("open app setting to open the permission");
                    showPermissionSettingsDialog();
                }
            }
        }
    }

    private void showPermissionSettingsDialog() {
        new AlertDialog.Builder(this).setCancelable(false).setTitle("温馨提示").
                setMessage("由於您已勾選不再提示該權限授權，如需登錄之前的遊客角色，請打開權限：設置－應用－怪物X聯盟2－權限－電話，謝謝！").setNegativeButton("下次吧", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setPositiveButton("去设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                jump2PermissionSettings();
            }
        }).show();
    }

    /**
     * 跳转到应用程序信息详情页面
     */
    private void jump2PermissionSettings() {
        mIsJump2Settings = true;
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private void getDeviceId() {
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = telephonyManager.getDeviceId();
        showMsg("获取设备ID:, "+deviceId);
    }

    private void uploadGameUserInfo(String gameAccountId, String
            gameAccountType, String gameAccountToken){
        AvidUserManager.uploadGameUserInfo(gameAccountId, gameAccountType, gameAccountToken, new AvidAccountUploadCallback(){
            @Override
            public void onSuccess(String gameAccountId, String avid) {
                showMsg("uploadGameUserInfo onSuccess: gameAccountId: " + gameAccountId + "\n" + "avid: " + avid);
            }

            @Override
            public void onFail(String errorMessage) {
                showMsg("uploadGameUserInfo 失败回调,"+ errorMessage);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void showMsg(String msg){
        if (SHOW_MSG) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            Log.d("LoginActivity", msg);
        }
    }
}
