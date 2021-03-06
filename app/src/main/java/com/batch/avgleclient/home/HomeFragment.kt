package com.batch.avgleclient.home


import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.CallbackProp
import com.batch.avgleclient.R
import com.batch.avgleclient.databinding.FragmentHomeBinding
import com.batch.avgleclient.model.AvVideo
import kotlinx.android.synthetic.main.item_video.*
import timber.log.Timber

class HomeFragment : Fragment(), VideoListController.ClickListener {

    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding
    private val controller by lazy { VideoListController(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        observeVideos()
        observeLoading()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentHomeBinding.bind(view)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.refreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
        initRecyclerView()
    }

    private fun observeVideos() {
        viewModel.topVideos.observe(this, Observer {
            controller.submitList(it)
        })
    }

    private fun observeLoading() {
        viewModel.loading.observe(this, Observer {
            controller.isLoading = it
        })
    }

    private fun initRecyclerView() {
        binding.topVideosList.apply {
            setHasFixedSize(true)
            setController(controller)
        }
    }

    override fun itemClickListener(item: AvVideo.Response.Video) {
        val tabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setToolbarColor(requireContext().getColor(R.color.colorAccent))
            .build()
        tabsIntent.launchUrl(requireContext(), item.videoUrl.toUri())
    }

    override fun moreClickListener(item: AvVideo.Response.Video) {
//        val menuBuilder = MenuBuilder(requireContext())
//        val inflater = MenuInflater(requireContext())
//        inflater.inflate(R.menu.video_menu, menuBuilder)
//        val optionMenu = MenuPopupHelper(requireContext(), menuBuilder)
//        optionMenu.setForceShowIcon(true)
//        menuBuilder.setCallback {
//
//        }


        val backgroundColor = ContextThemeWrapper(requireContext(), R.style.PopupMenu)
        val popup = PopupMenu(backgroundColor, button_more)
        popup.menuInflater.inflate(R.menu.video_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.watchlater -> {
                    Toast.makeText(requireContext(), "Save to Watch later", Toast.LENGTH_SHORT).show()
                }
                R.id.playlist -> {
                    Toast.makeText(requireContext(), "Save to playlist", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
        try {
            val fieldMPop = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldMPop.isAccessible = true
            val mPopup = fieldMPop.get(popup)
            mPopup.javaClass
                .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(mPopup, true)
        } catch (e: Exception) {
            Timber.e(e.toString())
        } finally {
            popup.show()
        }
    }
}
