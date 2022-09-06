package com.hisense.sound.ui.adapter

import android.media.MediaPlayer
import android.os.Environment
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.drake.channel.receiveTagLive
import com.drake.channel.sendTag
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.hisense.sound.R
import com.hisense.sound.logic.dao.AppConfig.AccessToken
import com.hisense.sound.logic.dao.AppConfig.PlayMusicName
import com.hisense.sound.logic.dao.AppConfig.PlayMusicPerson
import com.hisense.sound.logic.dao.AppConfig.PlayMusicPosition
import com.hisense.sound.logic.dao.AppConfig.SoundSynthesisAPI
import com.hisense.sound.logic.dao.AppConfig.mediaPlayer
import com.hisense.sound.logic.model.StoryModel
import com.hisense.sound.ui.dialog.TipsDialog
import com.hisense.sound.ui.dialog.WaitDialog
import com.hisense.sound.util.KLog
import com.hisense.sound.util.OperateMp3Utils.mergeMp3
import com.hisense.sound.widget.view.invisible
import com.hisense.sound.widget.view.visible
import com.hjq.toast.ToastUtils
import kotlinx.coroutines.delay
import java.io.File

/**
 * 故事文本适配器
 *
 * @property lifecycleOwner 生命周期管理者
 * @property mould 模型ID
 * @property person 发音人
 */
class StoryAdapter(private val lifecycleOwner: LifecycleOwner, private val mould: String, private val person: String) :
    BaseAdapter<StoryModel>(R.layout.item_story) {

    /** 等待加载框 */
    private val dialog by lazy { WaitDialog.Builder(context).setMessage(R.string.synthesizing) }

    init {
        //先注册需要点击的子控件id
        this.addChildClickViewIds(R.id.story_composite, R.id.story_play)
        //设置子控件点击监听
        this.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.story_composite -> {
                    //进行合成故事文本，显示等待加载框
                    requestSynthesizedAudio(data[position].title, data[position].text, position)
                    dialog.show()
                }
                R.id.story_play -> {
                    //如果故事未合成则不执行后续操作
                    if ((getViewByPosition(position, R.id.story_composite) as TextView).isVisible) {
                        ToastUtils.show(R.string.synthesize_story_before_play)
                        return@setOnItemChildClickListener
                    }
                    //如果持久化存储了音频名称并且和该条数据标题不相同（有正在播放中的音频，但不是该条item）
                    if (PlayMusicName.isNotBlank() && PlayMusicName.split("_")[0] != data[position].title) {
                        //不允许播放，必须先暂停其他正在播放中的文件
                        ToastUtils.show(R.string.pause_play_first)
                        return@setOnItemChildClickListener
                    }
                    //没有正在播放中的文件，可以进行播放，修改该条item的播放状态
                    data[position].state = !data[position].state
                    //处于播放中
                    if (data[position].state) {
                        //初始化媒体播放器
                        if (context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) != null) {
                            initMediaPlayer("${context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)}" + "/" + data[position].title + "_" + mould + ".mp3")
                        } else {
                            initMediaPlayer("${context.filesDir}" + "/" + data[position].title + "_" + mould + ".mp3")
                        }
                        //持久化存储该音频文件名称
                        PlayMusicName = data[position].title + "_" + mould + ".mp3"
                        //将播放中的音频item位置保存
                        PlayMusicPosition = position
                        //将播放中的音频发音人保存
                        PlayMusicPerson = person
                        //发送开始播放消息，用来刷新主页面播放中item模型
                        sendTag("tag_play_start")
                    } else {
                        //处于暂停状态，停止播放，重置媒体播放器并释放，将持久化文件名称与发音人置空
                        try {
                            //发送结束播放消息，用来刷新主页面播放中模型item
                            sendTag("tag_play_end")
                            mediaPlayer.stop()
                            mediaPlayer.reset()
                            mediaPlayer.release()
                        } catch (e: Exception) {
                            e.localizedMessage?.let { KLog.e(it) } ?: ToastUtils.debugShow(e)
                        }
                    }
                    //通知更新此条item数据
                    notifyItemChanged(position)
                }
            }
        }
        //接受消息标签，已完成播放
        lifecycleOwner.receiveTagLive("tag_play_end") {
            //置空音频名称
            PlayMusicName = ""
            //置空发音人
            PlayMusicPerson = ""
            //刷新item状态
            notifyItemChanged(PlayMusicPosition)
        }
    }

    override fun convert(holder: BaseViewHolder, item: StoryModel) {
        //设置故事标题
        holder.setText(R.id.story_text, item.title)
        //如果该故事文件存在
        if (File("${context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)}" + "/" + item.title + "_" + mould + ".mp3").exists() ||
            File("${context.filesDir}" + "/" + item.title + "_" + mould + ".mp3").exists()
        ) {
            //则按钮显示已合成绿色
            holder.getView<TextView>(R.id.story_synthesized).visible()
            holder.getView<TextView>(R.id.story_composite).invisible()
        } else {
            //否则显示未合成蓝色
            holder.getView<TextView>(R.id.story_composite).visible()
            holder.getView<TextView>(R.id.story_synthesized).invisible()
        }
        //如果持久化存储了音频名称并且和该条数据标题相同，则此条item播放状态为true
        item.state = PlayMusicName.isNotBlank() && PlayMusicName.split("_")[0] == item.title
        //如果此条数据正在播放中
        if (item.state) {
            //图片修改为暂停播放
            holder.getView<ImageView>(R.id.story_play).setImageDrawable(AppCompatResources.getDrawable(context, (R.drawable.btn_stop)))
            holder.getView<ImageView>(R.id.story_spectrum).visible()
        } else {
            //否则图片修改为开始播放
            if (holder.getView<TextView>(R.id.story_synthesized).isVisible) {
                holder.getView<ImageView>(R.id.story_play).setImageDrawable(AppCompatResources.getDrawable(context, (R.drawable.btn_play)))
            } else {
                holder.getView<ImageView>(R.id.story_play).setImageDrawable(AppCompatResources.getDrawable(context, (R.drawable.btn_play_disabled)))
            }
            holder.getView<ImageView>(R.id.story_spectrum).invisible()
        }
    }

    /**
     * 初始化媒体播放器
     *
     * @param filePath 要播放的音频文件全路径
     */
    private fun initMediaPlayer(filePath: String) {
        try {
            //创建媒体播放器对象
            mediaPlayer = MediaPlayer()
            //不循环播放
            mediaPlayer.isLooping = false
            //设置音频数据源全路径
            mediaPlayer.setDataSource(filePath)
            //准备播放
            mediaPlayer.prepare()
            //开始播放
            mediaPlayer.start()
            //播放完成后，发送消息标签，已完成播放
            mediaPlayer.setOnCompletionListener {
                ToastUtils.show(R.string.play_end)
                sendTag("tag_play_end")
                //置空音频名称
                PlayMusicName = ""
                //置空发音人
                PlayMusicPerson = ""
                //刷新item状态
                notifyItemChanged(PlayMusicPosition)
                //关闭媒体播放器
                mediaPlayer.reset()
                mediaPlayer.release()
            }
            //初始化发生错误，释放该对象，打印日志
            mediaPlayer.setOnErrorListener { _, what, extra ->
                KLog.e("Play local sound onError: $what, $extra")
                ToastUtils.debugShow("Play local sound onError: $what, $extra")
                mediaPlayer.reset()
                mediaPlayer.release()
                //置空音频名称
                PlayMusicName = ""
                //置空发音人
                PlayMusicPerson = ""
                return@setOnErrorListener true
            }
        } catch (e: Exception) {
            //异常捕获打印日志
            e.localizedMessage?.let { KLog.e(it) } ?: ToastUtils.debugShow(e)
            PlayMusicName = ""
            PlayMusicPerson = ""
        }
    }

    /**
     * 请求合成音频文件
     *
     * @param title 文件标题
     * @param text 文件内容
     * @param position 要更新的数据位置
     */
    private fun requestSynthesizedAudio(title: String, text: String, position: Int) {
        //此文件存在则不需要重复合成
        if (File("${context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)}" + "/" + title + "_" + mould + ".mp3").exists() ||
            File("${context.filesDir}" + "/" + title + "_" + mould + ".mp3").exists()
        ) {
            ToastUtils.show(R.string.file_exists)
            return
        }
        //长度超过330字则分段合成
        if (text.length > 330) {
            //文件集合
            val fileList = mutableListOf<File>()
            lifecycleOwner.scopeNetLife {
                //分段请求
                text.chunked(330).forEach {
                    val mp3File = Post<File>(SoundSynthesisAPI) {
                        param("text", it)
                        param("access_token", AccessToken)
                        param("domain", 1)
                        param("language", "zh")
                        param("voice_name", mould)
                        //生成的中间文件名称（标题_模型ID_当前时间.mp3）
                        setDownloadFileName(title + "_" + mould + "_" + System.currentTimeMillis() + ".mp3")
                        //存储路径
                        context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.let { setDownloadDir(it) } ?: setDownloadDir(context.filesDir)
                        setDownloadMd5Verify()
                        setDownloadFileNameDecode()
                        setDownloadTempFile()
                    }.await()
                    //如果生成的mp3中间文件小于10k，则表明可能出现问题，吐司文件内容，并删除该文件，显示错误弹窗
                    if (mp3File.length() < 10000) {
                        ToastUtils.debugShow(mp3File.readText())
                        delay(1000)
                        mp3File.delete()
                        TipsDialog.Builder(context).setIcon(TipsDialog.ICON_ERROR).setMessage(R.string.synthesis_wrong).setDuration(2000).show()
                    }
                    //添加中间文件进集合
                    fileList.add(mp3File)
                }
            }.finally {
                //请求完成后关闭等待加载框
                dialog.dismiss()
                //如果生成的中间文件大于等于两个，则说明没有问题
                if (fileList.size >= 2) {
                    //合并多个中间文件
                    mergeMp3(fileList.map { it.absolutePath }, "${title}_${mould}.mp3")
                    //删除所有中间文件
                    fileList.forEach { it.delete() }
                    //显示成功弹窗
                    TipsDialog.Builder(context).setIcon(TipsDialog.ICON_FINISH).setMessage(R.string.synthetic_success).show()
                    //刷新item状态
                    notifyItemChanged(position)
                } else {
                    //中间文件小于两个则说明合成出现问题，显示错误弹窗
                    TipsDialog.Builder(context).setIcon(TipsDialog.ICON_ERROR).setMessage(R.string.synthesis_wrong).setDuration(2000).show()
                }
            }.catch {
                //请求作用域内出现异常，显示错误弹窗，并打印错误日志或弹出错误信息
                TipsDialog.Builder(context).setIcon(TipsDialog.ICON_ERROR).setMessage(R.string.synthesis_wrong).setDuration(2000)
                    .show()
                it.localizedMessage?.let { KLog.e(it) } ?: ToastUtils.debugShow(it)
            }
        } else {
            //否则直接合成单个文件
            lifecycleOwner.scopeNetLife {
                val mp3File = Post<File>(SoundSynthesisAPI) {
                    param("text", text)
                    param("access_token", AccessToken)
                    param("domain", 1)
                    param("language", "zh")
                    param("voice_name", mould)
                    //生成的文件名称（标题_模型ID.mp3）
                    setDownloadFileName(title + "_" + mould + ".mp3")
                    //存储路径
                    context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.let { setDownloadDir(it) } ?: setDownloadDir(context.filesDir)
                    setDownloadMd5Verify()
                    setDownloadFileNameDecode()
                    setDownloadTempFile()
                }.await()
                //如果生成的mp3文件小于50k，则表明可能出现问题，吐司文件内容，并删除该文件，显示错误弹窗
                if (mp3File.length() < 50000) {
                    ToastUtils.debugShow(mp3File.readText())
                    delay(1000)
                    mp3File.delete()
                    TipsDialog.Builder(context).setIcon(TipsDialog.ICON_ERROR).setMessage(R.string.synthesis_wrong).setDuration(2000).show()
                } else {
                    //显示成功弹窗
                    TipsDialog.Builder(context).setIcon(TipsDialog.ICON_FINISH).setMessage(R.string.synthetic_success).show()
                    //刷新item状态
                    notifyItemChanged(position)
                }
            }.finally {
                //请求完成后关闭等待加载框
                dialog.dismiss()
            }.catch {
                //请求作用域内出现异常，显示错误弹窗，并打印错误日志或弹出错误信息
                TipsDialog.Builder(context).setIcon(TipsDialog.ICON_ERROR).setMessage(R.string.synthesis_wrong).setDuration(2000)
                    .show()
                it.localizedMessage?.let { KLog.e(it) } ?: ToastUtils.debugShow(it)
            }
        }
    }
}