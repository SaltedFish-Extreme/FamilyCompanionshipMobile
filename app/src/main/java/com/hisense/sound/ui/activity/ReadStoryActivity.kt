package com.hisense.sound.ui.activity

import android.os.Bundle
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.drake.serialize.intent.bundle
import com.hisense.sound.R
import com.hisense.sound.logic.model.StoryModel
import com.hisense.sound.ui.adapter.StoryAdapter
import com.hisense.sound.widget.decoration.SpaceItemDecoration
import com.hisense.sound.widget.view.clickNoRepeat

/** 讲故事页面 */
class ReadStoryActivity : BaseActivity() {

    private val comeback: ImageView by lazy { findViewById<ImageView>(R.id.comeback) }
    private val rv: RecyclerView by lazy { findViewById<RecyclerView>(R.id.rv) }

    /** 模型ID */
    private val mould: String by bundle()

    /** 发音人 */
    private val person: String by bundle()

    /** 适配器 */
    private val adapter by lazy { StoryAdapter(this, mould, person) }

    /** 故事文本数组 */
    private val text by lazy { resources.getStringArray(R.array.story_text) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_story)
        comeback.clickNoRepeat { finish() }
        //填充故事列表适配器
        adapter.setList(
            arrayListOf(
                StoryModel("虚荣的小灰鸭", text[0], false),
                StoryModel("分享花香的小松鼠", text[1], false),
                StoryModel("仗势欺人的小老虎", text[2], false),
                StoryModel("狐狸和小母鸡", text[3], false),
                StoryModel("小狼路路生病了", text[4], false),
                StoryModel("老狐狸和鸭子", text[5], false),
                StoryModel("毛毛虫历险记", text[6], false),
                StoryModel("小花猫和小白兔", text[7], false),
                StoryModel("最好的玩具", text[8], false),
                StoryModel("最大的财富", text[9], false)
            )
        )
        //设置适配器
        rv.adapter = adapter
        //设置分隔线
        rv.addItemDecoration(SpaceItemDecoration(this))
    }
}