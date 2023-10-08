package com.challenge.foodappchallenge3.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.challenge.foodappchallenge3.R
import com.challenge.foodappchallenge3.data.dummy.DummyCategoryDataSource
import com.challenge.foodappchallenge3.data.dummy.DummyCategoryDataSourceImplementation
import com.challenge.foodappchallenge3.data.local.database.AppDatabase
import com.challenge.foodappchallenge3.data.local.database.datasource.MenuDatabaseDataSource
import com.challenge.foodappchallenge3.data.local.datastore.UserPreferenceDataSourceImpl
import com.challenge.foodappchallenge3.data.local.datastore.appDataStore
import com.challenge.foodappchallenge3.data.repository.MenuRepository
import com.challenge.foodappchallenge3.data.repository.MenuRepositoryImplementation
import com.challenge.foodappchallenge3.databinding.FragmentHomeBinding
import com.challenge.foodappchallenge3.model.Menu
import com.challenge.foodappchallenge3.presentation.detailmenu.DetailMenuActivity
import com.challenge.foodappchallenge3.presentation.main.MainViewModel
import com.challenge.foodappchallenge3.utils.GenericViewModelFactory
import com.challenge.foodappchallenge3.utils.PreferenceDataStoreHelperImpl
import com.challenge.foodappchallenge3.utils.proceedWhen
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent


class FragmentHome : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private val dummyCategoryDataSource : DummyCategoryDataSource by lazy { DummyCategoryDataSourceImplementation() }

    private val adapterMenu: MenuListAdapter by lazy {
        MenuListAdapter(AdapterLayoutMode.LINEAR){
                menu: Menu -> navigateToDetail(menu)
        }
    }

    private val adapterCategory: CategoryListAdapter by lazy {
        CategoryListAdapter()
    }

    private val viewModel: MainViewModel by viewModels{
        val dataStore =  this.requireContext().appDataStore
        val dataStoreHelper = PreferenceDataStoreHelperImpl(dataStore)
        val userPreferenceDataSource = UserPreferenceDataSourceImpl(dataStoreHelper)
        GenericViewModelFactory.create(MainViewModel(userPreferenceDataSource))
    }

    private val homeViewModel: HomeViewModel by viewModels {
        val cds: DummyCategoryDataSource = DummyCategoryDataSourceImplementation()
        val database = AppDatabase.getInstance(requireContext())
        val menuDao = database.menuDao()
        val menuDataSource = MenuDatabaseDataSource(menuDao)
        val repo: MenuRepository = MenuRepositoryImplementation(menuDataSource, cds)
        GenericViewModelFactory.create(HomeViewModel(repo))
    }
    private fun navigateToDetail(menu: Menu) {
        DetailMenuActivity.startActivity(requireContext(), menu)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerViewCategory()
        setRecyclerViewMenu()
        setModeButton()
    }
    private fun setObserveData() {
        homeViewModel.menuData.observe(viewLifecycleOwner){ it ->
            it.proceedWhen(
                doOnSuccess = { result ->
                    binding.layoutState.root.isVisible = false
                    binding.layoutState.pbLoading.isVisible = false
                    binding.layoutState.tvError.isVisible = false
                    binding.rvMenu.isVisible = true
                    result.payload?.let {
                        adapterMenu.setData(it)
                    }
                },
                doOnLoading = {
                    binding.layoutState.root.isVisible = true
                    binding.layoutState.pbLoading.isVisible = true
                    binding.layoutState.tvError.isVisible = false
                    binding.rvMenu.isVisible = false
                },
                doOnError = { err ->
                    binding.layoutState.root.isVisible = true
                    binding.layoutState.pbLoading.isVisible = false
                    binding.layoutState.tvError.isVisible = true
                    binding.layoutState.tvError.text = err.exception?.message.orEmpty()
                    binding.rvMenu.isVisible = false
                }, doOnEmpty = {
                    binding.layoutState.root.isVisible = true
                    binding.layoutState.pbLoading.isVisible = false
                    binding.layoutState.tvError.isVisible = true
                    binding.layoutState.tvError.text = getString(R.string.error_empty)
                    binding.rvMenu.isVisible = false
                }
            )
        }
    }

    private fun setRecyclerViewMenu() {
        val span = if(adapterMenu.adapterLayoutMode == AdapterLayoutMode.LINEAR) 1 else 2
        binding.rvMenu.apply {
            layoutManager = GridLayoutManager(requireContext(),span)
            adapter = this@FragmentHome.adapterMenu
        }
        setObserveData()
    }

    private fun setModeButton() {
        viewModel.userLinearLayoutLiveData.observe(viewLifecycleOwner){
            binding.switchListGrid.isChecked = it
        }

        binding.switchListGrid.setOnCheckedChangeListener { _, isUsingLinear ->
            viewModel.setLinearLayoutPref(isUsingLinear)
            (binding.rvMenu.layoutManager as GridLayoutManager).spanCount = if (isUsingLinear) 2 else 1
            adapterMenu.adapterLayoutMode = if(isUsingLinear) AdapterLayoutMode.GRID else AdapterLayoutMode.LINEAR
            setObserveData()
        }
    }

    private fun setRecyclerViewCategory() {
        binding.rvCategory.apply {
            layoutManager = FlexboxLayoutManager(requireContext())
            (layoutManager as FlexboxLayoutManager).flexDirection = FlexDirection.ROW
            (layoutManager as FlexboxLayoutManager).justifyContent = JustifyContent.SPACE_BETWEEN
            adapter = adapterCategory
            adapterCategory.setData(dummyCategoryDataSource.getCategoryData())
        }
    }
}