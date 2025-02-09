package com.skyd.imomoe.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Html
import com.afollestad.materialdialogs.MaterialDialog
import com.skyd.imomoe.R
import com.skyd.imomoe.config.Api
import com.skyd.imomoe.config.Const
import com.skyd.imomoe.databinding.ActivityAboutBinding
import com.skyd.imomoe.model.DataSourceManager
import com.skyd.imomoe.util.Util
import com.skyd.imomoe.util.Util.getAppVersionName
import com.skyd.imomoe.util.Util.openBrowser
import com.skyd.imomoe.util.visible
import java.util.*

class AboutActivity : BaseActivity<ActivityAboutBinding>() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding.run {
            llAboutActivityToolbar.ivToolbar1Back.setOnClickListener { finish() }
            llAboutActivityToolbar.tvToolbar1Title.text = getString(R.string.about)
            llAboutActivityToolbar.ivToolbar1Button1.visible()
            llAboutActivityToolbar.ivToolbar1Button1.setImageResource(R.drawable.ic_info_white_24)
            llAboutActivityToolbar.ivToolbar1Button1.setOnClickListener {
                MaterialDialog(this@AboutActivity).show {
                    title(res = R.string.attention)
                    message(text = "本软件免费开源，严禁商用，支持Android 5.0+！仅在Github仓库长期发布！\n不介意的话可以给我的Github仓库点个Star")
                    positiveButton(text = "去点Star") { openBrowser(Const.Common.GITHUB_URL) }
                    negativeButton(res = R.string.cancel) { dismiss() }
                }
            }

            val c: Calendar = Calendar.getInstance()
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            if (month == Calendar.DECEMBER && day == 25) {     // 圣诞节彩蛋
                ivAboutActivityIconEgg.visible()
                ivAboutActivityIconEgg.setImageResource(R.drawable.ic_christmas_hat)
            }

            tvAboutActivityVersion.text = "V " + getAppVersionName()

            rlAboutActivityImomoe.setOnClickListener {
                openBrowser(Api.MAIN_URL)
            }

            ivAboutActivityCustomDataSourceAbout.setOnClickListener {
                MaterialDialog(this@AboutActivity).show {
                    title(res = R.string.data_source_info)
                    message(
                        text = (DataSourceManager.getConst()
                            ?: com.skyd.imomoe.model.impls.Const()).run {
                            "${getString(
                                R.string.data_source_jar_version_name,
                                versionName()
                            )}\n${getString(
                                R.string.data_source_jar_version_code,
                                versionCode().toString()
                            )}\n${about()}"
                        }
                    )
                    positiveButton(res = R.string.ok) { dismiss() }
                }
            }

            rlAboutActivityGithub.setOnClickListener {
                openBrowser(Const.Common.GITHUB_URL)
            }

            rlAboutActivityGitee.setOnClickListener {
                openBrowser(Const.Common.GITEE_URL)
            }

            rlAboutActivityLicense.setOnClickListener {
                startActivity(Intent(this@AboutActivity, LicenseActivity::class.java))
            }

            rlAboutActivityUserNotice.setOnClickListener {
                MaterialDialog(this@AboutActivity).show {
                    title(res = R.string.user_notice)
                    message(text = Html.fromHtml(Util.getUserNoticeContent()))
                    cancelable(false)
                    positiveButton(res = R.string.ok) {
                        Util.setReadUserNoticeVersion(Const.Common.USER_NOTICE_VERSION)
                    }
                }
            }

            rlAboutActivityTestDevice.setOnClickListener {
                MaterialDialog(this@AboutActivity).show {
                    title(res = R.string.test_device)
                    message(text = "HONOR V20 Android 10\nVirtual Machine: Pixel 2 Android 12")
                    positiveButton(res = R.string.ok) { dismiss() }
                }
            }
        }
    }

    override fun getBinding(): ActivityAboutBinding = ActivityAboutBinding.inflate(layoutInflater)
}