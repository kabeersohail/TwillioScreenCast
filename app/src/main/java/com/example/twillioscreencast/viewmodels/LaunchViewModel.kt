package com.example.twillioscreencast.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LaunchViewModel: ViewModel() {


    private val _screenShareStatus: MutableLiveData<Boolean> = MutableLiveData()
    val screenShareStatus: LiveData<Boolean> = _screenShareStatus

    fun startScreenShare() = _screenShareStatus.postValue(true)

    fun stopScreenShare() = _screenShareStatus.postValue(false)

}