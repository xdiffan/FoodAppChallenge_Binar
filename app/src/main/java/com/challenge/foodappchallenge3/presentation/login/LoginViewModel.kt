package com.challenge.foodappchallenge3.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.challenge.foodappchallenge3.data.repository.UserRepository
import com.challenge.foodappchallenge3.utils.ResultWrapper
import kotlinx.coroutines.launch

class LoginViewModel(private val repo: UserRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<ResultWrapper<Boolean>>()

    val loginResult: LiveData<ResultWrapper<Boolean>>
        get() = _loginResult

    fun doLogin(email: String, password: String) {
        viewModelScope.launch {
            repo.doLogin(email, password).collect {
                _loginResult.postValue(it)
            }
        }
    }
}
