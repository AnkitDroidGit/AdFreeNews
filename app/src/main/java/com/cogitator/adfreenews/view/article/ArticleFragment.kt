package com.cogitator.adfreenews.view.article

import android.app.ActivityOptions
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.cogitator.adfreenews.Injection
import com.cogitator.adfreenews.R
import com.cogitator.adfreenews.model.News
import com.cogitator.adfreenews.utils.gone
import com.cogitator.adfreenews.utils.inflate
import com.cogitator.adfreenews.utils.visible
import com.cogitator.adfreenews.view.adapter.NewsListAdapter
import com.cogitator.adfreenews.view.adapter.OnNewsItemClick
import com.cogitator.adfreenews.view.newsDetail.NewsDetailActivity
import kotlinx.android.synthetic.main.fragment_article.*


/**
 * @author Ankit Kumar on 14/09/2018
 */

class ArticleFragment : Fragment(), ArticleContract.View, OnNewsItemClick {
    lateinit var newsAdapter: NewsListAdapter
    private var mListener: OnArticleFragmentInteractionListener? = null
    var newsCategory = "Science"
    var mPresenter: ArticleContract.Presenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            newsCategory = it.getString(NEWS_CATEGORY, "Science")
        }
        context?.let { mPresenter = ArticlePresenter(Injection.provideNewsRepository(it.applicationContext)) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = container?.inflate(R.layout.fragment_article)
        mPresenter?.attachView(this)
        newsAdapter = NewsListAdapter(arrayListOf(), this)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter?.getArticlesByCategory(newsCategory)
        swiperefresh?.setOnRefreshListener {
            swiperefresh.isRefreshing = true
            mPresenter?.getArticlesByCategory(newsCategory)
        }
    }

    override fun onNewsListEmpty() {

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnArticleFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onBookmarkIvClick(news: News, position: Int) {
        mPresenter?.updateBookmarkNews(news, position)
    }

    override fun changeBookmarkStatus(position: Int) {
        newsAdapter.notifyItemChanged(position)
        mListener?.refreshBookmarksNews()
    }

    fun refreshBookmarkStatus(newsId: String) {
        newsAdapter.refreshBookmarkStatus(newsId)
    }

    override fun onItemClick(newsId: String, sharedView: View?) {
        val transition = sharedView?.let { ViewCompat.getTransitionName(it) }
        context?.let { startActivity(sharedView?.let { it1 -> NewsDetailActivity.createIntent(it, newsId, it1) }, ActivityOptions.makeSceneTransitionAnimation(activity, sharedView, transition).toBundle()) }
    }

    override fun showLoader() {
        loader.visible()
        swiperefresh.isRefreshing = true
        errorTv.gone()
        newsListRv.gone()
        swiperefresh.gone()
    }

    override fun showArticles(articleList: ArrayList<News>) {
        newsAdapter.setData(articleList)
        newsListRv.let {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = newsAdapter
        }
        swiperefresh.isRefreshing = false
        loader.gone()
        errorTv.gone()
        newsListRv.visible()
        swiperefresh.visible()

        val resId = R.anim.layout_animation_fall_down
        val animation = AnimationUtils.loadLayoutAnimation(context, resId)
        newsListRv.layoutAnimation = animation
    }

    override fun showError() {
        loader.gone()
        swiperefresh.isRefreshing = false
        errorTv.visible()
        newsListRv.gone()
        swiperefresh.gone()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.detachView()
    }

    interface OnArticleFragmentInteractionListener {
        fun refreshBookmarksNews()
    }

    companion object {
        val NEWS_CATEGORY = "newsCategory"

        fun newInstance(newsCategory: String): ArticleFragment {
            val fragment = ArticleFragment()
            val args = Bundle()
            args.putString(NEWS_CATEGORY, newsCategory)
            fragment.arguments = args
            return fragment
        }
    }
}