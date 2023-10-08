package com.challenge.foodappchallenge3.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.challenge.foodappchallenge3.data.local.datastore.UserPreferenceDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    private val userPreferenceDataSource: UserPreferenceDataSource
) : ViewModel(){
    val userLinearLayoutLiveData = userPreferenceDataSource.getUserLayoutPrefFlow().asLiveData(
        Dispatchers.IO)
    fun setLinearLayoutPref(isUsingLinear : Boolean){
        viewModelScope.launch {
            userPreferenceDataSource.setUserLayoutPref(isUsingLinear)
        }
    }
}