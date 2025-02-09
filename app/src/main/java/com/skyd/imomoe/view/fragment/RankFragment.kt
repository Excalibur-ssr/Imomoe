package com.skyd.imomoe.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.skyd.imomoe.R
import com.skyd.imomoe.databinding.FragmentRankBinding
import com.skyd.imomoe.util.Util.showToast
import com.skyd.imomoe.view.adapter.AnimeShowAdapter
import com.skyd.imomoe.view.adapter.decoration.AnimeShowItemDecoration
import com.skyd.imomoe.view.adapter.spansize.AnimeShowSpanSize
import com.skyd.imomoe.viewmodel.RankListViewModel


class RankFragment : BaseFragment<FragmentRankBinding>() {
    private var partUrl: String = ""
    private lateinit var viewModel: RankListViewModel
    private lateinit var adapter: AnimeShowAdapter.GridRecyclerView1Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(RankListViewModel::class.java)
        val arguments = arguments

        try {
            partUrl = arguments?.getString("partUrl") ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            e.message?.showToast(Toast.LENGTH_LONG)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapter = AnimeShowAdapter.GridRecyclerView1Adapter(activity!!, viewModel.rankList)
        adapter.showRankNumber = true
        mBinding.run {
            rvRankFragment.layoutManager = GridLayoutManager(activity, 4)
                .apply {
                    spanSizeLookup = AnimeShowSpanSize(adapter)
                }
            rvRankFragment.addItemDecoration(AnimeShowItemDecoration())
            rvRankFragment.setHasFixedSize(true)
            rvRankFragment.adapter = adapter
            srlRankFragment.setOnRefreshListener {
                viewModel.getRankListData(partUrl)
            }
            srlRankFragment.setOnLoadMoreListener {
                viewModel.pageNumberBean?.let {
                    viewModel.getRankListData(it.actionUrl, isRefresh = false)
                    return@setOnLoadMoreListener
                }
                mBinding.srlRankFragment.finishLoadMore()
                getString(R.string.no_more_info).showToast()
            }
        }

        viewModel.mldRankData.observe(viewLifecycleOwner, Observer {
            mBinding.srlRankFragment.closeHeaderOrFooter()

            when (it) {
                0 -> {
                    adapter.notifyDataSetChanged()
                    hideLoadFailedTip()
                }
                1 -> {
                    val pair = viewModel.newPageIndex
                    if (pair != null) {
                        adapter.notifyItemRangeInserted(pair.first, pair.second)
                    } else adapter.notifyDataSetChanged()
                    hideLoadFailedTip()
                }
                -1 -> {
                    adapter.notifyDataSetChanged()
                    showLoadFailedTip(getString(R.string.load_data_failed_click_to_retry),
                        View.OnClickListener {
                            viewModel.getRankListData(partUrl)
                            hideLoadFailedTip()
                        })
                }
            }
        })

        mBinding.srlRankFragment.autoRefresh()
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRankBinding =
        FragmentRankBinding.inflate(inflater, container, false)

}