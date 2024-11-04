package org.autojs.autojs.ui.project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.stardust.autojs.project.ProjectConfig;
import com.stardust.util.IntentUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.autojs.autojs.Pref;
import org.autojs.autojs.R;
import org.autojs.autojs.build.ApkBuilder;
import org.autojs.autojs.build.ApkBuilderPluginHelper;
import org.autojs.autojs.external.fileprovider.AppFileProvider;
import org.autojs.autojs.model.script.ScriptFile;
import org.autojs.autojs.tool.BitmapTool;
import org.autojs.autojs.ui.BaseActivity;
import org.autojs.autojs.ui.filechooser.FileChooserDialogBuilder;
import org.autojs.autojs.ui.shortcut.ShortcutIconSelectActivity;
import org.autojs.autojs.ui.shortcut.ShortcutIconSelectActivity_;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Stardust on 2017/10/22.
 */
@EActivity(R.layout.activity_build)
public class BuildActivity extends BaseActivity implements ApkBuilder.ProgressCallback {

    private static final int REQUEST_CODE = 44401;

    public static final String EXTRA_SOURCE = BuildActivity.class.getName() + ".extra_source_file";

    private static final String LOG_TAG = "BuildActivity";
    private static final Pattern REGEX_PACKAGE_NAME = Pattern.compile("^([A-Za-z][A-Za-z\\d_]*\\.)+([A-Za-z][A-Za-z\\d_]*)$");

    @ViewById(R.id.source_path)
    EditText mSourcePath;

    @ViewById(R.id.source_path_container)
    View mSourcePathContainer;

    @ViewById(R.id.output_path)
    EditText mOutputPath;

    @ViewById(R.id.app_name)
    EditText mAppName;

    @ViewById(R.id.package_name)
    EditText mPackageName;

    @ViewById(R.id.version_name)
    EditText mVersionName;

    @ViewById(R.id.version_code)
    EditText mVersionCode;

    @ViewById(R.id.icon)
    ImageView mIcon;

    @ViewById(R.id.app_config)
    CardView mAppConfig;

    @ViewById(R.id.use_open_cv)
    CheckBox mUseOpenCv;

    @ViewById(R.id.use_paddle_ocr)
    CheckBox mUsePaddleOcr;

    @ViewById(R.id.use_ml_kit_ocr)
    CheckBox mUseMlKitOcr;

    @ViewById(R.id.use_onnx_runtime)
    CheckBox mUseOnnx;

    @ViewById(R.id.select_key_store)
    AppCompatSpinner mSelectKeyStore;

    @ViewById(R.id.signature_scheme)
    AppCompatSpinner mSignatureScheme;

    private List<PermissionOption> mPermissionOptions;
    private PermissionOptionAdapter mPermissionOptionAdapter;

    private ProjectConfig mProjectConfig;
    private MaterialDialog mProgressDialog;
    private String mSource;
    private boolean mIsDefaultIcon = true;

    private KeyStoreViewModel mKeyStoreViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void setupViews() {
        setToolbarAsBack(getString(R.string.text_build_apk));
        preparePermissionView();
        mSource = getIntent().getStringExtra(EXTRA_SOURCE);
        if (mSource != null) {
            setupWithSourceFile(new ScriptFile(mSource));
        }

        // 可用密钥库
        mKeyStoreViewModel = new ViewModelProvider(this, new KeyStoreViewModel.Factory(getApplicationContext())).get(KeyStoreViewModel.class);
        prepareVerifiedKeyStoresSpinner();
        mKeyStoreViewModel.updateVerifiedKeyStores();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mKeyStoreViewModel.updateVerifiedKeyStores();
    }

    /**
     * 构建可用密钥库选项列表
     */
    private void prepareVerifiedKeyStoresSpinner() {
        // 添加 默认密钥库
        // 默认密钥库在ApkBuilder.signWithApkSigner中会从Assets复制到下面的路径：copyInputStreamToFile(GlobalAppContext.get().getAssets().open("keystore/default.jks"), new File(mWorkspacePath + "/default.jks"));
        String defaultKeyStorePath = getCacheDir().getAbsolutePath() + "/build/default.jks";
        ArrayList<KeyStore> verifiedKeyStores = new ArrayList<>();
        verifiedKeyStores.add(new KeyStore(
                defaultKeyStorePath,
                getString(R.string.text_default_key_store),
                "Auto.js",
                "Auto.js",
                "Auto.js",
                true
        ));

        ArrayAdapter<KeyStore> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, verifiedKeyStores);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSelectKeyStore.setAdapter(adapter);

        mKeyStoreViewModel.getVerifiedKeyStores().observe(this, keyStores -> {
            // 清空现有的选项，但保留第一个元素，即默认密钥库
            if (verifiedKeyStores.size() > 1) {
                verifiedKeyStores.subList(1, verifiedKeyStores.size()).clear();
            }
            verifiedKeyStores.addAll(keyStores);
            adapter.notifyDataSetChanged();
        });
    }

    /**
     * 构建权限选项列表
     */
    private void preparePermissionView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_permissions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 权限及说明
        String[][] permissions = PermissionProvider.getPermissions(this);
        mPermissionOptions = new ArrayList<>();
        for (String[] permission : permissions) {
            mPermissionOptions.add(new PermissionOption(permission[0], permission[1], false));
        }
        mPermissionOptionAdapter = new PermissionOptionAdapter(mPermissionOptions);
        recyclerView.setAdapter(mPermissionOptionAdapter);
        mPermissionOptionAdapter.calculateAndSetRecyclerViewHeight(recyclerView);
    }

    @SuppressLint("StringFormatInvalid")
    private void setupWithSourceFile(ScriptFile file) {
        String dir = file.getParent();
        if (dir.startsWith(getFilesDir().getPath())) {
            dir = Pref.getScriptDirPath();
        }
        mOutputPath.setText(dir);
        mAppName.setText(file.getSimplifiedName());
        mPackageName.setText(getString(R.string.format_default_package_name, System.currentTimeMillis()));
        setSource(file);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Click(R.id.select_source)
    void selectSourceFilePath() {
        String initialDir = new File(mSourcePath.getText().toString()).getParent();
        new FileChooserDialogBuilder(this)
                .title(R.string.text_source_file_path)
                .dir(Environment.getExternalStorageDirectory().getPath(),
                        initialDir == null ? Pref.getScriptDirPath() : initialDir)
                .singleChoice(this::setSource)
                .show();
    }

    private void setSource(File file) {
        if (!file.isDirectory()) {
            mSourcePath.setText(file.getPath());
            return;
        }
        mProjectConfig = ProjectConfig.fromProjectDir(file.getPath());
        if (mProjectConfig == null) {
            // 没有找到"project.json"文件，说明是针对单个js文件打包
            return;
        }

        // 存在"project.json"文件，对项目打包
        mOutputPath.setText(new File(mSource, mProjectConfig.getBuildDir()).getPath()); // 根据"project.json"设置输出目录
        mAppConfig.setVisibility(View.GONE); // 隐藏“脚本文件(夹)路径”和“选择”按钮
        mSourcePathContainer.setVisibility(View.GONE); // 隐藏“配置”卡片
        // 根据"project.json"设置使用的额外库
        mUseOpenCv.setChecked(mProjectConfig.getUseOpenCv());
        mUsePaddleOcr.setChecked(mProjectConfig.getUsePaddleOcr());
        mUseMlKitOcr.setChecked(mProjectConfig.getUseMlKitOcr());
        mUseOnnx.setChecked(mProjectConfig.getUseOnnx());

        Set<String> selectedPermissions = mProjectConfig.getPermissions();
        for (int i = 0; i < mPermissionOptions.size(); i++) {
            PermissionOption option = mPermissionOptions.get(i);
            boolean isSelected = selectedPermissions.contains(option.getPermission());
            option.setSelected(isSelected);

            mPermissionOptionAdapter.notifyItemChanged(i);
        }
    }

    @Click(R.id.select_output)
    void selectOutputDirPath() {
        String initialDir = new File(mOutputPath.getText().toString()).exists() ?
                mOutputPath.getText().toString() : Pref.getScriptDirPath();
        new FileChooserDialogBuilder(this)
                .title(R.string.text_output_apk_path)
                .dir(initialDir)
                .chooseDir()
                .singleChoice(dir -> mOutputPath.setText(dir.getPath()))
                .show();
    }

    @Click(R.id.icon)
    void selectIcon() {
        ShortcutIconSelectActivity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                .startForResult(REQUEST_CODE);
    }

    @Click(R.id.fab)
    void buildApk() {
        if (!checkInputs()) {
            return;
        }
        doBuildingApk();
    }

    @Click(R.id.manage_key_store)
    void manageKeyStore() {
        ManageKeyStoreActivity.Companion.startActivity(this);
    }

    private boolean checkInputs() {
        boolean inputValid = true;
        inputValid &= checkNotEmpty(mSourcePath);
        inputValid &= checkNotEmpty(mOutputPath);
        inputValid &= checkNotEmpty(mAppName);
        inputValid &= checkNotEmpty(mSourcePath);
        inputValid &= checkNotEmpty(mVersionCode);
        inputValid &= checkNotEmpty(mVersionName);
        inputValid &= checkPackageNameValid(mPackageName);
        return inputValid;
    }

    private boolean checkPackageNameValid(EditText editText) {
        Editable text = editText.getText();
        String hint = ((TextInputLayout) editText.getParent().getParent()).getHint().toString();
        if (TextUtils.isEmpty(text)) {
            editText.setError(hint + getString(R.string.text_should_not_be_empty));
            return false;
        }
        if (!REGEX_PACKAGE_NAME.matcher(text).matches()) {
            editText.setError(getString(R.string.text_invalid_package_name));
            return false;
        }
        return true;

    }

    private boolean checkNotEmpty(EditText editText) {
        if (!TextUtils.isEmpty(editText.getText()) || !editText.isShown())
            return true;
        // TODO: 2017/12/8 more beautiful ways?
        String hint = ((TextInputLayout) editText.getParent().getParent()).getHint().toString();
        editText.setError(hint + getString(R.string.text_should_not_be_empty));
        return false;
    }

    @SuppressLint("CheckResult")
    private void doBuildingApk() {
        ApkBuilder.AppConfig appConfig = createAppConfig();
        File tmpDir = new File(getCacheDir(), "build/");
        File outApk = new File(mOutputPath.getText().toString(),
                String.format("%s_v%s.apk", appConfig.getAppName(), appConfig.getVersionName()));
        showProgressDialog();
        Observable.fromCallable(() -> callApkBuilder(tmpDir, outApk, appConfig))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(apkBuilder -> onBuildSuccessful(outApk),
                        this::onBuildFailed);
    }

    private ApkBuilder.AppConfig createAppConfig() {
        ApkBuilder.AppConfig appConfig = null;
        if (mProjectConfig != null) {
            appConfig = ApkBuilder.AppConfig.fromProjectConfig(mSource, mProjectConfig);
        } else {
            String jsPath = mSourcePath.getText().toString();
            String versionName = mVersionName.getText().toString();
            int versionCode = Integer.parseInt(mVersionCode.getText().toString());
            String appName = mAppName.getText().toString();
            String packageName = mPackageName.getText().toString();
            appConfig = new ApkBuilder.AppConfig()
                    .setAppName(appName)
                    .setSourcePath(jsPath)
                    .setPackageName(packageName)
                    .setVersionCode(versionCode)
                    .setVersionName(versionName)
                    .setIcon(mIsDefaultIcon ? null : (Callable<Bitmap>) () ->
                            BitmapTool.drawableToBitmap(mIcon.getDrawable())
                    );
        }
        appConfig.setUseOpenCv(mUseOpenCv.isChecked());
        appConfig.setUsePaddleOcr(mUsePaddleOcr.isChecked());
        appConfig.setUseMlKitOcr(mUseMlKitOcr.isChecked());
        appConfig.setUseOnnx(mUseOnnx.isChecked());
        Set<String> enabledPermission = new HashSet<>();
        for (PermissionOption option : mPermissionOptions) {
            if (option.isSelected()) {
                enabledPermission.add(option.getPermission());
            }
        }
        appConfig.setEnabledPermission(enabledPermission);

//        String[] signatureSchemes = getResources().getStringArray(R.array.signature_scheme_spinner_items);
        /*
            <string-array name="signature_scheme_spinner_items">
                <item>V1 + V2</item>
                <item>V1 + V3</item>
                <item>V1 + V2 + V3</item>
                <item>V1</item>
                <item>V2 + V3 (Android 7.0+)</item>
                <item>V2 (Android 7.0+)</item>
                <item>V3 (Android 9.0+)</item>
            </string-array>
         */
        String signatureScheme = mSignatureScheme.getSelectedItem().toString();
        appConfig.setV1SigningEnabled(signatureScheme.contains("V1"));
        appConfig.setV2SigningEnabled(signatureScheme.contains("V2"));
        appConfig.setV3SigningEnabled(signatureScheme.contains("V3"));
        appConfig.setV4SigningEnabled(signatureScheme.contains("V4"));

        appConfig.setKeyStore((KeyStore) mSelectKeyStore.getSelectedItem());

        return appConfig;
    }

    private ApkBuilder callApkBuilder(File tmpDir, File outApk, ApkBuilder.AppConfig appConfig) throws Exception {
        InputStream templateApk = ApkBuilderPluginHelper.openTemplateApk(BuildActivity.this);
        return new ApkBuilder(templateApk, outApk, tmpDir.getPath())
                .setProgressCallback(BuildActivity.this)
                .prepare()
                .withConfig(appConfig)
                .build()
                .sign()
                .cleanWorkspace();
    }

    private void showProgressDialog() {
        mProgressDialog = new MaterialDialog.Builder(this)
                .progress(true, 100)
                .content(R.string.text_on_progress)
                .cancelable(false)
                .show();
    }

    private void onBuildFailed(Throwable error) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        Toast.makeText(this, getString(R.string.text_build_failed) + error.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e(LOG_TAG, "Build failed", error);
    }

    @SuppressLint("StringFormatInvalid")
    private void onBuildSuccessful(File outApk) {
        mProgressDialog.dismiss();
        mProgressDialog = null;
        new MaterialDialog.Builder(this)
                .title(R.string.text_build_successfully)
                .content(getString(R.string.format_build_successfully, outApk.getPath()))
                .positiveText(R.string.text_install)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) ->
                        IntentUtil.installApkOrToast(BuildActivity.this, outApk.getPath(), AppFileProvider.AUTHORITY)
                )
                .show();

    }

    @Override
    public void onPrepare(ApkBuilder builder) {
        mProgressDialog.setContent(R.string.apk_builder_prepare);
    }

    @Override
    public void onBuild(ApkBuilder builder) {
        mProgressDialog.setContent(R.string.apk_builder_build);

    }

    @Override
    public void onSign(ApkBuilder builder) {
        mProgressDialog.setContent(R.string.apk_builder_package);

    }

    @Override
    public void onClean(ApkBuilder builder) {
        mProgressDialog.setContent(R.string.apk_builder_clean);
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        ShortcutIconSelectActivity.getBitmapFromIntent(getApplicationContext(), data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    mIcon.setImageBitmap(bitmap);
                    mIsDefaultIcon = false;
                }, Throwable::printStackTrace);

    }

}
