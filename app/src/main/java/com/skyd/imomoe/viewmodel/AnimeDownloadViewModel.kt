package com.skyd.imomoe.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.skyd.imomoe.bean.AnimeCoverBean
import com.skyd.imomoe.config.Const
import com.skyd.imomoe.database.entity.AnimeDownloadEntity
import com.skyd.imomoe.database.getAppDataBase
import com.skyd.imomoe.util.MD5.getMD5
import com.skyd.imomoe.util.Util.getDirectorySize
import com.skyd.imomoe.util.Util.getFileSize
import com.skyd.imomoe.util.Util.getFormatSize
import com.skyd.imomoe.util.downloadanime.AnimeDownloadHelper.Companion.deleteAnimeFromXml
import com.skyd.imomoe.util.downloadanime.AnimeDownloadHelper.Companion.getAnimeFromXml
import com.skyd.imomoe.util.downloadanime.AnimeDownloadHelper.Companion.save2Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class AnimeDownloadViewModel : ViewModel() {
    var animeCoverList: MutableList<AnimeCoverBean> = ArrayList()
    var mldAnimeCoverList: MutableLiveData<Boolean> = MutableLiveData()

    fun getAnimeCover() {
        GlobalScope.launch(Dispatchers.IO) {
            val files = File(Const.DownloadAnime.animeFilePath).listFiles()
            files?.let {
                animeCoverList.clear()
                for (file in it) {
                    if (file.isDirectory) {
                        val episodeCount = file.listFiles { file, s ->
                            //查找文件名不以.temp结尾的文件
                            !s.endsWith(".temp") && !s.endsWith(".xml")
                        }?.size
                        animeCoverList.add(
                            animeCoverList.size, AnimeCoverBean(
                                "animeCover7",
                                Const.ActionUrl.ANIME_ANIME_DOWNLOAD_EPISODE + "/" + file.name,
                                "",
                                file.name,
                                null,
                                "",
                                size = getFormatSize(getDirectorySize(file).toDouble()),
                                episodeCount = episodeCount.toString() + "P"
                            )
                        )
                    }
                }
            }
            mldAnimeCoverList.postValue(true)
        }
    }

    fun getAnimeCoverEpisode(directoryName: String) {
        val files = File(Const.DownloadAnime.animeFilePath + directoryName).listFiles()

        //不支持重命名文件
        GlobalScope.launch(Dispatchers.IO) {
            files?.let {
                animeCoverList.clear()
                val animeList = getAnimeFromXml(directoryName)

                // xml里的文件名
                val animeFilesName: MutableList<String?> = ArrayList()
                // 文件夹下的文件名
                val filesName: MutableList<String> = ArrayList()
                // 获取文件夹下的文件名
                for (file in files) filesName.add(file.name)
                //数据库中的数据
                val animeMd5InDB = getAppDataBase().animeDownloadDao().getAnimeDownloadMd5List()
                // 先删除xml里被用户删除的视频，再获取xml里的文件名（保证xml里的文件名都是存在的文件）
                val iterator: MutableIterator<AnimeDownloadEntity> = animeList.iterator()
                while (iterator.hasNext()) {
                    val anime = iterator.next()
                    if (anime.fileName !in filesName) {
                        deleteAnimeFromXml(directoryName, anime)
                        iterator.remove()
                    } else {
                        // 如果不在数据库中，则加入数据库
                        if (anime.md5 !in animeMd5InDB) {
                            getAppDataBase().animeDownloadDao().insertAnimeDownload(anime)
                        }
                        animeFilesName.add(anime.fileName)
                    }
                }
                // 没有在xml里的视频
                for (file in it) {
                    if (file.name !in animeFilesName) {
                        // 试图从数据库中取出不在xml里的视频的数据，如果没找到则是null
                        val unsavedAnime: AnimeDownloadEntity? =
                            getAppDataBase().animeDownloadDao()
                                .getAnimeDownload(getMD5(file) ?: "")
                        if (unsavedAnime != null && unsavedAnime.fileName == null) {
                            unsavedAnime.fileName = file.name
                            getAppDataBase().animeDownloadDao()
                                .updateFileNameByMd5(unsavedAnime.md5, file.name)
                        }
                        if (unsavedAnime != null) {
                            save2Xml(directoryName, unsavedAnime)
                            animeList.add(unsavedAnime)
                        }
                    }
                }

                for (anime in animeList) {
                    val fileName =
                        Const.DownloadAnime.animeFilePath + directoryName + "/" + anime.fileName
                    animeCoverList.add(
                        AnimeCoverBean(
                            "animeCover7",
                            (if (fileName.endsWith(".m3u8", true))
                                Const.ActionUrl.ANIME_ANIME_DOWNLOAD_M3U8
                            else Const.ActionUrl.ANIME_ANIME_DOWNLOAD_PLAY)
                                    + "/" + fileName,
                            "",
                            anime.title,
                            null,
                            "",
                            size = getFormatSize(
                                getFileSize(
                                    File(
                                        Const.DownloadAnime.animeFilePath +
                                                directoryName + "/" + anime.fileName
                                    )
                                ).toDouble()
                            )
                        )
                    )
                }
                animeCoverList.sortWith(Comparator { o1, o2 ->
                    o1.title.compareTo(o2.title)
                })
                mldAnimeCoverList.postValue(true)
            }
        }
    }

    companion object {
        const val TAG = "AnimeDownloadViewModel"
    }
}